import { Navigate } from 'react-router-dom';
import { jwtDecode } from "jwt-decode"; //

const AdminRoute = ({ children }) => {
    const token = localStorage.getItem('token');

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    try {
        const payload = jwtDecode(token);

        const now = Date.now() / 1000;
        if (payload.exp < now) {
             localStorage.removeItem('token');
             return <Navigate to="/login" replace />;
        }

        if (payload.role === 'ADMIN') {
            return children;
        } else {
            return <Navigate to="/" replace />;
        }

    } catch (e) {
        localStorage.removeItem('token');
        return <Navigate to="/login" replace />;
    }
};

export default AdminRoute;