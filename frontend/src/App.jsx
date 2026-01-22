import { Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import HomePage from './pages/HomePage';
import MoviePage from './pages/MoviePage';
import HallsPage from './pages/HallsPage';
import AdminPage from './pages/AdminPage';
import AdminRoute from './components/AdminRoute';
import SessionPage from './pages/SessionPage';
import MyTicketPage from './pages/MyTicketPage';


const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <Routes>
      <Route path="/login" element={<AuthPage />} />
      <Route path="/" element={<HomePage />} />
      <Route path="/movie/:id" element={<MoviePage />} />
      <Route path="/halls" element={<HallsPage />} />
      <Route path="/admin" element={
          <AdminRoute>
              <AdminPage />
          </AdminRoute>
      } />
      <Route path="/session/:id" element={<SessionPage />} />
    </Routes>
  );
}

export default App;