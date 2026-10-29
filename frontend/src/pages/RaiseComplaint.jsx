import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { complaintAPI } from '../api';

export default function RaiseComplaint() {
  const [form, setForm] = useState({
    title: '', description: '', category: 'ELECTRICAL', priority: 'MEDIUM'
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((currentForm) => ({ ...currentForm, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setLoading(true);
    try {
      const res = await complaintAPI.create(form);
      setSuccess(`✅ Complaint raised! ID: ${res.data.complaintId}`);
      setTimeout(() => navigate('/my-complaints'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to raise complaint');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-layout">
      <nav className="navbar">
        <Link to="/student" className="navbar-brand">
          <div className="brand-icon">🏠</div>Hostel CMS
        </Link>
        <div className="navbar-links">
          <Link to="/student">Dashboard</Link>
          <Link to="/raise-complaint">Raise Complaint</Link>
          <Link to="/my-complaints">My Complaints</Link>
        </div>
      </nav>

      <div className="dashboard-content">
        <div className="page-header">
          <h1>📝 Raise a Complaint</h1>
          <p>Describe your issue and we'll get it resolved</p>
        </div>

        <div className="card" style={{ maxWidth: 600 }}>
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <form id="complaint-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Complaint Title</label>
              <input
                id="complaint-title"
                type="text"
                name="title"
                placeholder="e.g. Water not coming in Room A-101"
                value={form.title}
                onChange={handleChange}
                required
                minLength={5}
              />
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea
                id="complaint-description"
                name="description"
                placeholder="Describe the issue in detail..."
                value={form.description}
                onChange={handleChange}
                required
                minLength={10}
                rows={4}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="form-group">
                <label>Category</label>
                <select id="complaint-category" name="category" value={form.category} onChange={handleChange}>
                  <option value="ELECTRICAL">⚡ Electrical</option>
                  <option value="PLUMBING">🚿 Plumbing</option>
                  <option value="WIFI">📶 WiFi</option>
                  <option value="CLEANING">🧹 Cleaning</option>
                  <option value="FOOD">🍽️ Food</option>
                  <option value="SECURITY">🔐 Security</option>
                  <option value="FURNITURE">🪑 Furniture</option>
                  <option value="OTHER">📦 Other</option>
                </select>
              </div>

              <div className="form-group">
                <label>Priority</label>
                <select id="complaint-priority" name="priority" value={form.priority} onChange={handleChange}>
                  <option value="LOW">🟢 Low</option>
                  <option value="MEDIUM">🟡 Medium</option>
                  <option value="HIGH">🔴 High</option>
                  <option value="URGENT">🚨 Urgent</option>
                </select>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
              <button id="submit-complaint-btn" type="submit" className="btn btn-primary" disabled={loading}
                style={{ flex: 1 }}>
                {loading ? 'Submitting...' : '🚀 Submit Complaint'}
              </button>
              <Link to="/student" className="btn btn-secondary" style={{ flex: 0.4 }}>Cancel</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
