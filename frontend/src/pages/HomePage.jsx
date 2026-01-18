import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const HomePage = () => {
    const navigate = useNavigate();

    // --- Состояния ---
    const [movies, setMovies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showMenu, setShowMenu] = useState(false);
    const [userEmail, setUserEmail] = useState('');
    const [isAdmin, setIsAdmin] = useState(false); // Флаг админа

    useEffect(() => {
        // 1. Читаем токен
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));

                setUserEmail(decoded.sub || 'User');

                // Если в токене есть роль ADMIN - показываем кнопку
                if (decoded.role === 'ADMIN') {
                    setIsAdmin(true);
                }
            } catch (e) {
                console.error("Ошибка чтения токена", e);
            }
        }

        // 2. Загружаем фильмы
        const fetchMovies = async () => {
            try {
                const response = await api.get('/movies');
                setMovies(response.data);
            } catch (error) {
                console.error("Ошибка при загрузке фильмов:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchMovies();
    }, []);

    const avatarLetter = userEmail ? userEmail.charAt(0).toUpperCase() : 'U';
    const usernameDisplay = userEmail ? userEmail.split('@')[0] : 'Guest';

    const handleLogout = () => {
        localStorage.removeItem('token');
        setUserEmail('');
        setIsAdmin(false);
        setShowMenu(false);
        navigate('/login');
    };

    return (
        <div style={{ width: '100%', minHeight: '100vh', color: 'white' }}>

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

                      {isAdmin && (
                                            <button
                                                className="nav-btn"
                                                style={{
                                                    borderColor: '#e50914',
                                                    color: '#e50914',
                                                    fontWeight: 'bold'
                                                }}
                                                onClick={() => navigate('/admin')}
                                            >
                                                АДМИНКА
                                            </button>
                                        )}

                    <button className="nav-btn" onClick={() => navigate('/halls')}>Залы</button>

                    {userEmail && <button className="nav-btn">Мои билеты</button>}

                    {!userEmail ? (
                        <button className="nav-btn logout" onClick={() => navigate('/login')} style={{ fontWeight: 'bold' }}>
                            Войти
                        </button>
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
            <div style={{ padding: '100px 50px 50px' }}>
                <h2 style={{ marginBottom: '30px', color: 'white', textAlign: 'center', fontSize: '2.5rem', textShadow: '0 0 10px #e50914, 0 0 20px #e50914' }}>
                    Сейчас в кино
                </h2>

                {loading ? (
                    <div style={{textAlign: 'center', marginTop: '50px'}}>Загрузка...</div>
                ) : (
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                        gap: '30px'
                    }}>
                        {movies.map(movie => (
                            <div key={movie.title} className="movie-card">
                                <img
                                    src={movie.posterUrl || 'https://via.placeholder.com/300x450?text=No+Poster'}
                                    alt={movie.title}
                                />
                                <h3 style={{ marginTop: '10px', fontSize: '1.1rem', marginBottom: '5px' }}>{movie.title}</h3>

                                <div style={{ flexGrow: 1 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', color: '#e5e5e5', marginBottom: '5px' }}>
                                        <span style={{ color: '#ffd700', fontWeight: 'bold' }}>⭐ {movie.rating}</span>
                                        <span>{movie.releaseYear}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#aaa' }}>
                                        <span>{movie.durationMinutes} мин.</span>
                                        <span style={{ border: '1px solid #555', padding: '0 4px', borderRadius: '4px' }}>
                                            {movie.ageLimit}+
                                        </span>
                                    </div>
                                    <div style={{ fontSize: '0.8rem', color: '#888', marginTop: '5px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {movie.genre}
                                    </div>
                                </div>

                                <button
                                    className="buy-btn"
                                    onClick={() => {
                                        if (!userEmail) {
                                            navigate('/login');
                                        } else {
                                            navigate(`/movie/${movie.id}`);
                                        }
                                    }}
                                >
                                    Купить билет
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {!loading && movies.length === 0 && (
                    <div style={{textAlign: 'center', color: '#777'}}>Фильмов пока нет 😔</div>
                )}
            </div>
        </div>
    );
};

export default HomePage;