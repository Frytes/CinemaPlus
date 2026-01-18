import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const MoviePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(null);
  const [groupedSessions, setGroupedSessions] = useState({});
  const [sortedDates, setSortedDates] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [loading, setLoading] = useState(true);

  // --- Логика Хедера ---
  const [showMenu, setShowMenu] = useState(false);
  const [userEmail, setUserEmail] = useState('');

  useEffect(() => {
    // Токен
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = token.split('.')[1];
        const decoded = JSON.parse(atob(payload));
        setUserEmail(decoded.sub || 'User');
      } catch (e) {}
    }

    // Данные
    const fetchData = async () => {
      try {
        const movieRes = await api.get(`/movies/${id}`);
        setMovie(movieRes.data);

        const sessionsRes = await api.get(`/sessions/movie/${id}`);
        const allSessions = sessionsRes.data;

        const groups = {};
        allSessions.forEach(s => {
          const date = s.startTime.split('T')[0];
          if (!groups[date]) groups[date] = [];
          groups[date].push(s);
        });

        for (let date in groups) {
          groups[date].sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
        }

        setGroupedSessions(groups);
        const dates = Object.keys(groups).sort();
        setSortedDates(dates);
        if (dates.length > 0) setSelectedDate(dates[0]);

      } catch (error) {
        console.error("Error:", error);
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchData();
  }, [id]);

  const avatarLetter = userEmail ? userEmail.charAt(0).toUpperCase() : 'U';
  const usernameDisplay = userEmail ? userEmail.split('@')[0] : 'Guest';

  const handleLogout = () => {
    localStorage.removeItem('token');
    setUserEmail('');
    setShowMenu(false);
    navigate('/login');
  };

  const formatDate = (isoDate) => {
    const date = new Date(isoDate);
    return date.toLocaleDateString('ru-RU', { weekday: 'short', day: 'numeric', month: 'long' });
  };

  if (loading) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Загрузка...</div>;
  if (!movie) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Фильм не найден</div>;

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
          <button className="nav-btn" onClick={() => navigate('/halls')}>Залы</button>
          {userEmail && <button className="nav-btn">Мои билеты</button>}

          {!userEmail ? (
            <button
              className="nav-btn logout"
              onClick={() => navigate('/login')}
              style={{ fontWeight: 'bold' }}
            >
              Войти
            </button>
          ) : (
            /* Аватарка с меню (для залогиненных) */
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
          )}

        </div>
      </header>

      {/* --- КОНТЕНТ --- */}
      <div style={{ padding: '100px 50px', maxWidth: '1200px', margin: '0 auto' }}>

        <div style={{ marginTop: '60px', textAlign: 'left' }}>
          <button
            onClick={() => navigate('/')}
            style={{
              marginBottom: '20px',
              padding: '10px 20px',
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
            &larr; Назад к афише
          </button>
        </div>

        <div style={{
          background: 'rgba(30, 30, 30, 0.85)',
          backdropFilter: 'blur(20px)',
          borderRadius: '20px',
          padding: '40px',
          boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
          display: 'flex', gap: '50px', alignItems: 'flex-start', flexWrap: 'wrap'
        }}>
          <img
            src={movie.posterUrl}
            alt={movie.title}
            style={{
              width: '350px', borderRadius: '12px',
              boxShadow: '0 0 20px rgba(0,0,0,0.5)', objectFit: 'cover',
              aspectRatio: '2/3'
            }}
          />

          <div style={{ flex: 1, minWidth: '300px' }}>
            <h1 style={{ fontSize: '3.5rem', margin: '0 0 10px 0', color: '#e50914', lineHeight: 1.1 }}>
              {movie.title}
            </h1>

            <div style={{ display: 'flex', gap: '20px', color: '#aaa', marginBottom: '20px', fontSize: '1.1rem', alignItems: 'center' }}>
              <span style={{ color: '#ffd700', fontWeight: 'bold' }}>⭐ {movie.rating} (IMDb)</span>
              <span>⏱ {movie.durationMinutes} мин.</span>
              <span>📅 {movie.releaseYear}</span>
              <span style={{ border: '1px solid #777', padding: '2px 8px', borderRadius: '4px', fontSize: '0.9rem' }}>
                {movie.ageLimit}+
              </span>
            </div>

            <p style={{ color: '#bbb', fontStyle: 'italic', marginBottom: '20px' }}>
              {movie.genre}
            </p>

            <p style={{ fontSize: '1.2rem', lineHeight: '1.6', color: '#ddd' }}>
              {movie.description}
            </p>

            <hr style={{ borderColor: 'rgba(255,255,255,0.1)', margin: '30px 0' }} />

            {/* РАСПИСАНИЕ */}
            <div>
              <h3 style={{ marginBottom: '15px' }}>Расписание сеансов:</h3>

              {sortedDates.length === 0 ? (
                <p style={{ color: '#777' }}>На ближайшее время сеансов нет.</p>
              ) : (
                <>
                  {/* Даты (Табы) */}
                  <div style={{
                    display: 'flex', gap: '10px', marginBottom: '20px',
                    overflowX: 'auto', paddingBottom: '5px'
                  }}>
                    {sortedDates.map(dateKey => (
                      <button
                        key={dateKey}
                        onClick={() => setSelectedDate(dateKey)}
                        style={{
                          background: selectedDate === dateKey ? '#e50914' : 'rgba(255,255,255,0.05)',
                          color: selectedDate === dateKey ? 'white' : '#aaa',
                          border: selectedDate === dateKey ? 'none' : '1px solid #444',
                          padding: '10px 20px',
                          borderRadius: '30px',
                          cursor: 'pointer',
                          whiteSpace: 'nowrap',
                          transition: 'all 0.2s',
                          fontSize: '0.95rem'
                        }}
                      >
                        {formatDate(dateKey)}
                      </button>
                    ))}
                  </div>

                  {/* Сеансы (Grid сетка) */}
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))',
                    gap: '15px'
                  }}>
                    {selectedDate && groupedSessions[selectedDate].map(session => {
                      const timeStr = new Date(session.startTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                      return (
                        <button
                          key={session.id}
                          className="time-btn"
                          onClick={() => navigate(`/session/${session.id}`)}
                          title={`Зал: ${session.hallName}`}
                          style={{ width: '100%', minHeight: '70px' }}
                        >
                          <div style={{fontSize: '1.2rem', fontWeight: 'bold'}}>{timeStr}</div>
                          <div style={{fontSize: '0.75rem', color: '#aaa', marginTop: '4px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'}}>
                            {session.hallName}
                          </div>
                          <div style={{fontSize: '0.9rem', color: '#ffd700', marginTop: '2px'}}>{session.price} ₽</div>
                        </button>
                      );
                    })}
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MoviePage;