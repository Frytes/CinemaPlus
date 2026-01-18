import { Navigate } from 'react-router-dom';

const AdminRoute = ({ children }) => {
    const token = localStorage.getItem('token');

    // 1. Нет токена? На выход.
    if (!token) {
        return <Navigate to="/login" replace />;
    }

    try {
        // 2. Декодируем токен (он состоит из 3 частей, payload посередине)
        const payloadBase64 = token.split('.')[1];
        const payloadJson = atob(payloadBase64);
        const payload = JSON.parse(payloadJson);

        // 3. Проверяем роль (мы положили её в поле "role")
        // Также проверяем срок действия (exp), чтобы не пускать с протухшим
        const now = Date.now() / 1000;

        if (payload.exp < now) {
             localStorage.removeItem('token');
             return <Navigate to="/login" replace />;
        }

        if (payload.role === 'ADMIN') {
            return children; // Пускаем!
        } else {
            return <Navigate to="/" replace />; // Юзерам тут не место
        }

    } catch (e) {
        // Если токен битый
        localStorage.removeItem('token');
        return <Navigate to="/login" replace />;
    }
};

export default AdminRoute;