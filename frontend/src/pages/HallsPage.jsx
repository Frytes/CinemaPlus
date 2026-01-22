import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';

const halls = [
    {
        id: 1,
        name: 'Красный зал',
        desc: 'Наш самый большой зал на 256 мест. Огромный панорамный экран IMAX, звук Dolby Atmos и максимальное погружение.',
        img: '/Красный зал.png'
    },
    {
        id: 2,
        name: 'Синий зал',
        desc: 'Самый маленький зал на 77 мест. Камерная атмосфера для ценителей комфорта. Премиальные кресла, широкие проходы и персональное обслуживание.',
        img: '/Синий зал.png'
    },
    {
        id: 3,
        name: 'Зеленый зал',
        desc: 'Классический зал на 158 мест с улучшенной акустикой. Удобные кресла с откидными спинками и подставками для еды.',
        img: '/Зеленый зал.png'
    }
];

const HallsPage = () => {
    const navigate = useNavigate();
    const [selectedImage, setSelectedImage] = useState(null);

    return (
        <div style={{ width: '100%', minHeight: '100vh', color: 'white' }}>
            <Navbar />

            {/* --- КОНТЕНТ --- */}
            <div style={{ padding: '100px 50px', maxWidth: '1200px', margin: '0 auto' }}>
                <h1 style={{
                    textAlign: 'center',
                    marginBottom: '50px',
                    color: 'white',
                    textShadow: '0 0 10px #e50914, 0 0 20px #e50914'
                }}>
                    Наши Кинозалы
                </h1>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '40px' }}>
                    {halls.map(hall => (
                        <div key={hall.id} style={{
                            background: 'rgba(30, 30, 30, 0.95)',
                            borderRadius: '16px', overflow: 'hidden', padding: '12px',
                            border: '1px solid #444', boxShadow: '0 8px 20px rgba(0,0,0,0.6)',
                            display: 'flex', flexDirection: 'column'
                        }}>
                            <div
                                style={{ overflow: 'hidden', borderRadius: '10px', cursor: 'zoom-in', height: '240px', background: '#000' }}
                                onClick={() => setSelectedImage(hall.img)}
                            >
                                <img src={hall.img} alt={hall.name} style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.3s' }}
                                     onMouseOver={e => e.currentTarget.style.transform = 'scale(1.05)'}
                                     onMouseOut={e => e.currentTarget.style.transform = 'scale(1)'}
                                />
                            </div>
                            <div style={{ padding: '15px 5px', flexGrow: 1 }}>
                                <h2 style={{ marginTop: 0, color: 'white', fontSize: '1.5rem' }}>{hall.name}</h2>
                                <p style={{ color: '#bbb', lineHeight: '1.5', fontSize: '0.95rem' }}>{hall.desc}</p>
                            </div>
                        </div>
                    ))}
                </div>

                {/*"На главную" */}
                <div style={{ textAlign: 'center', marginTop: '60px' }}>
                    <button
                        onClick={() => navigate('/')}
                        style={{
                            background: '#333',
                            border: 'none',
                            color: 'white',
                            padding: '14px 50px',
                            fontSize: '1.1rem',
                            fontWeight: 'bold',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            transition: 'all 0.3s'
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
                        На главную
                    </button>
                </div>
            </div>

            {/* Модалка */}
            {selectedImage && (
                <div className="modal-overlay" onClick={() => setSelectedImage(null)}>
                    <img src={selectedImage} className="modal-content" onClick={(e) => e.stopPropagation()} />
                </div>
            )}
        </div>
    );
};

export default HallsPage;