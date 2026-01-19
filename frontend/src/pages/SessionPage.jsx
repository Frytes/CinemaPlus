import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const SessionPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [seats, setSeats] = useState([]);
    const [selectedSeatIds, setSelectedSeatIds] = useState([]);
    const [gridSize, setGridSize] = useState({ rows: 0, cols: 0 });
    const [loading, setLoading] = useState(true);

    const [showMenu, setShowMenu] = useState(false);
    const [userEmail, setUserEmail] = useState('');

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));
                setUserEmail(decoded.sub || 'User');
            } catch (e) {}
        }

        const fetchSeats = async () => {
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
                alert("Ошибка загрузки");
            } finally {
                setLoading(false);
            }
        };
        fetchSeats();
    }, [id]);

    const avatarLetter = userEmail ? userEmail.charAt(0).toUpperCase() : 'U';
    const usernameDisplay = userEmail ? userEmail.split('@')[0] : 'Guest';

    const handleLogout = () => {
        localStorage.removeItem('token');
        setUserEmail('');
        setShowMenu(false);
        navigate('/login');
    };

    const handleSeatClick = (seat) => {
        if (seat.isBooked) return;
        if (selectedSeatIds.includes(seat.id)) {
            setSelectedSeatIds(prev => prev.filter(id => id !== seat.id));
        } else {
            if (selectedSeatIds.length >= 5) {
                alert("Максимум 5 билетов!");
                return;
            }
            setSelectedSeatIds(prev => [...prev, seat.id]);
        }
    };

    const handleBuy = async () => {
        if (selectedSeatIds.length === 0) return;
        if (!localStorage.getItem('token')) {
            navigate('/login');
            return;
        }
        try {
            await api.post('/bookings', { sessionId: id, seatIds: selectedSeatIds });
            alert('Билеты успешно куплены! 🎟️');
            window.location.reload();
        } catch (err) {
            alert('Ошибка: ' + (err.response?.data?.message || 'Server Error'));
            window.location.reload();
        }
    };

    const totalPrice = seats
        .filter(s => selectedSeatIds.includes(s.id))
        .reduce((sum, s) => sum + s.price, 0);

    const calculateSeatSize = () => {
        if (gridSize.cols > 45) return 25;
        if (gridSize.cols > 40) return 36;
        if (gridSize.cols > 30) return 40;
        return 45;
    };
    const seatSize = calculateSeatSize();
    const fontSize = seatSize < 25 ? '0.6rem' : '0.8rem';
    const borderRadius = seatSize < 25 ? '3px' : '6px';

    if (loading) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Загрузка зала...</div>;

    return (
        <div style={{ color: 'white', paddingBottom: '50px', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>

            {/* --- ХЕДЕР --- */}
            <header style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '15px 50px',
                background: 'rgba(20, 20, 20, 0.9)',
                backdropFilter: 'blur(10px)',
                position: 'fixed', width: '100%', top: 0, zIndex: 100,
                borderBottom: '1px solid #333',
                boxSizing: 'border-box'
            }}>
                <h1 style={{ color: '#e50914', margin: 0, fontSize: '1.8rem', cursor: 'pointer' }} onClick={() => navigate('/')}>
                    CinemaPlus
                </h1>
                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                    <button className="nav-btn" onClick={() => navigate('/halls')}>Залы</button>
                    {userEmail && <button className="nav-btn">Мои билеты</button>}
                    {!userEmail ? (
                        <button className="nav-btn logout" onClick={() => navigate('/login')} style={{ fontWeight: 'bold' }}>Войти</button>
                    ) : (
                        <div className="profile-wrapper">
                            <div className="avatar-circle" onClick={() => setShowMenu(!showMenu)}>{avatarLetter}</div>
                            {showMenu && (
                                <div className="profile-dropdown">
                                    <div className="user-details">
                                        <div className="detail-row"><span className="detail-label">Имя аккаунта</span><span className="detail-value">{usernameDisplay}</span></div>
                                        <div className="detail-row"><span className="detail-label">Email</span><span className="detail-value">{userEmail}</span></div>
                                    </div>
                                    <div className="menu-divider"></div>
                                    <div className="menu-item">Сменить имя</div>
                                    <div className="menu-item">Сменить пароль</div>
                                    <div className="menu-item">Сменить Email</div>
                                    <div className="menu-divider"></div>
                                    <div className="menu-item logout-item" onClick={handleLogout}>Выйти</div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </header>

            {/* --- КОНТЕНТ --- */}
            <div style={{
                marginTop: '100px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                padding: '0 20px'
            }}>

                {/* ВЕРХНЯЯ ПАНЕЛЬ с кнопкой "Назад" и заголовком */}
                <div style={{
                    width: '100%',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '80px',
                    padding: '0 20px'
                }}>
                    {/* Кнопка НАЗАД */}
                    <button
                        onClick={() => navigate(-1)}
                        style={{
                            padding: '12px 24px',
                            background: '#333',
                            border: 'none',
                            color: 'white',
                            fontSize: '1rem',
                            fontWeight: 'bold',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            transition: 'all 0.3s',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '8px',
                            width: 'auto',
                            minWidth: '150px'
                        }}
                        onMouseOver={(e) => {
                            e.currentTarget.style.background = '#e50914';
                            e.currentTarget.style.transform = 'scale(1.05)';
                        }}
                        onMouseOut={(e) => {
                            e.currentTarget.style.background = '#333';
                            e.currentTarget.style.transform = 'scale(1)';
                        }}
                    >
                        &larr; Назад
                    </button>

                    {/*  "Выберите места" */}
                    <h1 style={{
                        textAlign: 'center',
                        margin: 0,
                        color: 'white',
                        textShadow: '0 0 10px #e50914, 0 0 20px #e50914',
                        flex: 1
                    }}>
                        Выберите места
                    </h1>

                    {/* Пустой элемент для балансировки */}
                    <div style={{ width: '150px' }}></div>
                </div>

                {/* ЭКРАН */}
                <div style={{
                    width: '60%',
                    height: '50px',
                    background: 'linear-gradient(to bottom, rgba(255,255,255,1), rgba(200,200,200,0.8))',
                    marginBottom: '60px',
                    borderRadius: '50% 50% 0 0 / 100% 100% 0 0',
                    boxShadow: '0 -10px 60px rgba(255,255,255,0.8), 0 0 30px rgba(229, 9, 20, 0.4)',
                    position: 'relative',
                    border: '1px solid rgba(255,255,255,0.3)'
                }}>
                    <span style={{
                        position: 'absolute',
                        width: '100%',
                        textAlign: 'center',
                        top: '15px',
                        color: '#000',
                        fontWeight: 'bold',
                        fontSize: '0.9rem',
                        letterSpacing: '8px',
                        textShadow: '0 0 5px rgba(255,255,255,0.7)',
                        fontFamily: 'Arial, sans-serif',
                        textTransform: 'uppercase'
                    }}>
                        ЭКРАН
                    </span>
                </div>

                {/* СЕТКА */}
                <div style={{
                    width: '100%',
                    overflowX: 'auto',
                    paddingBottom: '20px',
                    display: 'flex',
                    justifyContent: 'center'
                }}>
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: `repeat(${gridSize.cols}, ${seatSize}px)`,
                        gap: '4px'
                    }}>
                        {seats.map(seat => {
                            const isSelected = selectedSeatIds.includes(seat.id);
                            const isVip = seat.type === 'VIP';
                            return (
                                <div
                                    key={seat.id}
                                    onClick={() => handleSeatClick(seat)}
                                    style={{
                                        gridRowStart: seat.rowIndex + 1,
                                        gridColumnStart: seat.colIndex + 1,
                                        width: `${seatSize}px`,
                                        height: `${seatSize}px`,
                                        fontSize: fontSize,
                                        borderRadius: borderRadius,
                                        cursor: seat.isBooked ? 'not-allowed' : 'pointer',
                                        background: seat.isBooked
                                            ? '#333'
                                            : isSelected ? '#e50914' : isVip ? '#ffd700' : '#2ecc71',
                                        boxShadow: isSelected ? '0 0 10px #e50914' : 'inset 0 0 5px rgba(0,0,0,0.3)',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        color: seat.isBooked ? '#555' : '#121212',
                                        fontWeight: 'bold',
                                        userSelect: 'none',
                                        transition: 'all 0.1s'
                                    }}
                                >
                                    {seat.seatNumber}
                                </div>
                            );
                        })}
                    </div>
                </div>

                {/* ПАНЕЛЬ ПОКУПКИ */}
                <div style={{
                    marginTop: '30px',
                    background: 'rgba(30,30,30,0.95)',
                    padding: '20px 50px',
                    borderRadius: '16px',
                    boxShadow: '0 10px 40px rgba(0,0,0,0.5)',
                    border: '1px solid #444',
                    minWidth: '350px',
                    textAlign: 'center'
                }}>
                    <div style={{ marginBottom: '10px', color: '#ccc', fontSize: '0.9rem' }}>
                        Выбрано билетов: <span style={{ color: 'white', fontWeight: 'bold' }}>{selectedSeatIds.length}</span>
                    </div>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#e50914', marginBottom: '20px' }}>
                        {totalPrice} ₽
                    </div>
                    <button
                        className="buy-btn"
                        disabled={selectedSeatIds.length === 0}
                        style={{
                            opacity: selectedSeatIds.length === 0 ? 0.5 : 1,
                            width: '100%',
                            padding: '15px',
                            fontSize: '1.2rem',
                            background: selectedSeatIds.length === 0 ? '#444' : '#e50914'
                        }}
                        onClick={handleBuy}
                    >
                        Оплатить заказ
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SessionPage;