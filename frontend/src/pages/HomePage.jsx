import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const HomePage = () => {
    const navigate = useNavigate();
    const [showMenu, setShowMenu] = useState(false);
    const [userEmail, setUserEmail] = useState('');

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));
                setUserEmail(decoded.sub || 'User');
            } catch (e) {
                console.error("Ошибка чтения токена", e);
            }
        }
    }, []);

    const avatarLetter = userEmail ? userEmail.charAt(0).toUpperCase() : 'U';

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };


    const movies = [
        { id: 1, title: 'Inception', poster: 'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg' },
        { id: 2, title: 'Interstellar', poster: 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg' },
        { id: 3, title: 'The Dark Knight', poster: 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg' },
        { id: 4, title: 'Avengers: Endgame', poster: 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg' },
        { id: 5, title: 'Joker', poster: 'https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg' },
    ];

    return (
        <div style={{ width: '100%', minHeight: '100vh', backgroundColor: '#121212', color: 'white' }}>
            {/* Хедер */}
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

                    {/* АВАТАРКА И МЕНЮ */}
                      <div className="profile-wrapper">
                          <div
                              className="avatar-circle"
                              onClick={() => setShowMenu(!showMenu)}
                          >
                              {avatarLetter}
                          </div>


                          {showMenu && (
                              <div className="profile-dropdown">
                                  <div className="user-details">
                                      <div className="detail-row">
                                          <span className="detail-label">Имя аккаунта</span>
                                          <span className="detail-value" title={userEmail}>
                                              {userEmail.split('@')[0]}
                                          </span>
                                      </div>
                                      <div className="detail-row">
                                          <span className="detail-label">Email</span>
                                          <span className="detail-value" title={userEmail}>
                                              {userEmail}
                                          </span>
                                      </div>
                                  </div>

                                  <div className="menu-divider"></div>

                                  {/* Действия */}
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

            {/* Контент */}
            <div style={{ padding: '100px 50px 50px' }}>
                <h2 style={{ marginBottom: '30px' }}>Сейчас в кино</h2>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                    gap: '30px'
                }}>
                    {movies.map(movie => (
                        <div key={movie.id} className="movie-card">
                            <img src={movie.poster} alt={movie.title} style={{ width: '100%', borderRadius: '8px' }} />
                            <h3 style={{ marginTop: '10px', fontSize: '1.1rem' }}>{movie.title}</h3>
                            <button className="buy-btn">Купить билет</button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default HomePage;