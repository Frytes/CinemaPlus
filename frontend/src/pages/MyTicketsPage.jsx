import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import Navbar from '../components/Navbar';
import Toast from '../components/Toast';

const MyTicketsPage = () => {
    const navigate = useNavigate();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [toast, setToast] = useState(null);
    const [processingId, setProcessingId] = useState(null);

    const showToast = (message, type = 'error') => {
        setToast({ message, type });
        setTimeout(() => setToast(null), 3000);
    };

    const fetchMyTickets = async () => {
        try {
            setLoading(true);
            const response = await api.get('/bookings/my-tickets');
            setOrders(response.data);
        } catch (err) {
            console.error(err);
            if (err.response?.status === 401) navigate('/login');
            else showToast('Не удалось загрузить историю', 'error');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchMyTickets(); }, []);

    const handleCancelBooking = async (orderId) => {
        if (!window.confirm('Отменить бронирование?')) return;
        try {
            await api.post(`/bookings/${orderId}/cancel`);
            showToast('Бронь отменена', 'success');
            fetchMyTickets();
        } catch (err) { showToast('Ошибка отмены', 'error'); }
    };

    const handlePay = async (orderId) => {
        try {
            setProcessingId(orderId);
            const payRes = await api.post(`/bookings/${orderId}/pay`);
            showToast(`Успешно! ${payRes.data.message}`, 'success');
            fetchMyTickets();
        } catch (err) {
            const msg = err.response?.data?.message || 'Ошибка оплаты';
            showToast(msg);
            fetchMyTickets();
        } finally {
            setProcessingId(null);
        }
    };

    const formatDate = (isoDate) => {
        return new Date(isoDate).toLocaleString('ru-RU', {
            day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit'
        });
    };

    // --- ЛОГИКА СТАТУСОВ ---
    const getStatusInfo = (order, isArchive) => {
        if (isArchive) {
            return { text: 'ЗАВЕРШЕН', color: '#888', border: '1px solid #555', bg: 'rgba(255,255,255,0.05)', icon: '🏁' };
        }

        switch (order.status) {
            case 'PAID': return { text: 'АКТИВЕН', color: '#2ecc71', border: 'none', bg: 'rgba(46, 204, 113, 0.15)', icon: '✅' };
            case 'PENDING': return { text: 'ЖДЕТ ОПЛАТЫ', color: '#f39c12', border: '1px solid #f39c12', bg: 'rgba(243, 156, 18, 0.1)', icon: '⏳' };
            case 'CANCELLED': return { text: 'ОТМЕНЕН', color: '#e74c3c', border: '1px solid #e74c3c', bg: 'rgba(231, 76, 60, 0.05)', icon: '❌' };
            default: return { text: order.status, color: '#aaa', border: '1px solid #555', bg: 'transparent', icon: '?' };
        }
    };

    // --- СОРТИРОВКА И ГРУППИРОВКА ---
    const now = new Date();

    const pendingOrders = orders
        .filter(o => o.status === 'PENDING')
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const paidOrders = orders.filter(o => o.status === 'PAID');

    const activeOrders = paidOrders
        .filter(o => {
            const sessionStart = new Date(o.tickets[0]?.startTime);
            const sessionEnd = new Date(sessionStart.getTime() + 2 * 60 * 60 * 1000);
            return sessionEnd > now;
        })
        .sort((a, b) => new Date(a.tickets[0]?.startTime) - new Date(b.tickets[0]?.startTime));

    const archiveOrders = paidOrders
        .filter(o => {
            const sessionStart = new Date(o.tickets[0]?.startTime);
            const sessionEnd = new Date(sessionStart.getTime() + 2 * 60 * 60 * 1000);
            return sessionEnd <= now;
        })
        .sort((a, b) => new Date(b.tickets[0]?.startTime) - new Date(a.tickets[0]?.startTime));

    // --- РЕНДЕР КАРТОЧКИ ---
    const renderOrderCard = (order, isArchive = false) => {
        const status = getStatusInfo(order, isArchive);
        const movieInfo = order.tickets[0] || {};
        const isPaying = processingId === order.orderId;

        return (
            <div key={order.orderId} style={{
                display: 'flex',
                background: isArchive ? '#1a1a1a' : '#1e1e1e',
                borderRadius: '16px',
                overflow: 'hidden',
                boxShadow: isArchive ? 'none' : '0 10px 30px rgba(0,0,0,0.5)',
                border: order.status === 'PENDING' ? '1px solid #f39c12' : (isArchive ? '1px solid #333' : '1px solid #444'),
                marginBottom: '20px',
                position: 'relative',
                opacity: isArchive ? 0.85 : 1,
                transition: 'all 0.2s'
            }}>
                {/* ЛЕВАЯ ЧАСТЬ */}
                <div style={{ flex: 1, padding: '25px', display: 'flex', flexDirection: 'column' }}>

                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                        <span style={{
                            color: status.color, background: status.bg, border: status.border,
                            padding: '4px 10px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold', letterSpacing: '1px',
                            display: 'flex', alignItems: 'center', gap: '6px'
                        }}>
                            <span>{status.icon}</span> {status.text}
                        </span>
                        <span style={{ color: '#555', fontSize: '0.9rem' }}>#{order.orderId}</span>
                    </div>

                    <h2 style={{ margin: '0 0 10px 0', color: isArchive ? '#ccc' : 'white', fontSize: '1.6rem' }}>
                        {movieInfo.movieTitle || 'Билет'}
                    </h2>
                    <div style={{ color: '#888', marginBottom: '20px', fontSize: '1rem' }}>
                        {movieInfo.startTime ? formatDate(movieInfo.startTime) : 'Дата не указана'}
                        <span style={{margin: '0 10px', color: '#555'}}>|</span>
                        {movieInfo.hallName}
                    </div>

                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginBottom: 'auto' }}>
                        {order.tickets.map(t => (
                            <div key={t.ticketId} style={{
                                background: '#252525', padding: '6px 12px', borderRadius: '4px',
                                borderLeft: isArchive ? '2px solid #555' : '2px solid #e50914',
                                fontSize: '0.85rem', color: isArchive ? '#999' : '#ccc'
                            }}>
                                Ряд <b style={{color: isArchive ? '#bbb' : 'white'}}>{t.rowIndex + 1}</b>
                                <span style={{margin:'0 5px', opacity:0.3}}>|</span>
                                Место <b style={{color: isArchive ? '#bbb' : 'white'}}>{t.seatNumber}</b>
                            </div>
                        ))}
                    </div>

                    {order.status === 'PENDING' && (
                        <div style={{ marginTop: '20px', display: 'flex', gap: '15px' }}>
                            <button
                                onClick={() => handlePay(order.orderId)}
                                disabled={isPaying}
                                style={{ background: '#f39c12', color: 'black', border:'none', padding:'10px 20px', borderRadius:'6px', cursor: isPaying ? 'wait' : 'pointer', fontWeight:'bold', fontSize:'0.9rem', minWidth:'120px' }}
                            >
                                {isPaying ? '...' : `Оплатить ${order.totalPrice} ₽`}
                            </button>
                            <button
                                onClick={() => handleCancelBooking(order.orderId)}
                                disabled={isPaying}
                                style={{ background: 'transparent', color: '#aaa', border:'1px solid #555', padding:'10px 20px', borderRadius:'6px', cursor:'pointer', fontSize:'0.9rem' }}
                            >
                                Отменить
                            </button>
                        </div>
                    )}
                </div>

                {/* РАЗДЕЛИТЕЛЬ */}
                <div style={{ width: '2px', background: 'transparent', borderLeft: '2px dashed #333', position: 'relative' }}>
                    <div style={{ position: 'absolute', top: '-10px', left: '-10px', width: '20px', height: '20px', background: '#121212', borderRadius: '50%' }}></div>
                    <div style={{ position: 'absolute', bottom: '-10px', left: '-10px', width: '20px', height: '20px', background: '#121212', borderRadius: '50%' }}></div>
                </div>

                {/* ПРАВАЯ ЧАСТЬ */}
                <div style={{ width: '180px', background: '#181818', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    {order.status === 'PAID' && !isArchive ? (
                        <div style={{ background: 'white', padding: '8px', borderRadius: '4px' }}>
                            <img src={`https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=ORDER-${order.orderId}`} alt="QR" style={{ display: 'block' }} />
                        </div>
                    ) : (
                        <div style={{ color: '#333', fontSize: '3rem', opacity: 0.5 }}>
                            {isArchive ? '🏁' : (order.status === 'CANCELLED' ? '❌' : '⏳')}
                        </div>
                    )}
                </div>
            </div>
        );
    };

    if (loading) return <div style={{ color: 'white', textAlign: 'center', marginTop: '100px' }}>Загрузка...</div>;

    // Стили заголовков
    const sectionTitleStyle = {
        color: '#fff',
        marginBottom: '20px', fontSize: '1.2rem', fontWeight: 'bold',
        textTransform: 'uppercase', letterSpacing: '2px',
        borderLeft: '4px solid #e50914', paddingLeft: '15px',
        textShadow: '0 0 10px rgba(229, 9, 20, 0.3)'
    };

    return (
        <div style={{ color: 'white', minHeight: '100vh', paddingBottom: '80px', fontFamily: '"Segoe UI", sans-serif' }}>
            <Navbar />
            {toast && <Toast message={toast.message} type={toast.type} />}

            <div style={{ maxWidth: '900px', margin: '80px auto', padding: '0 20px' }}>

                <h1 style={{
                    textAlign: 'center', marginBottom: '60px',
                    color: 'white', fontSize: '2.5rem', letterSpacing: '2px', textTransform: 'uppercase',
                    textShadow: '0 0 10px #e50914, 0 0 20px #e50914'
                }}>
                    Мои билеты
                </h1>

                {pendingOrders.length > 0 && (
                    <div style={{ marginBottom: '50px' }}>
                        <div style={{...sectionTitleStyle, borderColor: '#f39c12', color: '#f39c12', textShadow: '0 0 10px rgba(243, 156, 18, 0.3)'}}>
                            ⚠️ Требует оплаты
                        </div>
                        {pendingOrders.map(o => renderOrderCard(o))}
                    </div>
                )}

                {activeOrders.length > 0 && (
                    <div style={{ marginBottom: '50px' }}>
                        <div style={sectionTitleStyle}>
                            Ближайшие сеансы
                        </div>
                        {activeOrders.map(o => renderOrderCard(o))}
                    </div>
                )}

                {archiveOrders.length > 0 && (
                    <div>
                        <div style={{...sectionTitleStyle, borderColor: '#555', color: '#888', textShadow: 'none'}}>
                            Архив заказов
                        </div>
                        {archiveOrders.map(o => renderOrderCard(o, true))}
                    </div>
                )}

                {orders.length === 0 && (
                    <div style={{ textAlign: 'center', color: '#555', padding: '50px' }}>
                        <h2>История пуста</h2>
                        <button onClick={() => navigate('/')} style={{marginTop:'20px', background:'#333', color:'white', border:'none', padding:'10px 20px', borderRadius:'6px', cursor:'pointer'}}>В афишу</button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MyTicketsPage;