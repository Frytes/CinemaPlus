import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const HomePage = () => {
    const navigate = useNavigate();

    // --- Состояния (State) ---
    const [movies, setMovies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showMenu, setShowMenu] = useState(false); // Для менюшки
    const [userEmail, setUserEmail] = useState('');

    useEffect(() => {
        // 1. Читаем токен
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));
                // Берем email из subject (sub)
                setUserEmail(decoded.sub || 'User');
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
        navigate('/login');
    };

    return (
        <div style={{ width: '100%', minHeight: '100vh', backgroundColor: '#121212', color: 'white' }}>

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
                <h1 style={{ color: '#e50914', margin: 0, fontSize: '1.8rem', cursor: 'pointer' }}>CinemaPlus</h1>

                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                    <button className="nav-btn">Мои билеты</button>

                    {/* Аватарка с меню */}
                    <div className="profile-wrapper">
                        <div
                            className="avatar-circle"
                            onClick={() => setShowMenu(!showMenu)}
                        >
                            {avatarLetter}
                        </div>

                        {/* Выпадашка */}
                        {showMenu && (
                            <div className="profile-dropdown">
                                <div className="user-details">
                                    <div className="detail-row">
                                        <span className="detail-label">Имя аккаунта</span>
                                        <span className="detail-value">{usernameDisplay}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span className="detail-label">Email</span>
                                        <span className="detail-value" title={userEmail}>{userEmail}</span>
                                    </div>
                                </div>

                                <div className="menu-divider"></div>

                                <div className="menu-item">Сменить имя</div>
                                <div className="menu-item">Сменить пароль</div>
                                <div className="menu-item">Сменить Email</div>

                                <div className="menu-divider"></div>

                                <div className="menu-item logout-item" onClick={handleLogout}>
                                    Выйти
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            {/* --- КОНТЕНТ --- */}
            <div style={{ padding: '100px 50px 50px' }}>
                <h2 style={{ marginBottom: '30px', color: '#e50914' }}>Сейчас в кино</h2>

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
                                    style={{ width: '100%', borderRadius: '8px', objectFit: 'cover', aspectRatio: '2/3' }}
                                />
                                <h3 style={{ marginTop: '10px', fontSize: '1.1rem', marginBottom: '5px' }}>{movie.title}</h3>
                                <p style={{ fontSize: '0.85rem', color: '#aaa', margin: '0 0 10px 0' }}>
                                    {movie.durationMinutes} мин.
                                </p>
                                <button className="buy-btn">Купить билет</button>
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