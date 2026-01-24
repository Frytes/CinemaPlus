import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { jwtDecode } from "jwt-decode"; // ⚠️ ВАЖНО: фигурные скобки обязательны для v4

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
                // Пытаемся декодировать
                const decoded = jwtDecode(token);

                // Логи для отладки (смотри в консоль браузера F12)
                console.log("Token decoded successfully:", decoded);

                // "sub" - это email (subject) из нашего Java JwtService
                setUserEmail(decoded.sub || '');

                if (decoded.role === 'ADMIN') {
                    setIsAdmin(true);
                }
            } catch (e) {
                console.error("Ошибка декодирования токена:", e);
                // Если токен битый — удаляем его, чтобы не путать систему
                localStorage.removeItem('token');
                setUserEmail('');
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
        } else {
            navigate('/');
        }
    };

    const avatarLetter = userEmail ? userEmail.charAt(0).toUpperCase() : 'U';
    const usernameDisplay = userEmail ? userEmail.split('@')[0] : 'Guest';
    const isHomePage = location.pathname === '/';



    const getNavStyle = (path) => {
        if (location.pathname === path) {
            return {
                borderColor: '#ff0000',
                background: 'linear-gradient(90deg, rgba(255, 0, 0, 0.15), rgba(208, 0, 0, 0.15), rgba(157, 2, 8, 0.15))',
                color: '#ff0000',
                fontWeight: 'bold',
                boxShadow: '0 4px 12px rgba(255, 0, 0, 0.2)'
            };
        }
        return {};
    };

    return (
        <header style={{
            display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            padding: '15px 50px',
            background: 'linear-gradient(135deg, #0a0a0a 0%, #1a0a0a 100%)',
            backdropFilter: 'blur(10px)',
            position: 'fixed', width: '100%', top: 0, zIndex: 100,
            borderBottom: '2px solid rgba(255, 0, 0, 0.3)',
            boxSizing: 'border-box'
        }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '30px' }}>
                <h1
                    style={{
                        color: '#ff0000',
                        margin: 0,
                        fontSize: '1.8rem',
                        cursor: 'pointer',
                        fontWeight: '900',
                        letterSpacing: '1px'
                    }}
                    onClick={() => navigate('/')}
                >
                    CINEMA<span style={{ color: '#ffffff' }}>PLUS</span>
                </h1>

                {!isHomePage && (
                    <button
                        className="nav-btn"
                        onClick={handleBack}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            background: 'rgba(255, 0, 0, 0.1)',
                            color: '#ffffff',
                            border: '2px solid rgba(255, 0, 0, 0.3)',
                            padding: '10px 20px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '15px',
                            fontWeight: '600',
                            transition: 'all 0.2s ease',
                            height: '45px'
                        }}
                        onMouseOver={(e) => {
                            e.target.style.background = 'rgba(255, 0, 0, 0.2)';
                            e.target.style.transform = 'translateY(-1px)';
                        }}
                        onMouseOut={(e) => {
                            e.target.style.background = 'rgba(255, 0, 0, 0.1)';
                            e.target.style.transform = 'translateY(0)';
                        }}
                    >
                        <span style={{ fontSize: '16px' }}>&larr;</span> Назад
                    </button>
                )}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>

                {isAdmin && (
                    <button
                        className="nav-btn"
                        onClick={() => navigate('/admin')}
                        style={{
                            ...getNavStyle('/admin'),
                            background: getNavStyle('/admin').background || 'rgba(40, 40, 40, 0.8)',
                            color: getNavStyle('/admin').color || '#ff6b6b',
                            border: '2px solid rgba(255, 0, 0, 0.3)',
                            padding: '10px 20px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '15px',
                            fontWeight: '600',
                            transition: 'all 0.2s ease',
                            height: '45px'
                        }}
                        onMouseOver={(e) => {
                            if (!getNavStyle('/admin').color) {
                                e.target.style.background = 'rgba(255, 0, 0, 0.15)';
                                e.target.style.color = '#ffffff';
                                e.target.style.transform = 'translateY(-1px)';
                            }
                        }}
                        onMouseOut={(e) => {
                            if (!getNavStyle('/admin').color) {
                                e.target.style.background = 'rgba(40, 40, 40, 0.8)';
                                e.target.style.color = '#ff6b6b';
                                e.target.style.transform = 'translateY(0)';
                            }
                        }}
                    >
                        АДМИНКА
                    </button>
                )}

                <button
                    className="nav-btn"
                    onClick={() => navigate('/halls')}
                    style={{
                        ...getNavStyle('/halls'),
                        background: getNavStyle('/halls').background || 'rgba(40, 40, 40, 0.8)',
                        color: getNavStyle('/halls').color || '#ffffff',
                        border: '2px solid rgba(255, 0, 0, 0.3)',
                        padding: '10px 20px',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontSize: '15px',
                        fontWeight: '600',
                        transition: 'all 0.2s ease',
                        height: '45px'
                    }}
                    onMouseOver={(e) => {
                        if (!getNavStyle('/halls').color) {
                            e.target.style.background = 'rgba(255, 0, 0, 0.15)';
                            e.target.style.color = '#ffffff';
                            e.target.style.transform = 'translateY(-1px)';
                        }
                    }}
                    onMouseOut={(e) => {
                        if (!getNavStyle('/halls').color) {
                            e.target.style.background = 'rgba(40, 40, 40, 0.8)';
                            e.target.style.color = '#ffffff';
                            e.target.style.transform = 'translateY(0)';
                        }
                    }}
                >
                    Залы
                </button>

                {userEmail && (
                    <button
                        className="nav-btn"
                        onClick={() => navigate('/tickets')}
                        style={{
                            ...getNavStyle('/tickets'),
                            background: getNavStyle('/tickets').background || 'rgba(40, 40, 40, 0.8)',
                            color: getNavStyle('/tickets').color || '#ffffff',
                            border: '2px solid rgba(255, 0, 0, 0.3)',
                            padding: '10px 20px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '15px',
                            fontWeight: '600',
                            transition: 'all 0.2s ease',
                            height: '45px'
                        }}
                        onMouseOver={(e) => {
                            if (!getNavStyle('/tickets').color) {
                                e.target.style.background = 'rgba(255, 0, 0, 0.15)';
                                e.target.style.color = '#ffffff';
                                e.target.style.transform = 'translateY(-1px)';
                            }
                        }}
                        onMouseOut={(e) => {
                            if (!getNavStyle('/tickets').color) {
                                e.target.style.background = 'rgba(40, 40, 40, 0.8)';
                                e.target.style.color = '#ffffff';
                                e.target.style.transform = 'translateY(0)';
                            }
                        }}
                    >
                        Мои билеты
                    </button>
                )}

                {!userEmail ? (
                    <button
                        className="nav-btn logout"
                        onClick={() => navigate('/login')}
                        style={{
                            background: 'linear-gradient(90deg, #ff0000, #d00000, #9d0208)',
                            color: '#ffffff',
                            border: 'none',
                            padding: '12px 28px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '16px',
                            fontWeight: '700',
                            transition: 'all 0.2s ease',
                            boxShadow: '0 4px 12px rgba(255, 0, 0, 0.4)',
                            height: '45px',
                            letterSpacing: '0.5px'
                        }}
                        onMouseOver={(e) => {
                            e.target.style.boxShadow = '0 6px 16px rgba(255, 0, 0, 0.6)';
                            e.target.style.transform = 'translateY(-2px)';
                        }}
                        onMouseOut={(e) => {
                            e.target.style.boxShadow = '0 4px 12px rgba(255, 0, 0, 0.4)';
                            e.target.style.transform = 'translateY(0)';
                        }}
                    >
                        Войти
                    </button>
                ) : (
                    <div className="profile-wrapper">
                        <div
                            className="avatar-circle"
                            onClick={() => setShowMenu(!showMenu)}
                            style={{
                                width: '42px',
                                height: '45px',
                                borderRadius: '50%',
                                background: 'linear-gradient(to bottom, #ff0000, #d00000, #9d0208)',
                                color: '#ffffff',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '19px',
                                fontWeight: 'bold',
                                cursor: 'pointer',
                                border: '2px solid rgba(255, 255, 255, 0.3)',
                                boxShadow: '0 4px 12px rgba(255, 0, 0, 0.3)',
                                transition: 'all 0.2s ease'
                            }}
                            onMouseOver={(e) => {
                                e.target.style.transform = 'scale(1.05)';
                                e.target.style.boxShadow = '0 6px 16px rgba(255, 0, 0, 0.4)';
                            }}
                            onMouseOut={(e) => {
                                e.target.style.transform = 'scale(1)';
                                e.target.style.boxShadow = '0 4px 12px rgba(255, 0, 0, 0.3)';
                            }}
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