import { Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import HomePage from './pages/HomePage';
import MoviePage from './pages/MoviePage';
import HallsPage from './pages/HallsPage';
import AdminPage from './pages/AdminPage';
import SessionPage from './pages/SessionPage';
import MyTicketsPage from './pages/MyTicketsPage'; // <--- НЕ ЗАБУДЬ ИМПОРТ
import AdminRoute from './components/AdminRoute';

// Компонент для защиты приватных маршрутов (только для залогиненных)
const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  // Если токена нет -> редирект на логин
  return token ? children : <Navigate to="/login" replace />;
};

function App() {
  return (
    <Routes>
      <Route path="/login" element={<AuthPage />} />
      <Route path="/" element={<HomePage />} />
      <Route path="/movie/:id" element={<MoviePage />} />
      <Route path="/halls" element={<HallsPage />} />


      <Route path="/session/:id" element={
          <PrivateRoute>
              <SessionPage />
          </PrivateRoute>
      } />

      <Route path="/tickets" element={
          <PrivateRoute>
              <MyTicketsPage />
          </PrivateRoute>
      } />


      <Route path="/admin" element={
          <AdminRoute>
              <AdminPage />
          </AdminRoute>
      } />
    </Routes>
  );
}

export default App;