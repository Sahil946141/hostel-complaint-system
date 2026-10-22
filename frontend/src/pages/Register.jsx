import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api';

export default function Register() {
  const [form, setForm] = useState({
    name: '', email: '', password: '', role: 'STUDENT', roomNumber: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authAPI.register(form);
      const { token, role, name, userId } = res.data;
      localStorage.setItem('token', token);
      localStorage.setItem('role', role);
      localStorage.setItem('name', name);
      localStorage.setItem('userId', userId);
      navigate(role === 'STUDENT' ? '/student' : '/admin');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-bg auth-container">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="logo-icon">🏠</div>
          <h1>Create Account</h1>
          <p>Join the Hostel CMS</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <input
              id="reg-name"
              type="text"
              name="name"
              placeholder="John Doe"
              value={form.name}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Email Address</label>
            <input
              id="reg-email"
              type="email"
              name="email"
              placeholder="your@email.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              id="reg-password"
              type="password"
              name="password"
              placeholder="Min 8 characters"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Role</label>
            <select id="reg-role" name="role" value={form.role} onChange={handleChange}>
              <option value="STUDENT">Student</option>
              <option value="STAFF">Staff</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          {form.role === 'STUDENT' && (
            <div className="form-group">
              <label>Room Number</label>
              <input
                id="reg-room"
                type="text"
                name="roomNumber"
                placeholder="e.g. A-101"
                value={form.roomNumber}
                onChange={handleChange}
              />
            </div>
          )}
          <button id="reg-btn" type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating account...' : '✨ Create Account'}
          </button>
        </form>

        <div className="auth-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
