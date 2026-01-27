import { useState } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/admin/Sidebar';
import SessionManager from '../components/admin/SessionManager';
import MovieManager from '../components/admin/MovieManager';
import HallManager from '../components/admin/HallManager';
import PricingManager from '../components/admin/PricingManager';
import DashboardStats from '../components/admin/DashboardStats';
import Toast from '../components/Toast'; // Импортируем Toast из отдельного файла

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState('sessions');
    const [toast, setToast] = useState(null);

    const showToast = (message, type = 'error') => {
        setToast({ message, type });
    };

    const renderContent = () => {
        switch (activeTab) {
            case 'dashboard': return <DashboardStats />;
            case 'halls': return <HallManager showToast={showToast} />;
            case 'movies': return <MovieManager showToast={showToast} />;
            case 'sessions': return <SessionManager showToast={showToast} />;
            case 'pricing': return <PricingManager showToast={showToast} />;
            default: return <h2>Добро пожаловать</h2>;
        }
    };

    return (
        <div style={{ width: '100%', minHeight: '100vh', color: '#e0e0e0', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            <Navbar />

            {/* Toast уведомление */}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast(null)}
                />
            )}

            <div style={{ display: 'flex', flexGrow: 1, marginTop: '76px', height: 'calc(100vh - 76px)' }}>
                <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />

                {/* ОСНОВНОЙ КОНТЕНТ */}
                <div style={{
                    marginLeft: '260px',
                    padding: '30px',
                    width: '100%',
                    background: '#0a0a0a',
                    overflowY: 'auto'
                }}>
                    <div style={{
                        background: '#181818',
                        padding: '30px',
                        borderRadius: '16px',
                        border: '1px solid #2a2a2a',
                        boxShadow: '0 4px 30px rgba(0,0,0,0.5)',
                        maxWidth: activeTab === 'halls' ? '100%' : '1200px',
                        margin: '0 auto',
                        minHeight: '100%'
                    }}>
                        {renderContent()}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminPage;