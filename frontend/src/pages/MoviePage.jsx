import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import Navbar from '../components/Navbar'; // <--- Импорт

const MoviePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(null);
  const [groupedSessions, setGroupedSessions] = useState({});
  const [sortedDates, setSortedDates] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [loading, setLoading] = useState(true);


  useEffect(() => {
    const fetchData = async () => {
      try {
        const movieRes = await api.get(`/movies/${id}`);
        setMovie(movieRes.data);

        const sessionsRes = await api.get(`/sessions/movie/${id}`);
        const allSessions = sessionsRes.data;

        // Группировка сеансов
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

  const formatDate = (isoDate) => {
    const date = new Date(isoDate);
    return date.toLocaleDateString('ru-RU', { weekday: 'short', day: 'numeric', month: 'long' });
  };

  if (loading) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Загрузка...</div>;
  if (!movie) return <div style={{color:'white', textAlign:'center', marginTop:'100px'}}>Фильм не найден</div>;

  return (
    <div style={{ width: '100%', minHeight: '100vh', color: 'white' }}>

      <Navbar />

      <div style={{ padding: '100px 50px', maxWidth: '1200px', margin: '0 auto' }}>


        <div style={{
          background: 'rgba(30, 30, 30, 0.85)',
          backdropFilter: 'blur(20px)',
          borderRadius: '20px',
          padding: '40px',
          boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
          display: 'flex', gap: '50px', alignItems: 'flex-start', flexWrap: 'wrap',
          marginTop: '20px'
        }}>

          <img src={movie.posterUrl} alt={movie.title} style={{ width: '350px', borderRadius: '12px', boxShadow: '0 0 20px rgba(0,0,0,0.5)', objectFit: 'cover', aspectRatio: '2/3' }} />

          <div style={{ flex: 1, minWidth: '300px' }}>
            <h1 style={{ fontSize: '3.5rem', margin: '0 0 10px 0', color: '#e50914', lineHeight: 1.1 }}>{movie.title}</h1>

            <div style={{ display: 'flex', gap: '20px', color: '#aaa', marginBottom: '20px', fontSize: '1.1rem', alignItems: 'center' }}>
              <span style={{ color: '#ffd700', fontWeight: 'bold' }}>⭐ {movie.rating}</span>
              <span>⏱ {movie.durationMinutes} мин.</span>
              <span>📅 {movie.releaseYear}</span>
              <span style={{ border: '1px solid #777', padding: '2px 8px', borderRadius: '4px', fontSize: '0.9rem' }}>{movie.ageLimit}+</span>
            </div>

            <p style={{ color: '#bbb', fontStyle: 'italic', marginBottom: '20px' }}>{movie.genre}</p>
            <p style={{ fontSize: '1.2rem', lineHeight: '1.6', color: '#ddd' }}>{movie.description}</p>

            <hr style={{ borderColor: 'rgba(255,255,255,0.1)', margin: '30px 0' }} />

            {/* РАСПИСАНИЕ */}
            <div>
              <h3 style={{ marginBottom: '15px' }}>Расписание сеансов:</h3>
              {sortedDates.length === 0 ? (
                <p style={{ color: '#777' }}>На ближайшее время сеансов нет.</p>
              ) : (
                <>
                  <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', overflowX: 'auto', paddingBottom: '5px' }}>
                    {sortedDates.map(dateKey => (
                      <button key={dateKey} onClick={() => setSelectedDate(dateKey)}
                        style={{
                          background: selectedDate === dateKey ? '#e50914' : 'rgba(255,255,255,0.05)',
                          color: selectedDate === dateKey ? 'white' : '#aaa',
                          border: selectedDate === dateKey ? 'none' : '1px solid #444',
                          padding: '10px 20px', borderRadius: '30px', cursor: 'pointer', whiteSpace: 'nowrap', transition: 'all 0.2s', fontSize: '0.95rem'
                        }}
                      >
                        {formatDate(dateKey)}
                      </button>
                    ))}
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: '15px' }}>
                    {selectedDate && groupedSessions[selectedDate].map(session => {
                      const timeStr = new Date(session.startTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                      return (
                        <button key={session.id} className="time-btn" onClick={() => navigate(`/session/${session.id}`)}
                          style={{ width: '100%', minHeight: '70px' }}
                        >
                          <div style={{fontSize: '1.2rem', fontWeight: 'bold'}}>{timeStr}</div>
                          <div style={{fontSize: '0.75rem', color: '#aaa', marginTop: '4px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'}}>{session.hallName}</div>
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