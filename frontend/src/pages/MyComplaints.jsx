import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { complaintAPI } from '../api';

export default function MyComplaints() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    complaintAPI.getMy()
      .then(res => setComplaints(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const filtered = filter === 'ALL'
    ? complaints
    : complaints.filter(c => c.status === filter);

  const getBadgeClass = (val) => `badge badge-${val.toLowerCase()}`;

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
        <button className="btn btn-secondary btn-sm" onClick={() => { localStorage.clear(); navigate('/login'); }}>
          Logout
        </button>
      </nav>

      <div className="dashboard-content">
        <div className="page-header">
          <h1>📋 My Complaints</h1>
          <p>Track the status of all your complaints</p>
        </div>

        {/* Filter Tabs */}
        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
          {['ALL', 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'].map(s => (
            <button
              key={s}
              id={`filter-${s.toLowerCase()}`}
              className={`btn btn-sm ${filter === s ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setFilter(s)}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>

        <div className="complaints-table-wrapper">
          <div className="complaints-table-header">
            <h2>Complaints ({filtered.length})</h2>
            <Link to="/raise-complaint" className="btn btn-primary btn-sm">+ New</Link>
          </div>

          {loading ? <div className="spinner" /> : (
            <table className="complaints-table" id="my-complaints-table">
              <thead>
                <tr>
                  <th>ID</th><th>Title</th><th>Category</th>
                  <th>Priority</th><th>Status</th><th>Assigned To</th><th>Date</th><th></th>
                </tr>
              </thead>
              <tbody>
                {filtered.length === 0 ? (
                  <tr><td colSpan={8} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    No complaints found
                  </td></tr>
                ) : filtered.map(c => (
                  <tr key={c.id}>
                    <td><code style={{ color: 'var(--primary-light)', fontSize: '0.7rem' }}>{c.complaintId}</code></td>
                    <td style={{ color: 'var(--text-primary)', fontWeight: 500, maxWidth: 200 }}>{c.title}</td>
                    <td>{c.category}</td>
                    <td><span className={getBadgeClass(c.priority)}>{c.priority}</span></td>
                    <td><span className={getBadgeClass(c.status)}>{c.status.replace('_',' ')}</span></td>
                    <td>{c.assignedToName || '—'}</td>
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
