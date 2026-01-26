import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

const PricingManager = ({ showToast }) => {
    const [rules, setRules] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadRules = async () => {
        try {
            const res = await api.get('/pricing');
            const sortedData = res.data.sort((a, b) => a.ruleName.localeCompare(b.ruleName));
            setRules(sortedData);
        } catch (err) {
            console.error(err);
            showToast('Ошибка загрузки правил', 'error');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRules();
    }, []);

    const handleUpdate = async (ruleName, amount, isActive) => {
        try {
            await api.put(`/pricing/${ruleName}`, { amount, isActive });
            showToast(' Правило обновлено', 'success');
            loadRules();
        } catch (err) {
            showToast('Ошибка обновления', 'error');
        }
    };

    const getRuleDescription = (name) => {
        switch (name) {
            case 'VIP_SURCHARGE': return 'Наценка за VIP места. Прибавляется к базовой цене сеанса.';
            case 'MORNING_DISCOUNT': return 'Скидка на утренние сеансы (до 14:00). Вычитается из цены.';
            default: return 'Правило ценообразования';
        }
    };

    const getRuleLabel = (name) => {
        switch (name) {
            case 'VIP_SURCHARGE': return '👑 VIP Наценка';
            case 'MORNING_DISCOUNT': return '☀️ Утренняя Скидка';
            default: return name;
        }
    };

    if (loading) return <div>Загрузка...</div>;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ marginTop: 0, borderBottom: '1px solid #333', paddingBottom: '15px' }}>
                💰 Управление Ценами (Стратегии)
            </h2>

            <div style={{ display: 'grid', gap: '20px' }}>
                {rules.map(rule => (
                    <div key={rule.ruleName} style={{
                        background: '#252525',
                        padding: '20px',
                        borderRadius: '12px',
                        border: '1px solid #444',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        opacity: rule.isActive ? 1 : 0.6,
                        transition: 'opacity 0.2s'
                    }}>
                        <div style={{ maxWidth: '60%' }}>
                            <h3 style={{ margin: '0 0 5px 0', color: 'white', display:'flex', alignItems:'center', gap:'10px' }}>
                                {getRuleLabel(rule.ruleName)}
                                {!rule.isActive && <span style={{fontSize:'0.7rem', background:'#555', padding:'2px 6px', borderRadius:'4px'}}>ВЫКЛ</span>}
                            </h3>
                            <p style={{ margin: 0, color: '#aaa', fontSize: '0.9rem' }}>
                                {getRuleDescription(rule.ruleName)}
                            </p>
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                            <div style={{display:'flex', flexDirection:'column', gap:'5px'}}>
                                <label style={{fontSize:'0.75rem', color:'#888'}}>Сумма (₽)</label>
                                <input
                                    type="number"
                                    defaultValue={rule.amount}
                                    onBlur={(e) => handleUpdate(rule.ruleName, e.target.value, rule.isActive)}
                                    style={{
                                        padding: '10px', width: '100px', background: '#333', border: '1px solid #555',
                                        color: 'white', borderRadius: '6px', textAlign:'center', fontWeight:'bold'
                                    }}
                                />
                            </div>

                            <div style={{display:'flex', flexDirection:'column', gap:'5px', alignItems:'center'}}>
                                <label style={{fontSize:'0.75rem', color:'#888'}}>Статус</label>
                                <label className="switch" style={{position:'relative', display:'inline-block', width:'50px', height:'28px'}}>
                                    <input
                                        type="checkbox"
                                        checked={rule.isActive}
                                        onChange={(e) => handleUpdate(rule.ruleName, rule.amount, e.target.checked)}
                                        style={{opacity:0, width:0, height:0}}
                                    />
                                    <span style={{
                                        position:'absolute', cursor:'pointer', top:0, left:0, right:0, bottom:0,
                                        backgroundColor: rule.isActive ? '#2ecc71' : '#ccc',
                                        transition: '.4s', borderRadius:'34px'
                                    }}>
                                        <span style={{
                                            position:'absolute', content:"", height:'20px', width:'20px',
                                            left: '4px', bottom: '4px', backgroundColor: 'white',
                                            transition: '.4s', borderRadius: '50%',
                                            transform: rule.isActive ? 'translateX(22px)' : 'translateX(0)'
                                        }}></span>
                                    </span>
                                </label>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PricingManager;