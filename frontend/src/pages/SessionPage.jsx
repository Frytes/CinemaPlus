import { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import Navbar from '../components/Navbar';
import Toast from '../components/Toast';
import { Client } from '@stomp/stompjs';

const SessionPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    // --- STATES ---
    const [seats, setSeats] = useState([]);
    const [selectedSeatIds, setSelectedSeatIds] = useState([]);
    const [gridSize, setGridSize] = useState({ rows: 0, cols: 0 });
    const [loading, setLoading] = useState(true);
    const [createdOrder, setCreatedOrder] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [timeLeft, setTimeLeft] = useState(0);
    const [toast, setToast] = useState(null);
    const [tooltip, setTooltip] = useState(null);

    const showToast = (message, type = 'error') => {
        setToast({ message, type });
        setTimeout(() => setToast(null), 2000);
    };

    // --- 1. ЗАГРУЗКА ДАННЫХ ---
    const fetchSeats = useCallback(async () => {
        try {
            const res = await api.get(`/bookings/session/${id}/seats`);
            const data = res.data;
            setSeats(data);

            if (data.length > 0) {
                const maxRow = Math.max(...data.map(s => s.rowIndex)) + 1;
                const maxCol = Math.max(...data.map(s => s.colIndex)) + 1;
                setGridSize({ rows: maxRow, cols: maxCol });
            }
        } catch (err) {
            console.error(err);
            showToast("Не удалось загрузить зал", 'error');
        } finally {
            setLoading(false);
        }
    }, [id]);

    const checkPendingOrder = useCallback(async () => {
        if (!localStorage.getItem('token')) return;
        try {
            const res = await api.get(`/bookings/session/${id}/my-pending`);
            if (res.data) {
                setCreatedOrder(res.data);
                // Не устанавливаем selectedSeatIds - используем effectiveSeatIds
            }
        } catch (err) {
            // Игнорируем
        }
    }, [id]);

    // Инициализация
    useEffect(() => {
        fetchSeats();
        checkPendingOrder();
    }, [fetchSeats, checkPendingOrder]);

    // --- 2. WEBSOCKET ---
    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            debug: (str) => console.log('STOMP:', str),
            onConnect: () => {
                console.log('✅ WebSocket подключен');
                client.subscribe(`/topic/session/${id}`, (message) => {
                    console.log('📨 Получено сообщение:', message.body);
                    try {
                        const body = JSON.parse(message.body);
                        console.log('✅ Парсинг успешен:', body);

                        // Обновляем статус места
                        setSeats(prevSeats => prevSeats.map(seat => {
                            if (seat.id === body.seatId) {
                                const isBookedNow = (body.status === 'LOCKED' || body.status === 'SOLD');
                                return { ...seat, isBooked: isBookedNow };
                            }
                            return seat;
                        }));

                        // Если место заблокировано/продано и у нас нет заказа - убираем из выбранных
                        if ((body.status === 'LOCKED' || body.status === 'SOLD') && !createdOrder) {
                            setSelectedSeatIds(prev => prev.filter(sid => sid !== body.seatId));
                        }

                    } catch (error) {
                        console.error('❌ Ошибка парсинга сообщения:', error);
                    }
                });
            },
            onStompError: (frame) => {
                console.error('❌ WebSocket ошибка:', frame.headers['message']);
            },
            onDisconnect: () => {
                console.log('🔌 WebSocket отключен');
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.activate();

        return () => {
            console.log('🧹 Очистка WebSocket');
            client.deactivate();
        };
    }, [id, createdOrder]); // Добавили createdOrder в зависимости

    // --- 3. ТАЙМЕР ---
    useEffect(() => {
        let timer = null;
        const ORDER_TTL_SECONDS = 600;

        if (createdOrder && createdOrder.createdAt) {
            const createdTime = new Date(createdOrder.createdAt).getTime();
            const expiryTime = createdTime + (ORDER_TTL_SECONDS * 1000);

            const updateTimer = () => {
                const now = Date.now();
                const secondsLeft = Math.floor((expiryTime - now) / 1000);

                if (secondsLeft <= 0) {
                    clearInterval(timer);
                    setTimeLeft(0);
                    handleCancelOrder();
                    showToast("Время бронирования истекло", 'error');
                } else {
                    setTimeLeft(secondsLeft);
                }
            };

            updateTimer();
            timer = setInterval(updateTimer, 1000);
        }

        return () => {
            if (timer) clearInterval(timer);
        };
    }, [createdOrder]);

    // --- ВЫЧИСЛЯЕМЫЕ ЗНАЧЕНИЯ ---
    const effectiveSeatIds = createdOrder ? createdOrder.seatIds : selectedSeatIds;

    // --- 4. ОБРАБОТЧИКИ ---
    const handleSeatClick = (seat) => {
        const isMyBooking = seat.isBooked && effectiveSeatIds.includes(seat.id);

        if (seat.isBooked && !isMyBooking) {
            return;
        }

        if (createdOrder) {
            showToast("Сначала отмените текущую бронь.");
            return;
        }

        if (selectedSeatIds.includes(seat.id)) {
            setSelectedSeatIds(prev => prev.filter(id => id !== seat.id));
        } else {
            if (selectedSeatIds.length >= 5) {
                showToast("Максимум 5 билетов!");
                return;
            }
            setSelectedSeatIds(prev => [...prev, seat.id]);
        }
    };

    const handleCancelOrder = async () => {
        if (!createdOrder) return;

        try {
            setIsProcessing(true);
            await api.post(`/bookings/${createdOrder.orderId}/cancel`);
            showToast("Заказ отменен", "success");
            setCreatedOrder(null);
            setSelectedSeatIds([]);
            setTimeout(() => fetchSeats(), 500);
        } catch (err) {
            console.error("Cancel Error:", err);
            setCreatedOrder(null);
            fetchSeats();
        } finally {
            setIsProcessing(false);
        }
    };

    const handlePay = async () => {
        try {
            setIsProcessing(true);
            const payRes = await api.post(`/bookings/${createdOrder.orderId}/pay`);
            showToast(`Успешно! ${payRes.data.message}`, 'success');
            setCreatedOrder(null);
            setSelectedSeatIds([]);
            setTimeout(() => fetchSeats(), 1500);
        } catch (err) {
            console.error("Payment Error:", err);
            const msg = err.response?.data?.message || 'Ошибка оплаты';
            showToast(msg);
            setCreatedOrder(null);
            setTimeout(() => fetchSeats(), 1500);
        } finally {
            setIsProcessing(false);
        }
    };

    const handleBuy = async () => {
        if (selectedSeatIds.length === 0) return;

        if (!localStorage.getItem('token')) {
            navigate('/login');
            return;
        }

        if (createdOrder) {
            handlePay();
            return;
        }

        try {
            setIsProcessing(true);
            const response = await api.post('/bookings', {
                sessionId: id,
                seatIds: selectedSeatIds
            });
            setCreatedOrder(response.data);
            // Очищаем selectedSeatIds после успешного бронирования
            setSelectedSeatIds([]);
        } catch (err) {
            console.error("Booking Error:", err);
            const msg = err.response?.data?.message || 'Ошибка сервера';
            showToast(msg);

            if (err.response?.status === 409 || err.response?.status === 404) {
                setTimeout(() => fetchSeats(), 1000);
            }
        } finally {
            setIsProcessing(false);
        }
    };

    // --- 5. ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    const handleMouseEnter = (e, seat) => {
        const rect = e.target.getBoundingClientRect();
        setTooltip({ x: rect.left + rect.width / 2, y: rect.top - 10, seat: seat });
    };

    const handleMouseLeave = () => {
        setTooltip(null);
    };

    // --- 6. РЕНДЕР ---
    if (loading) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Загрузка...</div>;

    const currentTotal = seats
        .filter(s => effectiveSeatIds.includes(s.id))
        .reduce((sum, s) => sum + s.price, 0);

    const finalPrice = createdOrder && createdOrder.totalPrice > 0
        ? createdOrder.totalPrice
        : currentTotal;

    // РАСЧЕТ РАЗМЕРА СИДЕНИЙ
    const calculateSeatSize = () => {
        if (gridSize.cols > 45) return 25;
        if (gridSize.cols > 40) return 36;
        if (gridSize.cols > 30) return 40;
        return 45;
    };
    const seatSize = calculateSeatSize();

    return (
        <div style={{ color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column', paddingBottom: '90px', fontFamily: '"Segoe UI", sans-serif' }}>
            <Navbar />
            {toast && <Toast message={toast.message} type={toast.type} />}

            {/* TOOLTIP */}
            {tooltip && (
                <div style={{
                    position: 'fixed',
                    top: tooltip.y,
                    left: tooltip.x,
                    transform: 'translate(-50%, calc(-100% - 8px))',
                    background: 'rgba(20, 20, 20, 0.98)',
                    backdropFilter: 'blur(10px)',
                    border: '1px solid rgba(255, 0, 0, 0.4)',
                    padding: '12px 16px',
                    borderRadius: '8px',
                    pointerEvents: 'none',
                    zIndex: 1000,
                    boxShadow: '0 8px 25px rgba(255, 0, 0, 0.2)',
                    minWidth: '160px',
                    animation: 'fadeIn 0.15s ease-out'
                }}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        marginBottom: '8px'
                    }}>
                        <div style={{
                            fontSize: '0.8rem',
                            color: '#ff6b6b',
                            fontWeight: '600',
                            textTransform: 'uppercase',
                            letterSpacing: '1px'
                        }}>
                            Ряд {tooltip.seat.rowIndex + 1} • Место {tooltip.seat.seatNumber}
                        </div>
                        {tooltip.seat.type === 'VIP' && (
                            <div style={{
                                background: 'linear-gradient(135deg, #ffd700, #ff9900)',
                                color: '#000',
                                fontSize: '0.7rem',
                                fontWeight: 'bold',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                marginLeft: '8px'
                            }}>
                                VIP
                            </div>
                        )}
                    </div>

                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        paddingTop: '8px',
                        borderTop: '1px solid rgba(255, 255, 255, 0.1)'
                    }}>
                        <span style={{
                            fontSize: '0.90rem',
                            color: tooltip.seat.type === 'VIP' ? '#ffd700' : '#cccccc',
                            fontWeight: '600',
                            paddingRight: '15px'
                        }}>
                            {tooltip.seat.type === 'VIP' ? 'VIP' : 'Стандарт'}
                        </span>
                        <span style={{
                            fontSize: '1.1rem',
                            fontWeight: '900',
                            color: tooltip.seat.type === 'VIP' ? '#ffd700' : '#ffffff',
                            whiteSpace: 'nowrap'
                        }}>
                            {tooltip.seat.price} ₽
                        </span>
                    </div>

                    {/* Стрелка вниз */}
                    <div style={{
                        position: 'absolute',
                        bottom: '-6px',
                        left: '50%',
                        transform: 'translateX(-50%)',
                        width: '0',
                        height: '0',
                        borderLeft: '6px solid transparent',
                        borderRight: '6px solid transparent',
                        borderTop: '6px solid rgba(255, 0, 0, 0.4)'
                    }}></div>

                    <style>{`
                        @keyframes fadeIn {
                            from {
                                opacity: 0;
                                transform: translate(-50%, calc(-100% - 15px));
                            }
                            to {
                                opacity: 1;
                                transform: translate(-50%, calc(-100% - 8px));
                            }
                        }
                    `}</style>
                </div>
            )}

            {/* ЗАЛ */}
            <div style={{
                marginTop: '80px', flexGrow: 1, display: 'flex', flexDirection: 'column', alignItems: 'center',
                padding: '20px'
            }}>
                <div style={{ marginTop: '20px', marginBottom: '60px', textAlign: 'center' }}>
                    <h1 style={{
                        margin: 0, color: 'white', textShadow: '0 0 10px #e50914, 0 0 20px #e50914',
                        fontSize: '2.8rem', textTransform: 'uppercase', letterSpacing: '2px'
                    }}>
                        Выберите места
                    </h1>
                </div>

                {/* ЭКРАН */}
                <div style={{
                    width: '65%', height: '50px',
                    background: 'linear-gradient(to bottom, #e0e0e0 0%, #a0a0a0 100%)',
                    marginBottom: '60px', borderRadius: '50% 50% 0 0 / 100% 100% 0 0',
                    boxShadow: '0 15px 50px rgba(255, 255, 255, 0.15)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', paddingTop: '5px'
                }}>
                    <span style={{
                        color: '#1a1a1a', fontWeight: '800', letterSpacing: '8px', fontSize: '0.9rem',
                        textShadow: '0 1px 0 rgba(255,255,255,0.4)'
                    }}>ЭКРАН</span>
                </div>

                {/* СЕТКА */}
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: `repeat(${gridSize.cols}, ${seatSize}px)`,
                    gap: '6px',
                    position: 'relative'
                }}>
                    {seats.map(seat => {
                        const isInMyOrder = createdOrder && createdOrder.seatIds && createdOrder.seatIds.includes(seat.id);
                        const isSelected = effectiveSeatIds.includes(seat.id);
                        const isMyBooking = (seat.isBooked && isSelected) || isInMyOrder;
                        const isVip = seat.type === 'VIP';
                        const isDimmed = createdOrder && !isInMyOrder;

                        // Определяем стиль для VIP мест
                        let seatStyle = {
                            gridRowStart: seat.rowIndex + 1,
                            gridColumnStart: seat.colIndex + 1,
                            width: `${seatSize}px`,
                            height: `${seatSize}px`,
                            fontSize: seatSize < 25 ? '0.6rem' : '0.8rem',
                            borderRadius: seatSize < 25 ? '3px' : '6px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 'bold',
                            userSelect: 'none',
                            transition: 'all 0.2s',
                            cursor: (seat.isBooked && !isMyBooking) || isDimmed ? 'default' : 'pointer',
                            opacity: isMyBooking ? 1 : (isDimmed ? 0.3 : (seat.isBooked ? 0.6 : 1)),
                        };

                        // Цвета для разных состояний
                        if (isMyBooking) {
                            // Мое забронированное место
                            seatStyle.background = '#e50914';
                            seatStyle.border = '2px solid #fff';
                            seatStyle.boxShadow = '0 0 15px #e50914';
                            seatStyle.color = '#fff';
                        } else if (seat.isBooked) {
                            // Занятое место
                            seatStyle.background = '#555';
                            seatStyle.border = '1px solid #777';
                            seatStyle.color = '#bbb';
                        } else if (isSelected) {
                            // Выбранное место
                            seatStyle.background = isVip
                                ? 'linear-gradient(135deg, #e50914, #ff4444)'
                                : '#e50914';
                            seatStyle.border = '2px solid white';
                            seatStyle.boxShadow = isVip
                                ? '0 0 20px rgba(255, 215, 0, 0.8)'
                                : '0 0 15px #e50914';
                            seatStyle.color = '#fff';
                        } else if (isVip) {
                            // Свободное VIP место
                            seatStyle.background = 'linear-gradient(135deg, #ffd700, #ff9900)';
                            seatStyle.border = '2px solid #ff9900';
                            seatStyle.boxShadow = '0 4px 10px rgba(255, 215, 0, 0.4)';
                            seatStyle.color = '#000';
                        } else {
                            // Свободное стандартное место
                            seatStyle.background = '#2ecc71';
                            seatStyle.border = 'none';
                            seatStyle.color = '#fff';
                        }

                        // Специальный значок для VIP
                        const seatContent = isVip && seatSize > 30 ? (
                            <div style={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center'
                            }}>
                                <div style={{ fontSize: seatSize < 40 ? '0.6rem' : '0.7rem', color: isSelected || isMyBooking ? '#fff' : '#000', fontWeight: 'bold' }}>
                                    VIP
                                </div>
                                <div style={{ fontSize: seatSize < 40 ? '0.8rem' : '0.9rem', color: isSelected || isMyBooking ? '#fff' : '#333' }}>
                                    {seat.seatNumber}
                                </div>
                            </div>
                        ) : seat.seatNumber;

                        return (
                            <div
                                key={seat.id}
                                onClick={() => handleSeatClick(seat)}
                                onMouseEnter={(e) => handleMouseEnter(e, seat)}
                                onMouseLeave={handleMouseLeave}
                                style={seatStyle}
                                title={isVip ? `VIP место ${seat.seatNumber}` : `Место ${seat.seatNumber}`}
                            >
                                {seatContent}
                            </div>
                        );
                    })}
                </div>

                {/* ЛЕГЕНДА */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    gap: '20px',
                    marginTop: '40px',
                    flexWrap: 'wrap',
                    padding: '20px',
                    background: 'rgba(30, 30, 30, 0.8)',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '20px', height: '20px', background: '#2ecc71', borderRadius: '4px' }}></div>
                        <span style={{ color: '#ccc', fontSize: '0.9rem' }}>Свободно</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                            width: '20px',
                            height: '20px',
                            background: 'linear-gradient(135deg, #ffd700, #ff9900)',
                            borderRadius: '4px',
                            border: '1px solid #ff9900'
                        }}></div>
                        <span style={{ color: '#ccc', fontSize: '0.9rem' }}>VIP</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '20px', height: '20px', background: '#e50914', borderRadius: '4px' }}></div>
                        <span style={{ color: '#ccc', fontSize: '0.9rem' }}>Выбрано</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '20px', height: '20px', background: '#555', borderRadius: '4px' }}></div>
                        <span style={{ color: '#ccc', fontSize: '0.9rem' }}>Занято</span>
                    </div>
                </div>
            </div>

            {/* НИЖНЯЯ ПАНЕЛЬ */}
            {effectiveSeatIds.length > 0 && (
                <div style={{
                    position: 'fixed', bottom: 0, width: '100%',
                    background: 'rgba(20, 20, 20, 0.95)',
                    backdropFilter: 'blur(15px)',
                    borderTop: '1px solid rgba(255, 255, 255, 0.1)',
                    padding: '15px 50px',
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    boxShadow: '0 -5px 30px rgba(0,0,0,0.5)',
                    zIndex: 200,
                    animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)'
                }}>

                    <div style={{
                        display: 'flex',
                        gap: '15px',
                        alignItems: 'center',
                        maxWidth: '70%',
                        overflowX: 'auto',
                        paddingBottom: '5px',
                        flexWrap: 'wrap'
                    }}>

                        {/* ТАЙМЕР */}
                        {createdOrder && timeLeft > 0 && (
                            <div style={{
                                display: 'flex', alignItems: 'center', gap: '8px',
                                background: 'rgba(231, 76, 60, 0.2)', padding: '5px 12px', borderRadius: '20px',
                                border: '1px solid rgba(231, 76, 60, 0.5)', marginRight: '10px'
                            }}>
                                <span style={{ fontSize: '1.2rem' }}>⏳</span>
                                <span style={{ color: '#ff6b6b', fontWeight: 'bold', fontSize: '1.1rem', fontFamily: 'monospace' }}>
                                    {formatTime(timeLeft)}
                                </span>
                            </div>
                        )}

                        <div style={{ fontSize: '0.9rem', color: '#888', letterSpacing: '1px', textTransform: 'uppercase', whiteSpace: 'nowrap' }}>
                            Билеты:
                        </div>
                        <div style={{
                            display: 'flex',
                            gap: '8px',
                            flexWrap: 'wrap',
                            maxHeight: '60px',
                            overflowY: 'auto'
                        }}>
                            {seats.filter(s => effectiveSeatIds.includes(s.id)).map(s => (
                                <span key={s.id} style={{
                                    background: s.type === 'VIP'
                                        ? 'linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 153, 0, 0.2))'
                                        : '#252525',
                                    padding: '6px 12px',
                                    borderRadius: '4px',
                                    borderLeft: s.type === 'VIP' ? '4px solid #ffd700' : '4px solid #e50914',
                                    fontSize: '0.85rem',
                                    color: s.type === 'VIP' ? '#ffd700' : '#ccc',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '5px'
                                }}>
                                    {s.type === 'VIP' && (
                                        <span style={{
                                            background: '#ffd700',
                                            color: '#000',
                                            fontSize: '0.6rem',
                                            padding: '1px 4px',
                                            borderRadius: '3px',
                                            fontWeight: 'bold'
                                        }}>
                                            VIP
                                        </span>
                                    )}
                                    <span>
                                        Ряд <b style={{color: 'white'}}>{s.rowIndex + 1}</b>
                                        <span style={{margin:'0 5px', opacity:0.3}}>|</span>
                                        Место <b style={{color: 'white'}}>{s.seatNumber}</b>
                                    </span>
                                    <span style={{ marginLeft: '8px', color: s.type === 'VIP' ? '#ffd700' : '#aaa' }}>
                                        {s.price} ₽
                                    </span>
                                </span>
                            ))}
                        </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '0.9rem', color: '#aaa' }}>Итого:</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#fff' }}>
                                {finalPrice} ₽
                            </div>
                        </div>

                        <div style={{ display: 'flex', gap: '15px' }}>
                            {createdOrder ? (
                                <>
                                    <button
                                        onClick={handlePay}
                                        disabled={isProcessing}
                                        style={{
                                            background: '#f39c12',
                                            color: 'black',
                                            border:'none',
                                            padding:'12px 30px',
                                            borderRadius:'8px',
                                            cursor: isProcessing ? 'wait' : 'pointer',
                                            fontWeight:'bold',
                                            fontSize:'1rem',
                                            minWidth: '150px'
                                        }}
                                    >
                                        {isProcessing ? '...' : `Оплатить ${finalPrice} ₽`}
                                    </button>
                                    <button
                                        onClick={handleCancelOrder}
                                        disabled={isProcessing}
                                        style={{
                                            background: 'transparent',
                                            color: '#aaa',
                                            border:'1px solid #555',
                                            padding:'12px 20px',
                                            borderRadius:'8px',
                                            cursor:'pointer',
                                            fontSize:'0.9rem'
                                        }}
                                    >
                                        Отменить
                                    </button>
                                </>
                            ) : (
                                <button
                                    onClick={handleBuy}
                                    disabled={isProcessing}
                                    style={{
                                        background: '#e50914',
                                        color: 'white',
                                        border:'none',
                                        padding:'14px 40px',
                                        borderRadius:'8px',
                                        cursor: 'pointer',
                                        fontWeight:'bold',
                                        fontSize:'1.1rem',
                                        boxShadow: '0 4px 15px rgba(229, 9, 20, 0.4)'
                                    }}
                                >
                                    {isProcessing ? '...' : 'Забронировать'}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                @keyframes slideUp {
                    from { transform: translateY(100%); }
                    to { transform: translateY(0); }
                }
            `}</style>
        </div>
    );
};

export default SessionPage;