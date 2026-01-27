import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

const ProfileModal = ({ isOpen, onClose, userEmail, userName, onLogout, onUpdateUser }) => {
    const [editMode, setEditMode] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [tempName, setTempName] = useState('');
    const [tempEmail, setTempEmail] = useState('');
    const [passForm, setPassForm] = useState({ current: '', new: '' });
    const [isEmailChanged, setIsEmailChanged] = useState(false);


    useEffect(() => {
        if (isOpen) {
            setEditMode(null);
            setMessage({});
            setTempName(userName || '');
            setTempEmail(userEmail || '');
            setPassForm({ current: '', new: '' });
            setIsEmailChanged(false);
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const handleSaveProfileField = async (field) => {

        if (field === 'username' && tempName.trim() === userName) {
            setEditMode(null);
            return;
        }

        setIsLoading(true);
        setMessage({});

        const payload = {
            username: field === 'username' ? tempName : userName,
            email: field === 'email' ? tempEmail : userEmail
        };

        try {
            await api.put('/users/profile', payload);
            setMessage({
                text: 'Данные успешно обновлены!',
                type: 'success'
            });

            if (onUpdateUser) {
                onUpdateUser({
                    username: field === 'username' ? tempName : userName,
                    email: field === 'email' ? tempEmail : userEmail
                });
            }

            setEditMode(null);

        } catch (err) {
            setMessage({
                text: err.response?.data?.message || 'Ошибка обновления',
                type: 'error'
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleChangePassword = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setMessage({});

        try {
            await api.put('/users/password', {
                currentPassword: passForm.current,
                newPassword: passForm.new
            });
            setMessage({
                text: 'Пароль успешно изменен!',
                type: 'success'
            });
            setEditMode(null);
            setPassForm({ current: '', new: '' });
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Ошибка смены пароля';
            const cleanErrorMessage = errorMessage
                .replace(/email/gi, '')
                .replace(/пароль/gi, 'пароль')
                .replace(/  +/g, ' ')
                .trim();

            setMessage({
                text: cleanErrorMessage || 'Неверный текущий пароль',
                type: 'error'
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleSaveEmail = async () => {

        if (tempEmail.trim() === userEmail) {
            setEditMode(null);
            return;
        }

        setIsLoading(true);
        setMessage({});

        try {
            await api.put('/users/profile', {
                username: userName,
                email: tempEmail
            });

            setMessage({
                text: 'Email успешно изменен!',
                type: 'success'
            });

            if (onUpdateUser) {
                onUpdateUser({
                    username: userName,
                    email: tempEmail
                });
            }

            setIsEmailChanged(true);

            setTimeout(() => {
                onLogout();
            }, 3000);

        } catch (err) {
            setMessage({
                text: err.response?.data?.message || 'Ошибка обновления email',
                type: 'error'
            });
        } finally {
            setIsLoading(false);
        }
    };

    const renderEditableRow = (label, value, fieldName, valueState, setValueState, isEmail = false) => {
        const isEditing = editMode === fieldName;

        const handleSave = () => {
            if (fieldName === 'email') {
                handleSaveEmail();
            } else {
                handleSaveProfileField(fieldName);
            }
        };

        const handleCancel = () => {
            setEditMode(null);
            setValueState(value);
        };

        const handleStartEdit = () => {
            setEditMode(fieldName);
            setValueState(value);
        };

        return (
            <div style={rowStyle}>
                <div style={fieldContentStyle}>
                    <div style={labelStyle}>{label}</div>

                    {!isEditing ? (
                        <div style={valueStyle}>{value}</div>
                    ) : (
                        <input
                            autoFocus
                            value={valueState}
                            onChange={(e) => setValueState(e.target.value)}
                            style={inputStyle}
                            disabled={isLoading || isEmailChanged}
                        />
                    )}
                </div>

                <div style={buttonWrapperStyle}>
                    {!isEditing ? (
                        <button
                            onClick={handleStartEdit}
                            style={editButtonStyle}
                            disabled={isLoading || isEmailChanged}
                        >
                            Изменить
                        </button>
                    ) : (
                        <div style={fieldName === 'password' ? passwordEditActionsStyle : editActionsStyle}>
                            <button
                                onClick={handleSave}
                                style={saveButtonStyle}
                                disabled={isLoading || isEmailChanged}
                            >
                                {isLoading ? '...' : '✓'}
                            </button>
                            <button
                                onClick={handleCancel}
                                style={cancelButtonStyle}
                                disabled={isLoading || isEmailChanged}
                            >
                                ✕
                            </button>
                        </div>
                    )}
                </div>
            </div>
        );
    };

    const isPasswordEditing = editMode === 'password';

    return (
        <div style={overlayStyle} onClick={isEmailChanged ? null : onClose}>
            <div style={modalStyle} onClick={e => e.stopPropagation()}>

                <div style={headerStyle}>
                    <div style={headerTitleStyle}>
                        <span style={iconStyle}>⚙️</span>
                        <h3 style={titleStyle}>Настройки аккаунта</h3>
                    </div>
                    {!isEmailChanged && (
                        <button
                            onClick={onClose}
                            style={closeBtnStyle}
                            aria-label="Закрыть"
                        >
                            &times;
                        </button>
                    )}
                </div>

                {message.text && (
                    <div style={{
                        ...alertStyle,
                        backgroundColor: message.type === 'success'
                            ? 'rgba(46, 204, 113, 0.1)'
                            : 'rgba(231, 76, 60, 0.1)',
                        borderColor: message.type === 'success' ? '#2ecc71' : '#e74c3c',
                        color: message.type === 'success' ? '#2ecc71' : '#e74c3c'
                    }}>
                        {message.text}
                        {isEmailChanged && (
                            <div style={{ marginTop: '5px', fontSize: '12px' }}>
                                Автоматический выход через 3 секунды...
                            </div>
                        )}
                    </div>
                )}

                <div style={contentStyle}>

                    {renderEditableRow(
                        "Имя пользователя",
                        userName,
                        "username",
                        tempName,
                        setTempName
                    )}

                    <div style={dividerStyle}></div>

                    {renderEditableRow(
                        "Email адрес",
                        userEmail,
                        "email",
                        tempEmail,
                        setTempEmail,
                        true
                    )}

                    {editMode === 'email' && !isEmailChanged && (
                        <div style={warningStyle}>
                            ⚠️ Смена email потребует повторного входа
                        </div>
                    )}

                    <div style={dividerStyle}></div>

                    <div style={passwordSectionStyle}>
                        <div style={passwordRowStyle}>
                            <div style={fieldContentStyle}>
                                <div style={labelStyle}>Пароль</div>
                                {!isPasswordEditing ? (
                                    <div style={passwordPlaceholderStyle}>••••••••••••••</div>
                                ) : null}
                            </div>

                            <div style={buttonWrapperStyle}>
                                {!isPasswordEditing ? (
                                    <button
                                        onClick={() => {
                                            setEditMode('password');
                                            setPassForm({ current: '', new: '' });
                                        }}
                                        style={editButtonStyle}
                                        disabled={isLoading || isEmailChanged}
                                    >
                                        Изменить
                                    </button>
                                ) : (
                                    <div style={passwordEditActionsStyle}>
                                        <button
                                            type="button"
                                            onClick={handleChangePassword}
                                            style={saveButtonStyle}
                                            disabled={isLoading || isEmailChanged}
                                        >
                                            {isLoading ? '...' : '✓'}
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setEditMode(null);
                                                setPassForm({ current: '', new: '' });
                                            }}
                                            style={cancelButtonStyle}
                                            disabled={isLoading || isEmailChanged}
                                        >
                                            ✕
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>

                        {isPasswordEditing && (
                            <form
                                onSubmit={handleChangePassword}
                                style={passwordFormStyle}
                            >
                                <div style={formGroupStyle}>
                                    <label style={formLabelStyle}>Текущий пароль</label>
                                    <input
                                        type="password"
                                        value={passForm.current}
                                        onChange={e => setPassForm({...passForm, current: e.target.value})}
                                        style={inputStyle}
                                        placeholder="Введите старый пароль"
                                        disabled={isLoading || isEmailChanged}
                                    />
                                </div>

                                <div style={formGroupStyle}>
                                    <label style={formLabelStyle}>Новый пароль</label>
                                    <input
                                        type="password"
                                        value={passForm.new}
                                        onChange={e => setPassForm({...passForm, new: e.target.value})}
                                        style={inputStyle}
                                        placeholder="Минимум 8 символов"
                                        disabled={isLoading || isEmailChanged}
                                    />
                                </div>
                            </form>
                        )}
                    </div>

                </div>
            </div>
        </div>
    );
};


const overlayStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0, 0, 0, 0.85)',
    backdropFilter: 'blur(8px)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2000,
    padding: '20px'
};

const modalStyle = {
    background: '#1a1a1a',
    width: '100%',
    maxWidth: '480px',
    padding: '32px',
    borderRadius: '16px',
    border: '1px solid #2a2a2a',
    boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)',
    animation: 'fadeIn 0.3s ease-out',
    position: 'relative'
};

const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '28px',
    position: 'relative'
};

const headerTitleStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '12px'
};

const iconStyle = {
    fontSize: '24px'
};

const titleStyle = {
    margin: 0,
    color: '#fff',
    fontSize: '20px',
    fontWeight: '600',
    letterSpacing: '-0.5px'
};

const closeBtnStyle = {
    background: 'transparent',
    border: 'none',
    color: '#888',
    fontSize: '28px',
    cursor: 'pointer',
    padding: '0',
    lineHeight: '1',
    transition: 'color 0.2s',
    position: 'absolute',
    right: '-10px',
    top: '-10px',
    width: '40px',
    height: '40px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    '&:hover': {
        color: '#fff',
        background: 'rgba(255, 255, 255, 0.1)',
        borderRadius: '50%'
    }
};

const contentStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px'
};

const rowStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '16px',
    minHeight: '60px'
};

const passwordRowStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '16px',
    minHeight: '60px'
};

const fieldContentStyle = {
    flex: 1,
    minWidth: 0
};

const buttonWrapperStyle = {
    flexShrink: 0,
    display: 'flex',
    alignItems: 'center'
};

const editActionsStyle = {
    display: 'flex',
    gap: '8px',
    marginTop: '18px'
};

const passwordEditActionsStyle = {
    display: 'flex',
    gap: '8px'
};

const labelStyle = {
    color: '#aaa',
    fontSize: '13px',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    marginBottom: '4px',
    fontWeight: '600',
    fontFamily: '-apple-system, BlinkMacSystemFont, sans-serif'
};

const valueStyle = {
    color: '#fff',
    fontSize: '16px',
    fontWeight: '500',
    lineHeight: '1.4'
};

const inputStyle = {
    width: '100%',
    padding: '10px 12px',
    background: '#252525',
    border: '1px solid #3a3a3a',
    color: '#fff',
    borderRadius: '8px',
    outline: 'none',
    fontSize: '15px',
    fontFamily: 'inherit',
    transition: 'border-color 0.2s',
    '&:focus': {
        borderColor: '#e50914'
    },
    '&:disabled': {
        opacity: 0.6,
        cursor: 'not-allowed'
    }
};

const dividerStyle = {
    height: '1px',
    background: 'linear-gradient(90deg, transparent, #333, transparent)',
    width: '100%',
    margin: '0'
};

const editButtonStyle = {
    background: 'transparent',
    color: '#e50914',
    border: '1px solid #e50914',
    padding: '6px 12px',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '13px',
    fontWeight: '600',
    transition: 'all 0.2s',
    fontFamily: 'inherit',
    whiteSpace: 'nowrap',
    height: '34px',
    display: 'flex',
    alignItems: 'center',
    '&:hover:not(:disabled)': {
        background: 'rgba(229, 9, 20, 0.1)'
    },
    '&:disabled': {
        opacity: 0.5,
        cursor: 'not-allowed'
    }
};

const saveButtonStyle = {
    background: '#e50914',
    color: '#fff',
    border: 'none',
    width: '34px',
    height: '34px',
    borderRadius: '6px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '14px',
    transition: 'all 0.2s',
    '&:hover:not(:disabled)': {
        background: '#f40612'
    },
    '&:disabled': {
        opacity: 0.6,
        cursor: 'not-allowed'
    }
};

const cancelButtonStyle = {
    background: '#2a2a2a',
    color: '#aaa',
    border: 'none',
    width: '34px',
    height: '34px',
    borderRadius: '6px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '14px',
    transition: 'all 0.2s',
    '&:hover:not(:disabled)': {
        background: '#333',
        color: '#fff'
    },
    '&:disabled': {
        opacity: 0.6,
        cursor: 'not-allowed'
    }
};

const alertStyle = {
    padding: '12px 16px',
    marginBottom: '24px',
    borderRadius: '8px',
    fontSize: '14px',
    border: '1px solid',
    textAlign: 'center',
    animation: 'slideDown 0.3s ease-out'
};

const warningStyle = {
    fontSize: '13px',
    color: '#e74c3c',
    marginTop: '-4px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontWeight: '500'
};

const passwordSectionStyle = {
    marginTop: '4px'
};

const passwordPlaceholderStyle = {
    color: '#666',
    fontSize: '16px',
    fontFamily: 'monospace',
    letterSpacing: '2px',
    padding: '4px 0',
    fontWeight: '500'
};

const passwordFormStyle = {
    background: '#252525',
    padding: '20px',
    borderRadius: '12px',
    border: '1px solid #333',
    marginTop: '16px'
};

const formGroupStyle = {
    marginBottom: '16px'
};

const formLabelStyle = {
    color: '#aaa',
    fontSize: '14px',
    marginBottom: '8px',
    display: 'block',
    fontWeight: '500'
};

const style = document.createElement('style');
style.textContent = `
    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @keyframes slideDown {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
`;
document.head.appendChild(style);

export default ProfileModal;