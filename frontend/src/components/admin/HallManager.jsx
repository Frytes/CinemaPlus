import { useState } from 'react';
import api from '../../api/axiosConfig';

const HallManager = ({ showToast }) => {
    const [form, setForm] = useState({ name: '', width: '', height: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/halls', form);
            showToast('✅ Зал успешно создан!', 'success');
            setForm({ name: '', width: '', height: '' });
        } catch (err) {
            showToast(err.response?.data?.message || 'Ошибка', 'error');
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ marginTop: 0, borderBottom:'1px solid #333', paddingBottom:'15px' }}>🏛️ Создать новый зал</h2>
            <InputGroup label="Название зала">
                <input placeholder="Например: Зеленый зал" value={form.name} onChange={e => setForm({...form, name: e.target.value})} required style={inputStyle} />
            </InputGroup>
            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'20px'}}>
                <InputGroup label="Мест в ряду (ширина)">
                    <input type="number" value={form.width} onChange={e => setForm({...form, width: e.target.value})} required style={inputStyle} />
                </InputGroup>
                <InputGroup label="Рядов (высота)">
                    <input type="number" value={form.height} onChange={e => setForm({...form, height: e.target.value})} required style={inputStyle} />
                </InputGroup>
            </div>
            <button type="submit" style={submitButtonStyle}>Создать зал</button>
        </form>
    );
};

const InputGroup = ({label, children}) => (
    <div style={{display:'flex', flexDirection:'column', gap:'8px'}}>
        <label style={{fontSize:'0.85rem', color:'#aaa', fontWeight:'500'}}>{label}</label>
        {children}
    </div>
);
const inputStyle = {
    padding: '12px', background: '#333', border: '1px solid #444',
    color: 'white', borderRadius: '6px', width: '100%', fontSize:'1rem', outline:'none'
};
const submitButtonStyle = {
    padding: '16px', background: '#e50914', color: 'white', border: 'none',
    borderRadius: '8px', fontSize: '1.1rem', fontWeight: 'bold', cursor: 'pointer',
    marginTop: '10px', textTransform:'uppercase', letterSpacing:'1px', width: '100%'
};

export default HallManager;