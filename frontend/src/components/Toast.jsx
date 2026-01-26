const Toast = ({ message, type = 'error' }) => {
    return (
        <div style={{
            position: 'fixed',
            bottom: '100px',
            left: '50%',
            transform: 'translateX(-50%)',
            background: type === 'error' ? '#ff4444' : '#2ecc71',
            color: 'white',
            padding: '12px 24px',
            borderRadius: '8px',
            boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
            zIndex: 9999,
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            animation: 'fadeIn 0.3s ease-out'
        }}>
            <span>{type === 'error' ? '⚠️' : '✅'}</span>
            <span style={{ fontWeight: '500' }}>{message}</span>
            <style>{`
                @keyframes fadeIn {
                    from { opacity: 0; transform: translate(-50%, 20px); }
                    to { opacity: 1; transform: translate(-50%, 0); }
                }
            `}</style>
        </div>
    );
};

export default Toast;