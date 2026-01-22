import { useState } from 'react';
import api from '../api/axiosConfig';
import Navbar from '../components/Navbar';

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState('movies');

    return (
        <div style={{ width: '100%', minHeight: '100vh', color: 'white' }}>
            <Navbar />

            <div style={{ padding: '100px 50px 50px 50px', color: 'white', minHeight: '100vh' }}>
                <h1 style={{
                    textAlign: 'center',
                    marginBottom: '50px',
                    color: 'white',
                    textShadow: '0 0 10px #e50914, 0 0 20px #e50914',
                    fontSize: '2.5rem',
                    fontWeight: 'bold',
                    letterSpacing: '1px'
                }}>
                    Панель Администратора
                </h1>

                {/* Табы */}
                <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginBottom: '40px' }}>
                    {['movies', 'halls', 'sessions'].map(tab => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            style={{
                                background: activeTab === tab ? '#e50914' : '#333',
                                color: 'white',
                                padding: '12px 40px',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                transition: 'all 0.3s'
                            }}
                            onMouseOver={(e) => {
                                if (activeTab !== tab) {
                                    e.currentTarget.style.background = '#444';
                                }
                            }}
                            onMouseOut={(e) => {
                                if (activeTab !== tab) {
                                    e.currentTarget.style.background = '#333';
                                }
                            }}
                        >
                            {tab === 'movies' ? 'Фильмы' : tab === 'halls' ? 'Залы' : 'Сеансы'}
                        </button>
                    ))}
                </div>

                {/* Контент */}
                <div style={{
                    background: 'rgba(30, 30, 30, 0.95)',
                    padding: '40px',
                    borderRadius: '16px',
                    maxWidth: '900px',
                    margin: '0 auto',
                    border: '1px solid #444',
                    boxShadow: '0 8px 25px rgba(0, 0, 0, 0.5)'
                }}>
                    {activeTab === 'movies' && <AddMovieForm />}
                    {activeTab === 'halls' && <AddHallForm />}
                    {activeTab === 'sessions' && <AddSessionForm />}
                </div>
            </div>
        </div>
    );
};

// --- Форма Фильма ---
const AddMovieForm = () => {
    const [form, setForm] = useState({ title: '', durationMinutes: '', description: '', posterUrl: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/movies', form);
            alert('Фильм добавлен!');
            setForm({ title: '', durationMinutes: '', description: '', posterUrl: '' });
        } catch (err) {
            alert('Ошибка: ' + (err.response?.data?.message || err.message));
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ marginTop: 0, color: 'white' }}>Добавить фильм</h2>
            <input
                placeholder="Название фильма"
                value={form.title}
                onChange={e => setForm({...form, title: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="Длительность (минут)"
                type="number"
                value={form.durationMinutes}
                onChange={e => setForm({...form, durationMinutes: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="Ссылка на постер"
                value={form.posterUrl}
                onChange={e => setForm({...form, posterUrl: e.target.value})}
                required
                style={inputStyle}
            />
            <textarea
                placeholder="Описание фильма"
                value={form.description}
                onChange={e => setForm({...form, description: e.target.value})}
                style={{...inputStyle, height: '120px', resize: 'vertical'}}
            />
            <button type="submit" style={submitButtonStyle}>Сохранить фильм</button>
        </form>
    );
};

// --- Форма Зала ---
const AddHallForm = () => {
    const [form, setForm] = useState({ name: '', width: '', height: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/halls', form);
            alert('Зал создан!');
            setForm({ name: '', width: '', height: '' });
        } catch (err) {
            alert('Ошибка: ' + err.message);
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ marginTop: 0, color: 'white' }}>Создать зал</h2>
            <input
                placeholder="Название зала"
                value={form.name}
                onChange={e => setForm({...form, name: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="Количество мест в ряду"
                type="number"
                value={form.width}
                onChange={e => setForm({...form, width: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="Количество рядов"
                type="number"
                value={form.height}
                onChange={e => setForm({...form, height: e.target.value})}
                required
                style={inputStyle}
            />
            <button type="submit" style={submitButtonStyle}>Создать зал</button>
        </form>
    );
};

// --- Форма Сеанса ---
const AddSessionForm = () => {
    const [form, setForm] = useState({ movieId: '', hallId: '', startTime: '', basePrice: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/sessions', form);
            alert('Сеанс создан!');
        } catch (err) {
            alert('Ошибка: ' + err.message);
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ marginTop: 0, color: 'white' }}>Добавить сеанс</h2>
            <input
                placeholder="ID Фильма"
                type="number"
                value={form.movieId}
                onChange={e => setForm({...form, movieId: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="ID Зала"
                type="number"
                value={form.hallId}
                onChange={e => setForm({...form, hallId: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="Дата и время"
                type="datetime-local"
                value={form.startTime}
                onChange={e => setForm({...form, startTime: e.target.value})}
                required
                style={inputStyle}
            />
            <input
                placeholder="Базовая цена (руб)"
                type="number"
                value={form.basePrice}
                onChange={e => setForm({...form, basePrice: e.target.value})}
                required
                style={inputStyle}
            />
            <button type="submit" style={submitButtonStyle}>Создать сеанс</button>
        </form>
    );
};

// Стили
const inputStyle = {
    padding: '14px',
    background: '#333',
    border: '1px solid #555',
    color: 'white',
    borderRadius: '8px',
    fontSize: '1rem',
    outline: 'none',
    transition: 'border-color 0.3s',
};

const submitButtonStyle = {
    padding: '14px',
    background: '#e50914',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '1.1rem',
    fontWeight: 'bold',
    cursor: 'pointer',
    transition: 'all 0.3s',
    marginTop: '10px'
};


Object.assign(inputStyle, {
    ':focus': {
        borderColor: '#e50914',
        boxShadow: '0 0 0 2px rgba(229, 9, 20, 0.2)'
    }
});

Object.assign(submitButtonStyle, {
    ':hover': {
        background: '#f40612',
        transform: 'translateY(-2px)',
        boxShadow: '0 4px 12px rgba(229, 9, 20, 0.4)'
    }
});

export default AdminPage;