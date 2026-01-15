import { Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import HomePage from './pages/HomePage';

// Компонент-охранник
// Если токена нет -> выкидывает на логин
const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <Routes>
      {}
      <Route path="/login" element={<AuthPage />} />

      {}
      <Route path="/" element={
        <PrivateRoute>
          <HomePage />
        </PrivateRoute>
      } />
    </Routes>
  );
}

export default App;