import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

const DashboardStats = ({ showToast }) => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [hoveredBar, setHoveredBar] = useState(null);

    useEffect(() => {
        api.get('/stats')
            .then(res => {
                setStats(res.data);
                showToast('Статистика загружена', 'success');
            })
            .catch(err => {
                console.error(err);
                showToast('Ошибка загрузки статистики', 'error');
            })
            .finally(() => setLoading(false));
    }, [showToast]);

    if (loading) return <div style={{color:'#aaa'}}>Загрузка статистики...</div>;
    if (!stats) return <div style={{color:'#e74c3c'}}>Ошибка загрузки</div>;

    const maxRevenue = Math.max(...Object.values(stats.dailyRevenue), 1);

    const handleBarHover = (date, value, index) => {
        setHoveredBar({ date, value, index });
    };

    const handleBarLeave = () => {
        setHoveredBar(null);
    };

    const handleBarClick = (date, value) => {
        showToast(`${date}: ${value} ₽`, 'success');
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <h2 style={{ marginTop: 0, borderBottom: '1px solid #333', paddingBottom: '15px' }}>📊 Панель управления</h2>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
                <StatCard
                    title="Выручка сегодня"
                    value={`${stats.revenueToday} ₽`}
                    color="#2ecc71"
                    icon="💰"
                    showToast={showToast}
                />
                <StatCard
                    title="Билетов сегодня"
                    value={stats.ticketsToday}
                    color="#3498db"
                    icon="🎟️"
                    showToast={showToast}
                />
                <StatCard
                    title="Выручка (Месяц)"
                    value={`${stats.revenueMonth} ₽`}
                    color="#f1c40f"
                    icon="📅"
                    showToast={showToast}
                />
                <StatCard
                    title="Пользователей"
                    value={stats.totalUsers}
                    color="#9b59b6"
                    icon="👥"
                    showToast={showToast}
                />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '30px', alignItems: 'start' }}>

                <div style={cardStyle}>
                    <h3 style={{ margin: '0 0 20px 0', color: '#aaa', fontSize: '1rem' }}>📈 Динамика выручки (7 дней)</h3>

                    <div style={{
                        display: 'flex',
                        height: '200px',
                        gap: '10px',
                        paddingBottom: '10px',
                        alignItems: 'stretch'
                    }}>
                        {Object.entries(stats.dailyRevenue).map(([date, value], index) => {
                            const heightPercent = (value / maxRevenue) * 100;
                            const isHovered = hoveredBar?.index === index;

                            return (
                                <div
                                    key={date}
                                    style={{
                                        flex: 1,
                                        display: 'flex',
                                        flexDirection: 'column',
                                        justifyContent: 'flex-end',
                                        alignItems: 'center',
                                        gap: '5px',
                                        height: '100%',
                                        cursor: 'pointer'
                                    }}
                                    onMouseEnter={() => handleBarHover(date, value, index)}
                                    onMouseLeave={handleBarLeave}
                                    onClick={() => handleBarClick(date, value)}
                                >
                                    <div
                                        style={{
                                            width: '100%',
                                            height: `${Math.max(heightPercent, 1)}%`,
                                            background: value > 0
                                                ? isHovered
                                                    ? 'linear-gradient(to top, #e50914, #ff8c94)'
                                                    : 'linear-gradient(to top, #e50914, #ff6b6b)'
                                                : '#333',
                                            borderRadius: '4px 4px 0 0',
                                            transition: 'all 0.2s ease',
                                            minHeight: '4px',
                                            position: 'relative',
                                            transform: isHovered ? 'scale(1.05)' : 'scale(1)',
                                            boxShadow: isHovered
                                                ? '0 0 15px rgba(229, 9, 20, 0.5)'
                                                : 'none',
                                            zIndex: isHovered ? 1 : 0
                                        }}
                                    >
                                        {isHovered && (
                                            <div style={{
                                                position: 'absolute',
                                                top: '-40px',
                                                left: '50%',
                                                transform: 'translateX(-50%)',
                                                background: 'rgba(0, 0, 0, 0.9)',
                                                color: '#fff',
                                                padding: '5px 10px',
                                                borderRadius: '4px',
                                                fontSize: '12px',
                                                whiteSpace: 'nowrap',
                                                border: '1px solid #444',
                                                zIndex: 10
                                            }}>
                                                <strong>{date}</strong><br />
                                                {value} ₽
                                            </div>
                                        )}
                                    </div>

                                    <span style={{
                                        fontSize: '0.75rem',
                                        color: isHovered ? '#fff' : '#888',
                                        fontWeight: isHovered ? 'bold' : 'normal',
                                        transition: 'all 0.2s ease'
                                    }}>
                                        {date}
                                    </span>
                                </div>
                            );
                        })}
                    </div>


                </div>

                <div style={cardStyle}>
                    <h3 style={{ margin: '0 0 15px 0', color: '#aaa', fontSize: '1rem' }}>🏆 Топ фильмов (по сборам)</h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {stats.topMovies.length === 0 && <div style={{color:'#666'}}>Нет данных</div>}
                        {stats.topMovies.map((m, idx) => (
                            <div
                                key={idx}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    padding: '10px',
                                    background: 'rgba(255,255,255,0.03)',
                                    borderRadius: '6px',
                                    cursor: 'pointer'
                                }}
                                onClick={() => {
                                    showToast(`"${m.title}": ${m.revenue} ₽ выручки`, 'success');
                                }}
                            >
                                <div style={{display:'flex', gap:'10px', alignItems:'center'}}>
                                    <span style={{
                                        fontWeight:'bold',
                                        color: idx===0?'#ffd700':idx===1?'#c0c0c0':idx===2?'#cd7f32':'#666',
                                        width:'20px'
                                    }}>
                                        {idx + 1}
                                    </span>
                                    <span style={{fontWeight:'500'}}>{m.title}</span>
                                </div>
                                <div style={{textAlign:'right'}}>
                                    <div style={{color:'#2ecc71', fontWeight:'bold'}}>{m.revenue} ₽</div>
                                    <div style={{fontSize:'0.75rem', color:'#666'}}>{m.ticketsSold} бил.</div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

const StatCard = ({ title, value, color, icon, showToast }) => {
    return (
        <div
            style={{
                ...cardStyle,
                borderLeft: `4px solid ${color}`,
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                cursor: 'pointer'
            }}
            onClick={() => {
                showToast(`${title}: ${value}`, 'info');
            }}
        >
            <div>
                <div style={{ color: '#888', fontSize: '0.85rem', marginBottom: '5px', textTransform:'uppercase' }}>{title}</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'white' }}>{value}</div>
            </div>
            <div style={{ fontSize: '2rem', opacity: 0.2 }}>
                {icon}
            </div>
        </div>
    );
};

const cardStyle = {
    background: '#252525',
    padding: '20px',
    borderRadius: '12px',
    border: '1px solid #444',
    boxShadow: '0 4px 15px rgba(0,0,0,0.2)'
};

export default DashboardStats;