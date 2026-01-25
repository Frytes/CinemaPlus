import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const AuthPage = () => {
    const navigate = useNavigate();
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

        const endpoint = isLogin ? '/auth/login' : '/auth/register';
        const payload = isLogin ? { email, password } : { username, email, password };

        try {
            const response = await api.post(endpoint, payload);
            console.log("Ответ сервера:", response.data);

            const accessToken = response.data.token || response.data.accessToken;
            const refreshToken = response.data.refreshToken;

            if (!accessToken) {
                throw new Error("Сервер не прислал токен!");
            }

            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);

            setMessage(isLogin ? 'Вход выполнен!' : 'Регистрация успешна! Вход...');
            setTimeout(() => {
                navigate('/');
            }, 500);

        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || err.message || 'Ошибка сервера';
            setError(msg);
        }
    };

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
            width: '100%'
        }}>
            <div className="auth-container">
                <h2>{isLogin ? 'Вход' : 'Регистрация'}</h2>

                <form onSubmit={handleSubmit}>
                    {!isLogin && (
                        <div className="form-group">
                            <label>Имя пользователя</label>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required={!isLogin}
                            />
                        </div>
                    )}

                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Пароль</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
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
                    <span onClick={() => { setIsLogin(!isLogin); setMessage(''); setError(''); }}>
                        {isLogin ? 'Зарегистрироваться' : 'Войти'}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default AuthPage;