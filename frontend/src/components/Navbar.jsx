import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const Navbar = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const [userEmail, setUserEmail] = useState('');
    const [isAdmin, setIsAdmin] = useState(false);
    const [showMenu, setShowMenu] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));
                setUserEmail(decoded.sub || 'User');

                if (decoded.role === 'ADMIN') {
                    setIsAdmin(true);
                }
            } catch (e) {
                console.error("Invalid token");
            }
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        setUserEmail('');
        setIsAdmin(false);
        setShowMenu(false);
        navigate('/login');
    };

      const handleBack = () => {
        if (location.pathname.startsWith('/session')) {
            navigate(-1);
        }
        else {
            navigate('/');
        }
    };

    const avatarLetter = userEmail ? userEmail.charAt(0).toUpperCase() : 'U';
    const usernameDisplay = userEmail ? userEmail.split('@')[0] : 'Guest';
    const isHomePage = location.pathname === '/';


    const getNavStyle = (path) => {
        if (location.pathname === path) {
            return {
                borderColor: '#e50914',
                background: 'rgba(229, 9, 20, 0.15)',
                color: '#e50914',
                fontWeight: 'bold'
            };
        }
        return {};
    };

    return (
        <header style={{
            display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            padding: '15px 50px',
            background: 'rgba(20, 20, 20, 0.9)',
            backdropFilter: 'blur(10px)',
            position: 'fixed', width: '100%', top: 0, zIndex: 100,
            borderBottom: '1px solid #333',
            boxSizing: 'border-box'
        }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '30px' }}>
                <h1
                    style={{ color: '#e50914', margin: 0, fontSize: '1.8rem', cursor: 'pointer' }}
                    onClick={() => navigate('/')}
                >
                    CinemaPlus
                </h1>

                {!isHomePage && (
                    <button
                        className="nav-btn"
                        onClick={handleBack}
                        style={{
                            display: 'flex', alignItems: 'center', gap: '8px',
                            fontWeight: 'bold', cursor: 'pointer'
                        }}
                    >
                        <span>&larr;</span> Назад
                    </button>
                )}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>

                {isAdmin && (
                    <button
                        className="nav-btn"
                        onClick={() => navigate('/admin')}
                        style={getNavStyle('/admin')}
                    >
                        АДМИНКА
                    </button>
                )}

                <button
                    className="nav-btn"
                    onClick={() => navigate('/halls')}
                    style={getNavStyle('/halls')}
                >
                    Залы
                </button>

                {userEmail && (
                    <button
                        className="nav-btn"
                        onClick={() => navigate('/tickets')}
                        style={getNavStyle('/tickets')}
                    >
                        Мои билеты
                    </button>
                )}

                {!userEmail ? (
                    <button
                        className="nav-btn logout"
                        onClick={() => navigate('/login')}
                        style={{ fontWeight: 'bold' }}
                    >
                        Войти
                    </button>
                ) : (
                    <div className="profile-wrapper">
                        <div
                            className="avatar-circle"
                            onClick={() => setShowMenu(!showMenu)}
                        >
                            {avatarLetter}
                        </div>

                        {showMenu && (
                            <div className="profile-dropdown">
                                <div className="user-details">
                                    <div className="detail-row">
                                        <span className="detail-label">Имя аккаунта</span>
                                        <span className="detail-value">{usernameDisplay}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span className="detail-label">Email</span>
                                        <span className="detail-value" title={userEmail}>{userEmail}</span>
                                    </div>
                                </div>

                                <div className="menu-divider"></div>

                                <div className="menu-item">Сменить имя</div>
                                <div className="menu-item">Сменить пароль</div>
                                <div className="menu-item">Сменить Email</div>

                                <div className="menu-divider"></div>

                                <div className="menu-item logout-item" onClick={handleLogout}>
                                    Выйти
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </header>
    );
};

export default Navbar;