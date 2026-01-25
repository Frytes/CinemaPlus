import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import Navbar from '../components/Navbar'; // <--- Импорт

const HomePage = () => {
    const navigate = useNavigate();
    const [movies, setMovies] = useState([]);
    const [loading, setLoading] = useState(true);


    useEffect(() => {
        const fetchMovies = async () => {
            try {
                const response = await api.get('/movies');
                setMovies(response.data);
            } catch (error) {
                console.error("Ошибка:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchMovies();
    }, []);

    // handleLogout - УДАЛЯЕМ

    // Проверка для кнопки "Купить билет"
    const handleBuyClick = (movieId) => {
        if (!localStorage.getItem('accessToken')) {
            navigate('/login');
        } else {
            navigate(`/movie/${movieId}`);
        }
    };

    return (
        <div style={{ width: '100%', minHeight: '100vh', color: 'white' }}>

             <Navbar />

            {/* Контент с отступом под навбар */}
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
                            <div key={movie.id} className="movie-card">
                                <img
                                    src={movie.posterUrl || 'https://via.placeholder.com/300x450'}
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
                                    onClick={() => handleBuyClick(movie.id)}
                                >
                                    Купить билет
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default HomePage;