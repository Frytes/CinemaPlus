import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

const MovieManager = ({ showToast }) => {
    const [movies, setMovies] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [editingId, setEditingId] = useState(null);

    const initialFormState = {
        title: '',
        durationMinutes: '',
        description: '',
        posterUrl: '',
        releaseYear: new Date().getFullYear(),
        rating: '',
        ageLimit: '12',
        genre: ''
    };

    const [form, setForm] = useState(initialFormState);


    const loadMovies = async () => {
        try {
            const res = await api.get('/movies');
            setMovies(res.data.sort((a, b) => b.id - a.id));
        } catch (err) {
            console.error(err);
        }
    };

    useEffect(() => {
        loadMovies();
    }, []);


    const handleEdit = (movie) => {
        setEditingId(movie.id);
        setForm({
            title: movie.title,
            durationMinutes: movie.durationMinutes,
            description: movie.description || '',
            posterUrl: movie.posterUrl,
            releaseYear: movie.releaseYear || '',
            rating: movie.rating || '',
            ageLimit: movie.ageLimit || 0,
            genre: movie.genre || ''
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleCancelEdit = () => {
        setEditingId(null);
        setForm(initialFormState);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Удалить фильм?')) return;
        try {
            await api.delete(`/movies/${id}`);
            showToast('🗑️ Фильм удален', 'success');
            loadMovies();
            if (editingId === id) handleCancelEdit();
        } catch (err) {
            showToast('Ошибка: ' + err.message, 'error');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingId) {
                await api.put(`/movies/${editingId}`, form);
                showToast('✏️ Фильм обновлен!', 'success');
            } else {
                await api.post('/movies', form);
                showToast('✅ Фильм добавлен!', 'success');
            }
            loadMovies();
            handleCancelEdit();
        } catch (err) {
            showToast(err.response?.data?.message || 'Ошибка', 'error');
        }
    };

    const filteredMovies = movies.filter(m =>
        m.title.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div style={{display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px', alignItems: 'start'}}>

            {/* СПИСОК (ЛЕВАЯ КОЛОНКА) */}
            <div style={{display:'flex', flexDirection:'column', gap:'20px'}}>
                <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                    <h2 style={{margin:0}}>🎬 Список ({movies.length})</h2>
                    <input
                        placeholder="🔍 Поиск..."
                        style={{...inputStyle, width:'180px', padding:'8px'}}
                        value={searchTerm}
                        onChange={e => setSearchTerm(e.target.value)}
                    />
                </div>

                <div style={{
                    display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(130px, 1fr))',
                    gap:'15px', maxHeight:'80vh', overflowY:'auto', paddingRight:'5px'
                }}>
                    {filteredMovies.map(movie => (
                        <div key={movie.id}
                             onClick={() => handleEdit(movie)}
                             style={{
                                 background: editingId === movie.id ? '#333' : '#1a1a1a',
                                 border: editingId === movie.id ? '2px solid #e50914' : '1px solid #333',
                                 borderRadius:'8px', overflow:'hidden', cursor:'pointer', position:'relative',
                                 transition:'transform 0.2s'
                             }}
                             onMouseOver={e => e.currentTarget.style.transform = 'translateY(-3px)'}
                             onMouseOut={e => e.currentTarget.style.transform = 'translateY(0)'}
                        >
                            <img src={movie.posterUrl} alt="" style={{width:'100%', aspectRatio:'2/3', objectFit:'cover'}} />
                            <div style={{padding:'8px'}}>
                                <div style={{fontWeight:'bold', fontSize:'0.85rem', whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{movie.title}</div>
                                <div style={{fontSize:'0.75rem', color:'#888'}}>{movie.releaseYear}</div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* ФОРМА (ПРАВАЯ КОЛОНКА) */}
            <div style={{background:'#252525', padding:'25px', borderRadius:'12px', position:'sticky', top:'20px'}}>
                <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'20px', borderBottom:'1px solid #444', paddingBottom:'15px'}}>
                    <h3 style={{margin:0, color: editingId ? '#e50914' : 'white'}}>
                        {editingId ? `✏️ Редактирование #${editingId}` : '➕ Новый фильм'}
                    </h3>
                    {editingId && (
                        <div style={{display:'flex', gap:'10px'}}>
                            <button type="button" onClick={() => handleDelete(editingId)} style={{background:'transparent', border:'1px solid #e74c3c', color:'#e74c3c', padding:'5px 10px', borderRadius:'4px', cursor:'pointer'}}>Удалить</button>
                            <button type="button" onClick={handleCancelEdit} style={{background:'#444', border:'none', color:'white', padding:'5px 10px', borderRadius:'4px', cursor:'pointer'}}>Отмена</button>
                        </div>
                    )}
                </div>

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <InputGroup label="Название">
                        <input value={form.title} onChange={e => setForm({...form, title: e.target.value})} required style={inputStyle} />
                    </InputGroup>

                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                        <InputGroup label="Длительность (мин)">
                            <input type="number" value={form.durationMinutes} onChange={e => setForm({...form, durationMinutes: e.target.value})} required style={inputStyle} />
                        </InputGroup>
                        <InputGroup label="Год выхода">
                            <input type="number" value={form.releaseYear} onChange={e => setForm({...form, releaseYear: e.target.value})} style={inputStyle} />
                        </InputGroup>
                    </div>

                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                        <InputGroup label="Рейтинг (0-10)">
                            <input type="number" step="0.1" value={form.rating} onChange={e => setForm({...form, rating: e.target.value})} style={inputStyle} />
                        </InputGroup>
                        <InputGroup label="Возраст">
                            <select value={form.ageLimit} onChange={e => setForm({...form, ageLimit: e.target.value})} style={inputStyle}>
                                <option value="0">0+</option>
                                <option value="6">6+</option>
                                <option value="12">12+</option>
                                <option value="16">16+</option>
                                <option value="18">18+</option>
                            </select>
                        </InputGroup>
                    </div>

                    <InputGroup label="Жанр">
                        <input placeholder="Боевик, Драма" value={form.genre} onChange={e => setForm({...form, genre: e.target.value})} style={inputStyle} />
                    </InputGroup>

                    <InputGroup label="Постер (URL)">
                        <div style={{display:'flex', gap:'10px', alignItems:'center'}}>
                            <input value={form.posterUrl} onChange={e => setForm({...form, posterUrl: e.target.value})} required style={inputStyle} />
                            {form.posterUrl && <img src={form.posterUrl} alt="preview" style={{width:'30px', height:'45px', objectFit:'cover', borderRadius:'2px', border:'1px solid #555'}} />}
                        </div>
                    </InputGroup>

                    <InputGroup label="Описание">
                        <textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})} style={{...inputStyle, height: '80px', resize: 'vertical'}} />
                    </InputGroup>

                    <button type="submit" style={{...submitButtonStyle, background: editingId ? '#3498db' : '#e50914'}}>
                        {editingId ? 'Сохранить изменения' : 'Добавить фильм'}
                    </button>
                </form>
            </div>
        </div>
    );
};

const InputGroup = ({label, children}) => (
    <div style={{display:'flex', flexDirection:'column', gap:'5px'}}>
        <label style={{fontSize:'0.8rem', color:'#aaa', fontWeight:'500'}}>{label}</label>
        {children}
    </div>
);

const inputStyle = {
    padding: '10px', background: '#333', border: '1px solid #444',
    color: 'white', borderRadius: '6px', width: '100%', fontSize:'0.95rem', outline:'none'
};

const submitButtonStyle = {
    padding: '14px', color: 'white', border: 'none',
    borderRadius: '6px', fontSize: '1rem', fontWeight: 'bold', cursor: 'pointer',
    marginTop: '10px', textTransform:'uppercase', letterSpacing:'1px', width: '100%',
    transition: 'all 0.2s'
};

export default MovieManager;