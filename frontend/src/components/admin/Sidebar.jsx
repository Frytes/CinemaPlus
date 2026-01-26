import React from 'react';


const SidebarItem = ({ label, active, onClick }) => (
    <div
        onClick={onClick}
        style={{
            padding: '14px 25px',
            cursor: 'pointer',
            background: active ? 'rgba(229, 9, 20, 0.1)' : 'transparent',
            color: active ? '#e50914' : '#aaa',
            borderLeft: active ? '4px solid #e50914' : '4px solid transparent',
            fontWeight: active ? '600' : '500',
            transition: 'all 0.2s',
            fontSize: '0.95rem'
        }}
        onMouseOver={(e) => !active && (e.currentTarget.style.color = 'white')}
        onMouseOut={(e) => !active && (e.currentTarget.style.color = '#aaa')}
    >
        {label}
    </div>
);

const Sidebar = ({ activeTab, setActiveTab }) => {
    return (
        <div style={{
            width: '260px',
            background: '#121212',
            borderRight: '1px solid #333',
            padding: '30px 0',
            display: 'flex',
            flexDirection: 'column',
            position: 'fixed',
            bottom: 0,
            top: '76px',
            zIndex: 10
        }}>
            <div style={{
                padding: '0 25px 20px',
                fontSize: '0.85rem',
                color: '#666',
                textTransform: 'uppercase',
                letterSpacing: '1px'
            }}>
                Меню
            </div>

            <SidebarItem
                label="📊 Статистика"
                active={activeTab === 'dashboard'}
                onClick={() => setActiveTab('dashboard')}
            />

            <div style={{ height: '1px', background: '#222', margin: '15px 25px' }}></div>

            <SidebarItem
                label="🏛️ Залы"
                active={activeTab === 'halls'}
                onClick={() => setActiveTab('halls')}
            />
            <SidebarItem
                label="🎬 Фильмы"
                active={activeTab === 'movies'}
                onClick={() => setActiveTab('movies')}
            />
            <SidebarItem
                label="📅 Управление Сеансами"
                active={activeTab === 'sessions'}
                onClick={() => setActiveTab('sessions')}
            />

            <div style={{ height: '1px', background: '#222', margin: '15px 25px' }}></div>

            <SidebarItem
                label="💰 Цены и Скидки"
                active={activeTab === 'pricing'}
                onClick={() => setActiveTab('pricing')}
            />
        </div>
    );
};

export default Sidebar;