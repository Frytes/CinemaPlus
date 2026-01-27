import { useEffect, useState, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { jwtDecode } from "jwt-decode";
import ProfileModal from './ProfileModal';

const Navbar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const profileRef = useRef(null);

    const [userEmail, setUserEmail] = useState('');
    const [userName, setUserName] = useState('');
    const [isAdmin, setIsAdmin] = useState(false);
    const [showMenu, setShowMenu] = useState(false);
    const [isProfileOpen, setIsProfileOpen] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');

        if (token) {
            try {
                const decoded = jwtDecode(token);
                const email = decoded.sub || '';
                setUserEmail(email);

                const usernameKey = `username_${email}`;
                const savedUsername = localStorage.getItem(usernameKey);

                if (savedUsername && savedUsername !== 'undefined' && savedUsername !== 'null') {
                    setUserName(savedUsername);
                } else {
                    const emailName = email ? email.split('@')[0] : 'User';
                    setUserName(emailName);
                    localStorage.setItem(usernameKey, emailName);
                }

                if (decoded.role === 'ADMIN') {
                    setIsAdmin(true);
                }
            } catch (e) {
                console.error("Ошибка токена (сброс сессии):", e);
                handleLogout();
            }
        } else {
            setUserEmail('');
            setUserName('');
            setIsAdmin(false);
        }
    }, [location.pathname]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (profileRef.current && !profileRef.current.contains(event.target)) {
                setShowMenu(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleLogout = () => {

        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');


        Object.keys(localStorage).forEach(key => {
            if (key.startsWith('username_')) {
                localStorage.removeItem(key);
            }
        });


        setUserEmail('');
        setUserName('');
        setIsAdmin(false);
        setShowMenu(false);
        setIsProfileOpen(false);


        navigate('/login');
    };

    const handleUpdateUser = (updatedData) => {
        if (updatedData.username) {
            setUserName(updatedData.username);
            const usernameKey = `username_${userEmail}`;
            localStorage.setItem(usernameKey, updatedData.username);
        }
        if (updatedData.email) {
            const oldUsernameKey = `username_${userEmail}`;
            const savedUsername = localStorage.getItem(oldUsernameKey);

            if (savedUsername) {
                const newUsernameKey = `username_${updatedData.email}`;
                localStorage.setItem(newUsernameKey, savedUsername);
                localStorage.removeItem(oldUsernameKey);
            }

            setUserEmail(updatedData.email);
        }
    };

    const handleBack = () => {
        if (location.pathname.startsWith('/session')) {
            navigate(-1);
        } else {
            navigate('/');
        }
    };

    const avatarLetter = userName ? userName.charAt(0).toUpperCase() : (userEmail ? userEmail.charAt(0).toUpperCase() : 'U');

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
        <>
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
                        <div className="profile-wrapper" ref={profileRef}>
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
                                <div className="profile-dropdown" style={{
                                    position: 'absolute',
                                    right: '50px',
                                    top: '70px',
                                    background: 'linear-gradient(135deg, #1a0a0a 0%, #0a0a0a 100%)',
                                    border: '2px solid rgba(255, 0, 0, 0.3)',
                                    borderRadius: '12px',
                                    padding: '15px',
                                    minWidth: '250px',
                                    zIndex: 1000,
                                    boxShadow: '0 8px 24px rgba(0, 0, 0, 0.8)',
                                    backdropFilter: 'blur(10px)'
                                }}>
                                    <div className="user-details" style={{
                                        paddingBottom: '10px',
                                        borderBottom: '1px solid rgba(255, 0, 0, 0.2)'
                                    }}>
                                        <div className="detail-row" style={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            marginBottom: '8px'
                                        }}>
                                            <span className="detail-label" style={{
                                                color: '#ff6b6b',
                                                fontSize: '14px',
                                                fontWeight: '600'
                                            }}>Аккаунт</span>
                                            <span className="detail-value" style={{
                                                color: '#ffffff',
                                                fontSize: '14px',
                                                fontWeight: 'bold'
                                            }}>{userName}</span>
                                        </div>
                                        <div className="detail-row" style={{
                                            display: 'flex',
                                            justifyContent: 'space-between'
                                        }}>
                                            <span className="detail-label" style={{
                                                color: '#ff6b6b',
                                                fontSize: '14px',
                                                fontWeight: '600'
                                            }}>Email</span>
                                            <span className="detail-value" title={userEmail} style={{
                                                color: '#ffffff',
                                                fontSize: '14px',
                                                maxWidth: '150px',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                whiteSpace: 'nowrap'
                                            }}>{userEmail}</span>
                                        </div>
                                    </div>

                                    <div className="menu-divider" style={{
                                        height: '1px',
                                        background: 'rgba(255, 0, 0, 0.2)',
                                        margin: '10px 0'
                                    }}></div>

                                    <div
                                        className="menu-item"
                                        onClick={() => {
                                            setIsProfileOpen(true);
                                            setShowMenu(false);
                                        }}
                                        style={{
                                            padding: '12px 15px',
                                            cursor: 'pointer',
                                            color: '#ffffff',
                                            borderRadius: '6px',
                                            transition: 'all 0.2s ease',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '10px'
                                        }}
                                        onMouseOver={(e) => {
                                            e.target.style.background = 'rgba(255, 0, 0, 0.15)';
                                        }}
                                        onMouseOut={(e) => {
                                            e.target.style.background = 'transparent';
                                        }}
                                    >
                                        ⚙️ Настройки профиля
                                    </div>

                                    <div className="menu-divider" style={{
                                        height: '1px',
                                        background: 'rgba(255, 0, 0, 0.2)',
                                        margin: '10px 0'
                                    }}></div>

                                    <div className="menu-item logout-item" onClick={handleLogout}
                                         style={{
                                             padding: '12px 15px',
                                             cursor: 'pointer',
                                             color: '#ff6b6b',
                                             borderRadius: '6px',
                                             transition: 'all 0.2s ease',
                                             display: 'flex',
                                             alignItems: 'center',
                                             gap: '10px',
                                             fontWeight: '600'
                                         }}
                                         onMouseOver={(e) => {
                                             e.target.style.background = 'rgba(255, 0, 0, 0.15)';
                                         }}
                                         onMouseOut={(e) => {
                                             e.target.style.background = 'transparent';
                                         }}
                                    >
                                        Выйти
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </header>

            <ProfileModal
                isOpen={isProfileOpen}
                onClose={() => setIsProfileOpen(false)}
                userEmail={userEmail}
                userName={userName}
                onLogout={handleLogout}
                onUpdateUser={handleUpdateUser}
            />
        </>
    );
};

export default Navbar;