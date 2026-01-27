import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

const HallManager = ({ showToast }) => {

    const [name, setName] = useState('');
    const [rows, setRows] = useState(10);
    const [cols, setCols] = useState(15);
    const [grid, setGrid] = useState([]);
    const [loading, setLoading] = useState(false);


    useEffect(() => {
        setGrid(prev => {
            const newGrid = [];
            for (let r = 0; r < rows; r++) {
                const row = [];
                for (let c = 0; c < cols; c++) {
                    const oldCell = prev[r]?.[c];
                    row.push(oldCell ? { ...oldCell } : { type: 'STANDARD', row: r, col: c });
                }
                newGrid.push(row);
            }
            return newGrid;
        });
    }, [rows, cols]);

    // --- ОБРАБОТЧИКИ ---

    const handleLeftClick = (r, c) => {
        setGrid(prev => {
            const newGrid = prev.map(row => [...row]);
            const cell = { ...newGrid[r][c] };

            if (cell.type === 'EMPTY') cell.type = 'STANDARD';
            else if (cell.type === 'STANDARD') cell.type = 'VIP';
            else if (cell.type === 'VIP') cell.type = 'STANDARD';

            newGrid[r][c] = cell;
            return newGrid;
        });
    };

    // ПКМ: Удалить / Вернуть
    const handleRightClick = (e, r, c) => {
        e.preventDefault();
        setGrid(prev => {
            const newGrid = prev.map(row => [...row]);
            const cell = { ...newGrid[r][c] };

            if (cell.type === 'EMPTY') cell.type = 'STANDARD';
            else cell.type = 'EMPTY';

            newGrid[r][c] = cell;
            return newGrid;
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        const seatsPayload = [];


        for (let r = 0; r < rows; r++) {
            let seatCounter = 1;
            for (let c = 0; c < cols; c++) {
                const cell = grid[r][c];
                if (cell.type !== 'EMPTY') {
                    seatsPayload.push({
                        row: r,
                        col: c,
                        type: cell.type,
                        seatNumber: `${seatCounter}`
                    });
                    seatCounter++;
                }
            }
        }

        try {
            await api.post('/halls', {
                name,
                width: cols,
                height: rows,
                seats: seatsPayload
            });
            showToast('Зал успешно создан!', 'success');
            setName('');
        } catch (err) {
            showToast('Ошибка: ' + (err.response?.data?.message || err.message), 'error');
        } finally {
            setLoading(false);
        }
    };

    // --- ВЫЧИСЛЕНИЯ ---
    const totalSeats = grid.reduce((acc, row) =>
        acc + row.filter(s => s.type !== 'EMPTY').length, 0
    );

    const seatSize = Math.min(Math.max(Math.floor((800 - 80) / cols), 20), 40);

    return (
        <div style={{ display: 'grid', gridTemplateColumns: '350px 1fr', gap: '30px', alignItems: 'start', height: '100%' }}>

            {/* --- ЛЕВАЯ ПАНЕЛЬ --- */}
            <div style={{ background: '#252525', padding: '25px', borderRadius: '12px', border: '1px solid #444', height: 'fit-content' }}>
                <h3 style={{ margin: '0 0 20px 0', borderBottom:'1px solid #444', paddingBottom:'15px' }}>🏗️ Конструктор</h3>

                <form onSubmit={handleSubmit} style={{display:'flex', flexDirection:'column', gap:'20px'}}>
                    <InputGroup label="Название зала">
                        <input value={name} onChange={e => setName(e.target.value)} required style={inputStyle} placeholder="Например: IMAX Зал"/>
                    </InputGroup>

                    <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                        <InputGroup label="Рядов">
                            <input type="number" min="1" max="30" value={rows} onChange={e => setRows(Number(e.target.value))} style={inputStyle}/>
                        </InputGroup>
                        <InputGroup label="Мест в ряду">
                            <input type="number" min="1" max="40" value={cols} onChange={e => setCols(Number(e.target.value))} style={inputStyle}/>
                        </InputGroup>
                    </div>

                    <div style={{background:'rgba(255,255,255,0.05)', padding:'15px', borderRadius:'8px'}}>
                        <div style={{display:'flex', justifyContent:'space-between', marginBottom:'10px', fontSize:'1.1rem'}}>
                            <span style={{color:'#aaa'}}>Всего мест:</span>
                            <strong style={{color:'#e50914'}}>{totalSeats}</strong>
                        </div>

                        <p style={{margin:'10px 0 5px', fontSize:'0.8rem', color:'#888', textTransform:'uppercase'}}>Управление:</p>
                        <ul style={{margin:0, paddingLeft:'20px', fontSize:'0.85rem', color:'#ccc', lineHeight:'1.6'}}>
                            <li>🖱️ <strong>ЛКМ:</strong> Стандарт ↔ VIP</li>
                            <li>🖱️ <strong>ПКМ:</strong> Удалить место (проход)</li>
                        </ul>
                    </div>

                    <div style={{display:'flex', gap:'10px', justifyContent:'center', fontSize:'0.8rem', color:'#aaa'}}>
                        <div style={{display:'flex', alignItems:'center', gap:'5px'}}>
                            <div style={{width:'15px', height:'15px', background:'#2ecc71', borderRadius:'3px'}}></div> STANDARD
                        </div>
                        <div style={{display:'flex', alignItems:'center', gap:'5px'}}>
                            <div style={{width:'15px', height:'15px', background:'linear-gradient(135deg, #ffd700, #ff9900)', borderRadius:'3px'}}></div> VIP
                        </div>
                        <div style={{display:'flex', alignItems:'center', gap:'5px'}}>
                            <div style={{width:'15px', height:'15px', border:'1px dashed #555', borderRadius:'3px'}}></div> Пусто
                        </div>
                    </div>

                    <button type="submit" disabled={loading || !name} style={submitButtonStyle}>
                        {loading ? 'Сохранение...' : 'СОХРАНИТЬ ЗАЛ'}
                    </button>
                </form>
            </div>

            {/* --- ПРАВАЯ ПАНЕЛЬ (ПРЕВЬЮ) --- */}
            <div style={{
                background: '#1a1a1a',
                padding: '30px',
                borderRadius: '12px',
                border: '1px solid #333',
                minHeight: '600px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                overflowX: 'auto'
            }}>
                <div style={{width:'60%', height:'8px', background: 'linear-gradient(to right, transparent, #555, transparent)', borderRadius:'4px', marginBottom:'40px', boxShadow:'0 0 20px rgba(255,255,255,0.1)'}}></div>

                <div style={{ display:'flex', flexDirection:'column', gap:'6px' }}>
                    {grid.map((row, rIndex) => {
                        const activeCount = row.filter(s => s.type !== 'EMPTY').length;

                        return (
                            <div key={rIndex} style={{ display:'flex', alignItems:'center', gap:'15px' }}>
                                <span style={rowLabelStyle}>{rIndex + 1}</span>

                                <div style={{ display:'flex', gap:'4px' }}>
                                    {row.map((cell, cIndex) => (
                                        <div
                                            key={`${rIndex}-${cIndex}`}
                                            onMouseDown={(e) => {
                                                if (e.button === 0) handleLeftClick(rIndex, cIndex);
                                                if (e.button === 2) handleRightClick(e, rIndex, cIndex);
                                            }}
                                            onContextMenu={(e) => e.preventDefault()}
                                            style={{
                                                width: `${seatSize}px`,
                                                height: `${seatSize}px`,
                                                borderRadius: '4px',
                                                cursor: 'pointer',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                fontSize: seatSize < 25 ? '0' : '0.6rem',
                                                fontWeight: 'bold',
                                                userSelect: 'none',
                                                transition: 'all 0.15s cubic-bezier(0.18, 0.89, 0.32, 1.28)',
                                                transform: 'scale(1)',
                                                ...getSeatStyle(cell.type),

                                            }}
                                        >
                                            {cell.type === 'VIP' && seatSize > 20 && (
                                                <span style={{
                                                    fontSize: seatSize < 30 ? '0.5rem' : '0.65rem',
                                                    fontWeight: '800',
                                                    textShadow: '0 1px 2px rgba(0,0,0,0.3)'
                                                }}>
                                                    V
                                                </span>
                                            )}
                                            {cell.type === 'STANDARD' && seatSize > 25 && (
                                                <span style={{
                                                    fontSize: seatSize < 30 ? '0.4rem' : '0.55rem',
                                                    color: 'rgba(255,255,255,0.7)',
                                                    fontWeight: '600'
                                                }}>

                                                </span>
                                            )}
                                        </div>
                                    ))}
                                </div>

                                <span style={rowLabelStyle}>{rIndex + 1}</span>
                                <span style={{...rowLabelStyle, color: '#e50914', width: '30px', textAlign:'right', fontSize:'0.75rem'}}>
                                    {activeCount}
                                </span>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

// --- СТИЛИ ---
const getSeatStyle = (type) => {
    const baseStyle = {
        boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
    };

    switch (type) {
        case 'VIP':
            return {
                ...baseStyle,
                background: 'linear-gradient(135deg, #ffd700, #ff9900)',
                border: '1px solid #ff9900',
                color: '#000',
                boxShadow: '0 0 5px rgba(255, 215, 0, 0.4), 0 2px 4px rgba(0,0,0,0.2)'
            };
        case 'STANDARD':
            return {
                ...baseStyle,
                background: '#2ecc71',
                border: '1px solid #27ae60',
                color: 'transparent'
            };
        default:
            return {
                ...baseStyle,
                background: 'transparent',
                border: '1px dashed #555',
                color: 'transparent',
                opacity: 0.3
            };
    }
};

const rowLabelStyle = {
    color: '#666',
    fontSize: '0.8rem',
    width: '20px',
    textAlign: 'center',
    fontWeight: 'bold',
    userSelect: 'none'
};

const InputGroup = ({label, children}) => (
    <div style={{display:'flex', flexDirection:'column', gap:'8px'}}>
        <label style={{fontSize:'0.85rem', color:'#aaa', fontWeight:'500'}}>{label}</label>
        {children}
    </div>
);

const inputStyle = {
    padding: '12px',
    background: '#333',
    border: '1px solid #444',
    color: 'white',
    borderRadius: '6px',
    width: '100%',
    outline: 'none',
    transition: 'all 0.2s',
    ':focus': {
        borderColor: '#e50914',
        boxShadow: '0 0 0 2px rgba(229, 9, 20, 0.1)'
    }
};

const submitButtonStyle = {
    padding: '16px',
    background: '#e50914',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '1rem',
    fontWeight: 'bold',
    cursor: 'pointer',
    marginTop: '10px',
    transition: 'all 0.2s cubic-bezier(0.18, 0.89, 0.32, 1.28)',
    transform: 'translateY(0)',
    boxShadow: '0 4px 6px rgba(229, 9, 20, 0.2)',
    ':hover:not(:disabled)': {
        background: '#f40612',
        transform: 'translateY(-2px)',
        boxShadow: '0 6px 12px rgba(229, 9, 20, 0.3)'
    },
    ':active:not(:disabled)': {
        transform: 'translateY(0)',
        transition: 'transform 0.1s'
    },
    ':disabled': {
        background: '#555',
        cursor: 'not-allowed',
        transform: 'none',
        boxShadow: 'none'
    }
};

export default HallManager;