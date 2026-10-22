import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { complaintAPI } from '../api';

function Navbar() {
  const navigate = useNavigate();
  const name = localStorage.getItem('name');
  const role = localStorage.getItem('role');

  const logout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/student" className="navbar-brand">
        <div className="brand-icon">🏠</div>
        Hostel CMS
      </Link>
      <div className="navbar-links">
        <Link to="/student">Dashboard</Link>
        <Link to="/raise-complaint">Raise Complaint</Link>
        <Link to="/my-complaints">My Complaints</Link>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div className="user-badge">
          👤 {name} <span className="role-tag">{role}</span>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={logout}>Logout</button>
      </div>
    </nav>
  );
}

export default function StudentDashboard() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    complaintAPI.getMy()
      .then(res => setComplaints(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const stats = {
    total: complaints.length,
    pending: complaints.filter(c => c.status === 'PENDING').length,
    inProgress: complaints.filter(c => c.status === 'IN_PROGRESS').length,
    resolved: complaints.filter(c => c.status === 'RESOLVED').length,
  };

  const getBadgeClass = (val) => `badge badge-${val.toLowerCase().replace('_', '_')}`;

  return (
    <div className="dashboard-layout">
      <Navbar />
      <div className="dashboard-content">
        <div className="page-header">
          <h1>👋 My Dashboard</h1>
          <p>Track all your hostel complaints</p>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon purple">📋</div>
            <div className="stat-info"><h3>{stats.total}</h3><p>Total Complaints</p></div>
          </div>
          <div className="stat-card">
            <div className="stat-icon yellow">⏳</div>
            <div className="stat-info"><h3>{stats.pending}</h3><p>Pending</p></div>
          </div>
          <div className="stat-card">
            <div className="stat-icon blue">🔧</div>
            <div className="stat-info"><h3>{stats.inProgress}</h3><p>In Progress</p></div>
          </div>
          <div className="stat-card">
            <div className="stat-icon green">✅</div>
            <div className="stat-info"><h3>{stats.resolved}</h3><p>Resolved</p></div>
          </div>
        </div>

        <div className="complaints-table-wrapper">
          <div className="complaints-table-header">
            <h2>Recent Complaints</h2>
            <Link to="/raise-complaint" className="btn btn-primary btn-sm">+ Raise Complaint</Link>
          </div>

          {loading ? <div className="spinner" /> : (
            <table className="complaints-table" id="student-complaints-table">
              <thead>
                <tr>
                  <th>Complaint ID</th>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {complaints.length === 0 ? (
                  <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    No complaints yet. <Link to="/raise-complaint" style={{ color: 'var(--primary-light)' }}>Raise your first complaint</Link>
                  </td></tr>
                ) : complaints.map(c => (
                  <tr key={c.id}>
                    <td><code style={{ color: 'var(--primary-light)', fontSize: '0.75rem' }}>{c.complaintId}</code></td>
                    <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{c.title}</td>
                    <td>{c.category}</td>
                    <td><span className={getBadgeClass(c.priority)}>{c.priority}</span></td>
                    <td><span className={getBadgeClass(c.status)}>{c.status.replace('_', ' ')}</span></td>
                    <td>{new Date(c.createdAt).toLocaleDateString()}</td>
                    <td><Link to={`/complaint/${c.id}`} className="btn btn-secondary btn-sm">View</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
