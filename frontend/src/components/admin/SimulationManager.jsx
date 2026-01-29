import { useState } from 'react';
import api from '../../api/axiosConfig';

const SimulationManager = ({ showToast }) => {
    const [loading, setLoading] = useState(false);

    const handleStart = async () => {
        if(!window.confirm('⚠️ Вы уверены? Это создаст высокую нагрузку на систему!')) return;

        setLoading(true);
        try {
            await api.post('/simulation/start');
            showToast('🚀 Симуляция запущена! Смотрите графики.', 'success');
        } catch (err) {
            showToast('Ошибка запуска: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleStop = async () => {
        setLoading(true);
        try {
            await api.post('/simulation/stop');
            showToast('🛑 Сигнал остановки отправлен', 'success');
        } catch (err) {
            showToast('Ошибка остановки: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ marginTop: 0, borderBottom: '1px solid #333', paddingBottom: '15px' }}>
                🤖 Стресс-тестирование
            </h2>

            <div style={{
                background: '#252525',
                padding: '30px',
                borderRadius: '12px',
                border: '1px solid #444',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '20px'
            }}>
                <div style={{ fontSize: '3rem' }}>🔥</div>
                <div style={{ textAlign: 'center', color: '#ccc', maxWidth: '500px' }}>
                    Этот инструмент запускает виртуальных ботов, которые регистрируются, ищут сеансы и покупают билеты.
                    Параметры нагрузки настроены в <code>application.yaml</code>.
                </div>

                <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
                    <button
                        onClick={handleStart}
                        disabled={loading}
                        style={{
                            padding: '15px 40px',
                            background: '#e50914',
                            color: 'white',
                            border: 'none',
                            borderRadius: '8px',
                            fontSize: '1.2rem',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            opacity: loading ? 0.7 : 1,
                            boxShadow: '0 0 20px rgba(229, 9, 20, 0.4)'
                        }}
                    >
                        {loading ? '...' : 'ЗАПУСК АТАКИ'}
                    </button>

                    <button
                        onClick={handleStop}
                        disabled={loading}
                        style={{
                            padding: '15px 40px',
                            background: '#333',
                            color: 'white',
                            border: '1px solid #555',
                            borderRadius: '8px',
                            fontSize: '1.2rem',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer'
                        }}
                    >
                        ОСТАНОВИТЬ
                    </button>
                </div>

                <div style={{ marginTop: '20px', fontSize: '0.9rem', color: '#666' }}>
                    Мониторинг доступен в <a href="http://localhost:3000" target="_blank" style={{color: '#e50914'}}>Grafana</a>
                </div>
            </div>
        </div>
    );
};

export default SimulationManager;