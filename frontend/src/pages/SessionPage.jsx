import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import Navbar from '../components/Navbar';
import Toast from '../components/Toast';

const SessionPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    // --- STATES ---
    const [seats, setSeats] = useState([]);
    const [selectedSeatIds, setSelectedSeatIds] = useState([]);
    const [gridSize, setGridSize] = useState({ rows: 0, cols: 0 });
    const [loading, setLoading] = useState(true);

    // Заказ
    const [createdOrder, setCreatedOrder] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);

    // Таймер
    const [timeLeft, setTimeLeft] = useState(0);

    // UI
    const [toast, setToast] = useState(null);
    const [tooltip, setTooltip] = useState(null);

    const showToast = (message, type = 'error') => {
        setToast({ message, type });
        setTimeout(() => {
            setToast(null);
        }, 2000);
    };

    // --- 1. ЗАГРУЗКА ДАННЫХ (Зал + Активный заказ) ---
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
            setToast({ message: "Не удалось загрузить зал", type: 'error' });
        } finally {
            setLoading(false);
        }
    }, [id]);

    const checkPendingOrder = useCallback(async () => {
        if (!localStorage.getItem('token')) return;
        try {
            const res = await api.get(`/bookings/session/${id}/my-pending`);
            if (res.data) {
                console.log("Restored order:", res.data);
                setCreatedOrder(res.data);
                if (res.data.seatIds && res.data.seatIds.length > 0) {
                    setSelectedSeatIds(res.data.seatIds);
                }
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

    // --- 2. ТАЙМЕР ---
    const ORDER_TTL_SECONDS = 600;

    useEffect(() => {
        let timer = null;

        if (createdOrder && createdOrder.createdAt) {
            const startTimer = () => {
                const createdTime = new Date(createdOrder.createdAt).getTime();
                const expiryTime = createdTime + (ORDER_TTL_SECONDS * 1000);

                timer = setInterval(() => {
                    const now = Date.now();
                    const secondsLeft = Math.floor((expiryTime - now) / 1000);

                    if (secondsLeft <= 0) {
                        clearInterval(timer);
                        setTimeLeft(0);
                        handleCancelOrder();
                        setToast({ message: "Время бронирования истекло", type: 'error' });
                    } else {
                        setTimeLeft(secondsLeft);
                    }
                }, 1000);
            };

            startTimer();
        } else {
            clearInterval(timer);
        }
        return () => clearInterval(timer);
    }, [createdOrder]);

    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    // --- 3. ДЕЙСТВИЯ (Клик, Отмена, Оплата, Бронь) ---
    const handleSeatClick = (seat) => {
        const isMyBooking = seat.isBooked && selectedSeatIds.includes(seat.id);

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

    // --- TOOLTIP ---
    const handleMouseEnter = (e, seat) => {
        const rect = e.target.getBoundingClientRect();
        setTooltip({ x: rect.left + rect.width / 2, y: rect.top - 10, seat: seat });
    };
    const handleMouseLeave = () => { setTooltip(null); };

    // --- РАСЧЕТЫ ---
    const currentTotal = seats
        .filter(s => selectedSeatIds.includes(s.id))
        .reduce((sum, s) => sum + s.price, 0);

    // ФИКС: Если у createdOrder нет totalPrice или он 0, используем currentTotal
    const finalPrice = createdOrder ?
        (createdOrder.totalPrice > 0 ? createdOrder.totalPrice : currentTotal) :
        currentTotal;

    // --- ВИЗУАЛ ---
    const calculateSeatSize = () => {
        if (gridSize.cols > 45) return 25;
        if (gridSize.cols > 40) return 36;
        if (gridSize.cols > 30) return 40;
        return 45;
    };
    const seatSize = calculateSeatSize();
    const fontSize = seatSize < 25 ? '0.6rem' : '0.8rem';
    const borderRadius = seatSize < 25 ? '3px' : '6px';

    if (loading) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Загрузка...</div>;

    return (
        <div style={{ color: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column', paddingBottom: '90px', fontFamily: '"Segoe UI", sans-serif' }}>

            <Navbar />
            {toast && <Toast message={toast.message} type={toast.type} />}

            {/* TOOLTIP */}
            {tooltip && (
                <div style={{
                    position: 'fixed', top: tooltip.y, left: tooltip.x,
                    transform: 'translate(-50%, -100%)', background: 'rgba(0, 0, 0, 0.95)',
                    border: `1px solid ${tooltip.seat.type === 'VIP' ? '#ffd700' : '#e50914'}`,
                    padding: '8px 12px', borderRadius: '6px', pointerEvents: 'none', zIndex: 1000,
                    boxShadow: '0 4px 15px rgba(0,0,0,0.5)', whiteSpace: 'nowrap', textAlign: 'center'
                }}>
                    <div style={{ fontSize: '0.9rem', fontWeight: 'bold', color: 'white' }}>
                        Ряд {tooltip.seat.rowIndex + 1}, Место {tooltip.seat.seatNumber}
                    </div>
                    <div style={{ fontSize: '0.8rem', color: tooltip.seat.type === 'VIP' ? '#ffd700' : '#ccc', marginTop: '2px' }}>
                        {tooltip.seat.type === 'VIP' ? '💎 VIP' : 'Стандарт'} • {tooltip.seat.price} ₽
                    </div>
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
                    gap: '6px'
                }}>
                    {seats.map(seat => {
                        const isSelected = selectedSeatIds.includes(seat.id);
                        const isVip = seat.type === 'VIP';
                        const isDimmed = createdOrder && !isSelected;
                        const isMyBooking = seat.isBooked && isSelected;

                        return (
                            <div
                                key={seat.id}
                                onClick={() => handleSeatClick(seat)}
                                onMouseEnter={(e) => handleMouseEnter(e, seat)}
                                onMouseLeave={handleMouseLeave}
                                style={{
                                    gridRowStart: seat.rowIndex + 1,
                                    gridColumnStart: seat.colIndex + 1,
                                    width: `${seatSize}px`,
                                    height: `${seatSize}px`,
                                    fontSize: fontSize,
                                    borderRadius: borderRadius,

                                    background: isMyBooking
                                        ? '#e50914'
                                        : (seat.isBooked
                                            ? '#555'
                                            : (isSelected ? '#e50914' : (isVip ? '#ffd700' : '#2ecc71'))),

                                    border: isMyBooking
                                        ? '2px solid #fff'
                                        : (seat.isBooked ? '1px solid #777' : (isSelected ? '2px solid white' : 'none')),

                                    opacity: isMyBooking ? 1 : (isDimmed ? 0.3 : (seat.isBooked ? 0.6 : 1)),

                                    cursor: (seat.isBooked && !isMyBooking) || isDimmed ? 'default' : 'pointer',

                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    color: '#121212', fontWeight: 'bold', userSelect: 'none',
                                    boxShadow: isSelected ? '0 0 15px #e50914' : 'none',
                                    transition: 'all 0.2s',
                                }}
                            >
                                {seat.seatNumber}
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* --- НИЖНЯЯ ПАНЕЛЬ --- */}
            {selectedSeatIds.length > 0 && (
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
                    {/* ИСПРАВЛЕНИЕ: Увеличил maxWidth с 50% до 60% и добавил flex-wrap */}
                    <div style={{
                        display: 'flex',
                        gap: '15px',
                        alignItems: 'center',
                        maxWidth: '60%',
                        overflowX: 'auto',
                        paddingBottom: '5px',
                        flexWrap: 'wrap'  /* Добавил перенос на новую строку если не помещается */
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
                            flexWrap: 'wrap',  /* Перенос билетов на новую строку */
                            maxHeight: '60px', /* Ограничение высоты для 2 строк */
                            overflowY: 'auto'  /* Прокрутка по вертикали если много билетов */
                        }}>
                            {seats.filter(s => selectedSeatIds.includes(s.id)).map(s => (
                                <span key={s.id} style={{
                                    background: '#333', color: 'white',
                                    padding: '6px 12px', borderRadius: '6px', fontSize: '0.9rem', fontWeight: '500',
                                    border: s.type === 'VIP' ? '1px solid #ffd700' : '1px solid #444',
                                    boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                                    whiteSpace: 'nowrap',
                                    flexShrink: 0  /* Чтобы элементы не сжимались */
                                }}>
                                    <span style={{color: '#aaa', fontSize: '0.8rem'}}>Ряд</span> {s.rowIndex + 1} <span style={{color: '#555'}}>|</span> <span style={{color: '#aaa', fontSize: '0.8rem'}}>Место</span> {s.seatNumber}
                                </span>
                            ))}
                        </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '30px', flexShrink: 0 }}>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: '2px' }}>Итого к оплате</div>
                            <div style={{ fontSize: '1.6rem', fontWeight: '700', color: 'white', lineHeight: '1' }}>
                                {finalPrice}&nbsp;₽
                            </div>
                        </div>

                        {/* КНОПКИ */}
                        <div style={{ display: 'flex', gap: '15px' }}>
                            {createdOrder && (
                                <button onClick={handleCancelOrder} style={{
                                    background: 'transparent', border: '1px solid #555', color: '#ccc',
                                    padding: '12px 25px', fontSize: '1rem', borderRadius: '30px', cursor: 'pointer'
                                }}>Отмена</button>
                            )}
                            <button
                                onClick={createdOrder ? handlePay : handleBuy}
                                disabled={isProcessing}
                                style={{
                                    background: createdOrder ? '#2ecc71' : 'linear-gradient(135deg, #e50914 0%, #ff4f4f 100%)',
                                    color: 'white', border: 'none', padding: '12px 40px', fontSize: '1.1rem', fontWeight: '600',
                                    borderRadius: '30px', cursor: 'pointer', minWidth: '200px'
                                }}
                            >
                                {isProcessing ? '...' : (createdOrder ? 'Оплатить' : 'Забронировать')}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <style>{`@keyframes slideUp {from{transform:translateY(100%);}to{transform:translateY(0);}}`}</style>
        </div>
    );
};

export default SessionPage;