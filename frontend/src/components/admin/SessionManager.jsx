import { useState, useEffect, useMemo, useCallback } from 'react';
import api from '../../api/axiosConfig';
import Timeline from './Timeline';
import TimePicker from './TimePicker';
import CalendarPicker from './CalendarPicker';

const SessionManager = ({ showToast }) => {
    // --- ДАННЫЕ ---
    const [movies, setMovies] = useState([]);
    const [halls, setHalls] = useState([]);
    const [existingSessions, setExistingSessions] = useState([]);

    // --- ФИЛЬТРЫ ---
    const [hallId, setHallId] = useState('');
    const [dateStr, setDateStr] = useState(new Date().toLocaleDateString('en-CA'));

    // --- СОЗДАНИЕ ---
    const [selectedMovie, setSelectedMovie] = useState(null);
    const [newSession, setNewSession] = useState({
        movieId: '',
        time: '10:00',
        basePrice: '300',
        adsMinutes: '15',
        cleanupMinutes: '20'
    });

    // --- РЕДАКТИРОВАНИЕ (МОДАЛКА) ---
    const [editingSession, setEditingSession] = useState(null);
    const [showConfirmDelete, setShowConfirmDelete] = useState(false); // Для окна подтверждения удаления


    useEffect(() => {
        Promise.all([api.get('/movies'), api.get('/halls')]).then(([mRes, hRes]) => {
            setMovies(mRes.data);
            setHalls(hRes.data);
            if (hRes.data.length > 0) setHallId(hRes.data[0].id);
        }).catch(console.error);
    }, []);


    const loadSessions = useCallback(async () => {
        if (!hallId || !dateStr) return;
        try {
            const todayP = api.get(`/sessions?date=${dateStr}`);
            const tomorrow = new Date(dateStr);
            tomorrow.setDate(tomorrow.getDate() + 1);
            const tmrStr = tomorrow.toLocaleDateString('en-CA');
            const tomorrowP = api.get(`/sessions?date=${tmrStr}`);

            const [res1, res2] = await Promise.all([todayP, tomorrowP]);
            const combined = [...res1.data, ...res2.data];
            setExistingSessions(combined.filter(s => s.hallId.toString() === hallId.toString()));
        } catch (e) { console.error(e); }
    }, [hallId, dateStr]);

    useEffect(() => { loadSessions(); }, [loadSessions]);

    // --- ОБРАБОТЧИКИ СОЗДАНИЯ ---
    const handleMovieSelect = (e) => {
        const mId = e.target.value;
        const movie = movies.find(m => m.id.toString() === mId);
        setNewSession({...newSession, movieId: mId});
        setSelectedMovie(movie);
    };

    const handleTimeChange = (t) => setNewSession(p => ({...p, time: t}));

    const previewRange = useMemo(() => {
        if (!newSession.time) return null;
        let start = new Date(`${dateStr}T${newSession.time}`);
        const hours = parseInt(newSession.time.split(':')[0]);
        if (hours < 6) start.setDate(start.getDate() + 1);

        const duration = selectedMovie ? selectedMovie.durationMinutes : 0;
        const total = duration + parseInt(newSession.adsMinutes || 0) + parseInt(newSession.cleanupMinutes || 0);
        const end = new Date(start.getTime() + total * 60000);
        return { start, end };
    }, [dateStr, newSession, selectedMovie]);

    const hasOverlap = useMemo(() => {
        if (!previewRange) return false;
        const nS = previewRange.start.getTime();
        const nE = previewRange.end.getTime();
        return existingSessions.some(s => {
            const sS = new Date(s.startTime).getTime();
            const sE = new Date(s.endTime).getTime();
            return (nS < sE) && (nE > sS);
        });
    }, [previewRange, existingSessions]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (hasOverlap) return showToast('Наложение сеансов!', 'error');
        if (!selectedMovie) return showToast('Выберите фильм!', 'error');

        try {
            const d = previewRange.start;
            const pad = (n) => String(n).padStart(2, '0');
            const startStr = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;

            await api.post('/sessions', {
                movieId: newSession.movieId,
                hallId,
                startTime: startStr,
                basePrice: newSession.basePrice,
                adsMinutes: newSession.adsMinutes,
                cleanupMinutes: newSession.cleanupMinutes
            });
            showToast('Сеанс создан!', 'success');
            loadSessions();
        } catch (err) {
            showToast(err.response?.data?.message || 'Ошибка создания', 'error');
        }
    };

    // --- ОБРАБОТЧИКИ РЕДАКТИРОВАНИЯ ---

    const handleSessionClick = (session) => {
        setEditingSession(session);
    };

    const handleUpdateSession = async (updatedData) => {
        try {
            await api.put(`/sessions/${editingSession.id}`, updatedData);
            showToast('Сеанс обновлен', 'success');
            setEditingSession(null);
            loadSessions();
        } catch (err) {
            showToast(err.response?.data?.message || 'Ошибка обновления', 'error');
        }
    };

    const handleDeleteSession = async () => {
        try {
            await api.delete(`/sessions/${editingSession.id}`);
            showToast('Сеанс удален', 'success');
            setEditingSession(null);
            setShowConfirmDelete(false);
            loadSessions();
        } catch (err) {
            const msg = err.response?.data?.message || 'Ошибка удаления';
            if (msg.includes('билет')) showToast('Нельзя удалить: уже куплены билеты!', 'error');
            else showToast(msg, 'error');
            setShowConfirmDelete(false);
        }
    };

    const handleDeleteClick = () => {
        setShowConfirmDelete(true);
    };

    const handleCancelDelete = () => {
        setShowConfirmDelete(false);
    };

    return (
        <div style={{display:'flex', flexDirection:'column', gap:'25px'}}>
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', borderBottom:'1px solid #333', paddingBottom:'15px'}}>
                <h2 style={{margin:0}}>📅 Планирование</h2>
            </div>

            {/* Фильтры */}
            <div style={{background:'#252525', padding:'20px', borderRadius:'12px', display:'flex', flexDirection:'column', gap:'15px'}}>
                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'20px'}}>
                    <InputGroup label="1. Выберите Зал">
                        <select style={inputStyle} value={hallId} onChange={e => setHallId(e.target.value)}>
                            {halls.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                        </select>
                    </InputGroup>
                    <InputGroup label="2. Выберите Дату">
                        <CalendarPicker
                            value={dateStr}
                            onChange={setDateStr}
                            label=""
                            minDate={new Date().toISOString().split('T')[0]}
                        />
                    </InputGroup>
                </div>
                <DateSelector selectedDateStr={dateStr} onSelect={setDateStr} />
            </div>

            {/* Таймлайн */}
            <Timeline
                selectedDateStr={dateStr}
                sessions={existingSessions}
                previewSession={previewRange}
                isOverlap={hasOverlap}
                onTimeChange={handleTimeChange}
                disabled={!selectedMovie}
                onDragAttemptWithoutMovie={() => showToast('Сначала выберите фильм!', 'error')}
                onSessionClick={handleSessionClick}
            />

            {/* Форма создания (внизу) */}
            <form onSubmit={handleSubmit} style={{background:'#252525', padding:'25px', borderRadius:'12px', display:'flex', flexDirection:'column', gap:'20px'}}>
               <h3 style={{margin:0, color:'#aaa'}}>Создание нового сеанса</h3>
               <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'20px'}}>
                    <InputGroup label="Фильм">
                        <select style={inputStyle} value={newSession.movieId} onChange={handleMovieSelect} required>
                            <option value="">-- Выберите фильм --</option>
                            {movies.map(m => <option key={m.id} value={m.id}>{m.title} ({m.durationMinutes} мин)</option>)}
                        </select>
                    </InputGroup>
                    <InputGroup label="Время начала">
                        <TimePicker
                            value={newSession.time}
                            onChange={(time) => setNewSession({...newSession, time})}
                            label=""
                            interval={15}
                            minTime="09:00"
                            maxTime="23:00"
                        />
                    </InputGroup>
                </div>
                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'20px'}}>
                    <InputGroup label="Реклама (мин)">
                        <input type="number" style={inputStyle} value={newSession.adsMinutes} onChange={e => setNewSession({...newSession, adsMinutes: e.target.value})} />
                    </InputGroup>
                    <InputGroup label="Уборка (мин)">
                        <input type="number" style={inputStyle} value={newSession.cleanupMinutes} onChange={e => setNewSession({...newSession, cleanupMinutes: e.target.value})} />
                    </InputGroup>
                    <InputGroup label="Цена билета (₽)">
                        <input type="number" style={inputStyle} value={newSession.basePrice} onChange={e => setNewSession({...newSession, basePrice: e.target.value})} required />
                    </InputGroup>
                </div>
                <button type="submit" disabled={hasOverlap || !selectedMovie} style={{...submitButtonStyle, background: (hasOverlap || !selectedMovie) ? '#555' : '#e50914', cursor: (hasOverlap || !selectedMovie) ? 'not-allowed' : 'pointer'}}>
                    {hasOverlap ? 'Выбранное время занято' : 'Создать сеанс'}
                </button>
            </form>

            {/* МОДАЛЬНОЕ ОКНО РЕДАКТИРОВАНИЯ */}
            {editingSession && (
                <EditSessionModal
                    session={editingSession}
                    movies={movies}
                    halls={halls}
                    onClose={() => setEditingSession(null)}
                    onSave={handleUpdateSession}
                    onDeleteClick={handleDeleteClick}
                    showConfirmDelete={showConfirmDelete}
                    onConfirmDelete={handleDeleteSession}
                    onCancelDelete={handleCancelDelete}
                />
            )}
        </div>
    );
};

// --- КОМПОНЕНТ МОДАЛКИ (ФИНАЛ) ---
const EditSessionModal = ({
    session,
    movies,
    halls,
    onClose,
    onSave,
    onDeleteClick,
    showConfirmDelete,
    onConfirmDelete,
    onCancelDelete
}) => {
    const startDate = new Date(session.startTime);

    const getTimeStr = (d) => {
        const h = String(d.getHours()).padStart(2,'0');
        const m = String(d.getMinutes()).padStart(2,'0');
        return `${h}:${m}`;
    };
    const getDateStr = (d) => d.toISOString().split('T')[0];

    const [form, setForm] = useState({
        movieId: session.movieId,
        hallId: session.hallId,
        date: getDateStr(startDate),
        time: getTimeStr(startDate),
        basePrice: session.price,
        adsMinutes: 15,
        cleanupMinutes: 20
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        const startIso = `${form.date}T${form.time}:00`;
        onSave({
            movieId: form.movieId,
            hallId: form.hallId,
            startTime: startIso,
            basePrice: form.basePrice,
            adsMinutes: form.adsMinutes,
            cleanupMinutes: form.cleanupMinutes
        });
    };

    return (
        <>
            {/* Основное модальное окно редактирования */}
            <div style={{
                position:'fixed', top:0, left:0, width:'100%', height:'100%',
                background:'rgba(0,0,0,0.8)', zIndex:1000,
                display:'flex', alignItems:'center', justifyContent:'center'
            }} onClick={onClose}>

                <div style={{
                    background:'#252525',
                    padding:'30px',
                    borderRadius:'16px',
                    width:'600px',
                    maxWidth:'95%',
                    boxShadow:'0 10px 40px rgba(0,0,0,0.5)',
                    border:'1px solid #444',
                    position: 'relative'
                }} onClick={e => e.stopPropagation()}>

                    {/* КРЕСТИК - ПРИБИТ К ПРАВОМУ ВЕРХНЕМУ УГЛУ ОБЩЕГО ОКНА */}
                    <button
                        onClick={onClose}
                        style={{
                            position: 'absolute',
                            top: '0',
                            right: '0',
                            background:'transparent',
                            border:'none',
                            color:'#aaa',
                            fontSize:'2.5rem',
                            lineHeight: '1',
                            cursor:'pointer',
                            padding: '15px',
                            width: '60px',
                            height: '60px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            zIndex: 10
                        }}
                        onMouseOver={e => e.target.style.color = 'white'}
                        onMouseOut={e => e.target.style.color = '#aaa'}
                    >
                        &times;
                    </button>

                    {/* ЗАГОЛОВОК - В ОДНУ СТРОКУ */}
                    <h3 style={{
                        margin:'0 0 25px 0',
                        color:'white',
                        paddingRight: '60px',
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        lineHeight: '1.2'
                    }}>
                        Редактирование сеанса
                    </h3>

                    <form onSubmit={handleSubmit} style={{display:'flex', flexDirection:'column', gap:'15px'}}>
                        <InputGroup label="Фильм">
                            <select style={inputStyle} value={form.movieId} onChange={e => setForm({...form, movieId: e.target.value})}>
                                {movies.map(m => <option key={m.id} value={m.id}>{m.title}</option>)}
                            </select>
                        </InputGroup>

                        <InputGroup label="Зал">
                            <select style={inputStyle} value={form.hallId} onChange={e => setForm({...form, hallId: e.target.value})}>
                                {halls.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                            </select>
                        </InputGroup>

                        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                            <InputGroup label="Дата">
                                <CalendarPicker
                                    value={form.date}
                                    onChange={(date) => setForm({...form, date})}
                                    label=""
                                    minDate={new Date().toISOString().split('T')[0]}
                                />
                            </InputGroup>
                            <InputGroup label="Время">
                                <TimePicker
                                    value={form.time}
                                    onChange={(time) => setForm({...form, time})}
                                    label=""
                                    interval={15}
                                    minTime="09:00"
                                    maxTime="23:00"
                                />
                            </InputGroup>
                        </div>

                        <InputGroup label="Цена (₽)">
                            <input type="number" style={inputStyle} value={form.basePrice} onChange={e => setForm({...form, basePrice: e.target.value})} />
                        </InputGroup>

                        <div style={{marginTop:'10px', fontSize:'0.85rem', color:'#f39c12', background:'rgba(243, 156, 18, 0.1)', padding:'10px', borderRadius:'4px', lineHeight:'1.4'}}>
                            ⚠️ Если билеты уже куплены, изменение фильма, зала или времени невозможно. Разрешено менять только цену.
                        </div>

                        <div style={{display:'flex', gap:'10px', marginTop:'15px'}}>
                            <button type="submit" style={{...submitButtonStyle, flex:1, marginTop:0}}>СОХРАНИТЬ</button>
                            <button type="button" onClick={onDeleteClick} style={{...submitButtonStyle, background:'#e74c3c', marginTop:0, width:'auto'}}>УДАЛИТЬ</button>
                        </div>
                    </form>
                </div>
            </div>

            {/* ОКНО ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ */}
            {showConfirmDelete && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    background: 'rgba(0,0,0,0.9)',
                    zIndex: 1100,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    <div style={{
                        background: '#252525',
                        padding: '30px',
                        borderRadius: '16px',
                        width: '500px',
                        maxWidth: '90%',
                        boxShadow: '0 10px 40px rgba(0,0,0,0.7)',
                        border: '1px solid #e74c3c',
                        textAlign: 'center'
                    }}>
                        <h3 style={{ color: '#fff', marginBottom: '20px' }}>
                            Подтверждение удаления
                        </h3>

                        <p style={{ color: '#ccc', marginBottom: '25px', fontSize: '1.1rem', lineHeight: '1.5' }}>
                            Вы уверены, что хотите удалить этот сеанс?<br />
                            <span style={{ color: '#e74c3c', fontWeight: 'bold' }}>
                                Это действие нельзя отменить!
                            </span>
                        </p>

                        <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
                            <button
                                onClick={onCancelDelete}
                                style={{
                                    padding: '12px 30px',
                                    background: '#555',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '1rem',
                                    fontWeight: 'bold',
                                    cursor: 'pointer',
                                    flex: 1
                                }}
                            >
                                ОТМЕНА
                            </button>
                            <button
                                onClick={onConfirmDelete}
                                style={{
                                    padding: '12px 30px',
                                    background: '#e74c3c',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '1rem',
                                    fontWeight: 'bold',
                                    cursor: 'pointer',
                                    flex: 1
                                }}
                            >
                                УДАЛИТЬ
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};


const DateSelector = ({ selectedDateStr, onSelect }) => {
    const dates = [];
    const today = new Date();
    for (let i = 0; i < 14; i++) {
        const d = new Date(today);
        d.setDate(today.getDate() + i);
        dates.push(d);
    }
    return (
        <div style={{display:'flex', gap:'10px', overflowX:'auto', paddingBottom:'5px'}}>
            {dates.map((date, idx) => {
                const dStr = date.toLocaleDateString('en-CA');
                const active = dStr === selectedDateStr;
                const dayName = idx===0?'Сегодня':idx===1?'Завтра':date.toLocaleDateString('ru-RU',{weekday:'short'});
                const dayNum = date.toLocaleDateString('ru-RU',{day:'numeric', month:'short'});
                return (
                    <div key={idx} onClick={() => onSelect(dStr)} style={{minWidth:'100px', padding:'10px', borderRadius:'8px', cursor:'pointer', background:active?'#e50914':'#333', border:active?'1px solid #e50914':'1px solid #444', color:active?'white':'#aaa', textAlign:'center', flexShrink:0}}>
                        <div style={{fontSize:'0.8rem', fontWeight:'bold', textTransform:'uppercase', marginBottom:'4px'}}>{dayName}</div>
                        <div style={{fontSize:'0.9rem'}}>{dayNum}</div>
                    </div>
                );
            })}
        </div>
    );
};

const InputGroup = ({label, children}) => (
    <div style={{display:'flex', flexDirection:'column', gap:'8px'}}>
        <label style={{fontSize:'0.85rem', color:'#aaa', fontWeight:'500', textTransform:'uppercase'}}>{label}</label>
        {children}
    </div>
);

const inputStyle = { padding: '12px', background: '#333', border: '1px solid #444', color: 'white', borderRadius: '6px', width: '100%', fontSize:'1rem', outline:'none' };
const submitButtonStyle = { padding: '16px', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1.1rem', fontWeight: 'bold', cursor: 'pointer', marginTop: '10px', textTransform:'uppercase', width: '100%', transition: 'all 0.2s' };

export default SessionManager;