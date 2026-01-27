import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axiosConfig';

const AuthPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isLogin, setIsLogin] = useState(true);

    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');

        if (!isLogin) {
             if (username.length < 2) {
                  setError('Имя должно быть не короче 2 символов');
                  return;
             }
             if (username.length > 50) {
                  setError('Имя должно быть не длиннее 50 символов');
                  return;
             }
             const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
             if (!emailRegex.test(email)) {
                  setError('Некорректный формат email');
                  return;
             }
             if (password.length < 8) {
                  setError('Пароль должен быть не менее 8 символов');
                  return;
             }

             const passwordRegex = /^[a-zA-Z0-9!@#$%^&*()_+\-=]+$/;
             if (!passwordRegex.test(password)) {
                  setError('Пароль содержит недопустимые символы');
                  return;
             }
        }

        const endpoint = isLogin ? '/auth/login' : '/auth/register';
        const payload = isLogin ? { email, password } : { username, email, password };

        try {
            const response = await api.post(endpoint, payload);
            const { accessToken, refreshToken } = response.data; // Исправил деструктуризацию

            if (!accessToken) throw new Error("Сервер не прислал токен!");

            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);

            setMessage(isLogin ? 'Вход выполнен!' : 'Регистрация успешна! Вход...');

            setTimeout(() => {
                const origin = location.state?.from?.pathname || '/';
                navigate(origin, { replace: true });
            }, 500);

        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || err.message || 'Ошибка сервера';
            setError(msg);
        }
    };

    const handleSwitchMode = () => {
        setIsLogin(!isLogin);
        setMessage('');
        setError('');
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', width: '100%' }}>
            <div className="auth-container">
                <h2>{isLogin ? 'Вход' : 'Регистрация'}</h2>

                {/* noValidate отключает встроенные тултипы браузера */}
                <form onSubmit={handleSubmit} noValidate>
                    {!isLogin && (
                        <div className="form-group">
                            <label>Имя пользователя</label>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => { setUsername(e.target.value); setError(''); }}
                                required={!isLogin}
                            />
                        </div>
                    )}

                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => { setEmail(e.target.value); setError(''); }}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Пароль</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => { setPassword(e.target.value); setError(''); }}
                            required
                        />
                    </div>

                    <button type="submit">
                        {isLogin ? 'Войти' : 'Создать аккаунт'}
                    </button>
                </form>

                {error && <div className="error-msg">{error}</div>}
                {message && <div style={{color: '#4caf50', marginTop: '10px'}}>{message}</div>}

                <div className="toggle-link">
                    {isLogin ? 'Нет аккаунта? ' : 'Уже есть аккаунт? '}
                    <span onClick={handleSwitchMode}>
                        {isLogin ? 'Зарегистрироваться' : 'Войти'}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default AuthPage;