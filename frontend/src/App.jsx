import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import StudentDashboard from './pages/StudentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import RaiseComplaint from './pages/RaiseComplaint';
import MyComplaints from './pages/MyComplaints';
import ComplaintDetail from './pages/ComplaintDetail';
import './App.css';

function PrivateRoute({ children, allowedRoles }) {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  if (!token) return <Navigate to="/login" />;
  if (allowedRoles && !allowedRoles.includes(role)) return <Navigate to="/login" />;
  return children;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Student Routes */}
        <Route path="/student" element={
          <PrivateRoute allowedRoles={['STUDENT']}>
            <StudentDashboard />
          </PrivateRoute>
        } />
        <Route path="/raise-complaint" element={
          <PrivateRoute allowedRoles={['STUDENT']}>
            <RaiseComplaint />
          </PrivateRoute>
        } />
        <Route path="/my-complaints" element={
          <PrivateRoute allowedRoles={['STUDENT']}>
            <MyComplaints />
          </PrivateRoute>
        } />

        {/* Admin / Staff Routes */}
        <Route path="/admin" element={
          <PrivateRoute allowedRoles={['ADMIN', 'STAFF']}>
            <AdminDashboard />
          </PrivateRoute>
        } />
        <Route path="/complaint/:id" element={
          <PrivateRoute allowedRoles={['ADMIN', 'STAFF', 'STUDENT']}>
            <ComplaintDetail />
          </PrivateRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
