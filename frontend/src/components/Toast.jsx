import { useEffect, useState } from 'react';

const Toast = ({ message, type = 'error', onClose }) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        requestAnimationFrame(() => setIsVisible(true));

        const timer = setTimeout(() => {
            setIsVisible(false);
            setTimeout(onClose, 300);
        }, 3000);

        return () => clearTimeout(timer);
    }, [onClose]);

    const isSuccess = type === 'success';

    return (
        <div style={{
            position: 'fixed',
            bottom: '30px',
            left: '50%',
            transform: `translate(-50%, ${isVisible ? '0' : '20px'})`,
            opacity: isVisible ? 1 : 0,
            background: 'rgba(30, 30, 30, 0.9)',
            backdropFilter: 'blur(10px)',
            color: 'white',
            padding: '12px 24px',
            borderRadius: '12px',
            border: `1px solid ${isSuccess ? '#2ecc71' : '#e74c3c'}`,
            boxShadow: '0 8px 30px rgba(0,0,0,0.5)',
            zIndex: 9999,
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            transition: 'all 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
            minWidth: '300px',
            maxWidth: '90vw'
        }}>
            <div style={{
                width: '24px',
                height: '24px',
                borderRadius: '50%',
                background: isSuccess ? 'rgba(46, 204, 113, 0.2)' : 'rgba(231, 76, 60, 0.2)',
                color: isSuccess ? '#2ecc71' : '#e74c3c',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '14px'
            }}>
                {isSuccess ? '✓' : '!'}
            </div>

            <span style={{ fontWeight: '500', fontSize: '0.95rem' }}>{message}</span>
        </div>
    );
};

export default Toast;