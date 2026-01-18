import { useState } from 'react';
import api from '../api/axiosConfig';

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState('movies');

    return (
        <div style={{ padding: '50px', color: 'white', minHeight: '100vh' }}>
            <h1 style={{ color: '#e50914', textAlign: 'center' }}>Панель Администратора</h1>

            {/* Табы */}
            <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginBottom: '40px' }}>
                {['movies', 'halls', 'sessions'].map(tab => (
                    <button
                        key={tab}
                        onClick={() => setActiveTab(tab)}
                        style={{
                            background: activeTab === tab ? '#e50914' : '#333',
                            color: 'white', padding: '10px 30px', border: 'none', borderRadius: '8px', cursor: 'pointer'
                        }}
                    >
                        {tab === 'movies' ? 'Фильмы' : tab === 'halls' ? 'Залы' : 'Сеансы'}
                    </button>
                ))}
            </div>

            {/* Контент */}
            <div style={{ background: '#1e1e1e', padding: '30px', borderRadius: '12px', maxWidth: '800px', margin: '0 auto' }}>
                {activeTab === 'movies' && <AddMovieForm />}
                {activeTab === 'halls' && <AddHallForm />}
                {activeTab === 'sessions' && <AddSessionForm />}
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
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <h2>Добавить фильм</h2>
            <input placeholder="Название" value={form.title} onChange={e => setForm({...form, title: e.target.value})} required style={inputStyle} />
            <input placeholder="Длительность (мин)" type="number" value={form.durationMinutes} onChange={e => setForm({...form, durationMinutes: e.target.value})} required style={inputStyle} />
            <input placeholder="Ссылка на постер" value={form.posterUrl} onChange={e => setForm({...form, posterUrl: e.target.value})} required style={inputStyle} />
            <textarea placeholder="Описание" value={form.description} onChange={e => setForm({...form, description: e.target.value})} style={{...inputStyle, height: '100px'}} />
            <button type="submit" className="buy-btn">Сохранить</button>
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
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <h2>Создать зал</h2>
            <input placeholder="Название зала" value={form.name} onChange={e => setForm({...form, name: e.target.value})} required style={inputStyle} />
            <input placeholder="Ширина (мест)" type="number" value={form.width} onChange={e => setForm({...form, width: e.target.value})} required style={inputStyle} />
            <input placeholder="Высота (рядов)" type="number" value={form.height} onChange={e => setForm({...form, height: e.target.value})} required style={inputStyle} />
            <button type="submit" className="buy-btn">Создать</button>
        </form>
    );
};

// --- Форма Сеанса ---
const AddSessionForm = () => {
    const [form, setForm] = useState({ movieId: '', hallId: '', startTime: '', basePrice: '' });
    // Тут по-хорошему надо подгружать списки фильмов и залов для <select>, но пока ID вручную

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
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <h2>Добавить сеанс</h2>
            <input placeholder="ID Фильма" type="number" value={form.movieId} onChange={e => setForm({...form, movieId: e.target.value})} required style={inputStyle} />
            <input placeholder="ID Зала" type="number" value={form.hallId} onChange={e => setForm({...form, hallId: e.target.value})} required style={inputStyle} />
            <input placeholder="Дата и время" type="datetime-local" value={form.startTime} onChange={e => setForm({...form, startTime: e.target.value})} required style={inputStyle} />
            <input placeholder="Цена (руб)" type="number" value={form.basePrice} onChange={e => setForm({...form, basePrice: e.target.value})} required style={inputStyle} />
            <button type="submit" className="buy-btn">Сохранить</button>
        </form>
    );
};

const inputStyle = {
    padding: '10px',
    background: '#333',
    border: '1px solid #555',
    color: 'white',
    borderRadius: '4px'
};

export default AdminPage;