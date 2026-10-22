import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { complaintAPI, adminAPI } from '../api';

function Navbar() {
  const navigate = useNavigate();
  const name = localStorage.getItem('name');
  const role = localStorage.getItem('role');
  return (
    <nav className="navbar">
      <Link to="/admin" className="navbar-brand">
        <div className="brand-icon">🏠</div>Hostel CMS Admin
      </Link>
      <div className="navbar-links">
        <Link to="/admin">Dashboard</Link>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div className="user-badge">👤 {name} <span className="role-tag">{role}</span></div>
        <button className="btn btn-secondary btn-sm" onClick={() => { localStorage.clear(); navigate('/login'); }}>
          Logout
        </button>
      </div>
    </nav>
  );
}

export default function AdminDashboard() {
  const [stats, setStats] = useState({});
  const [complaints, setComplaints] = useState([]);
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [updateData, setUpdateData] = useState({ status: '', remarks: '', assignedToId: '' });
  const [selectedId, setSelectedId] = useState(null);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    Promise.all([adminAPI.getDashboard(), complaintAPI.getAll(), adminAPI.getAllStaff()])
      .then(([statsRes, compRes, staffRes]) => {
        setStats(statsRes.data);
        setComplaints(compRes.data);
        setStaff(staffRes.data);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const filtered = filter === 'ALL' ? complaints : complaints.filter(c => c.status === filter);
  const getBadge = (val) => `badge badge-${val.toLowerCase()}`;

  const handleUpdate = async (id) => {
    try {
      await complaintAPI.updateStatus(id, {
        status: updateData.status,
        remarks: updateData.remarks,
        assignedToId: updateData.assignedToId || null
      });
      setMsg('✅ Status updated!');
      setSelectedId(null);
      const res = await complaintAPI.getAll();
      setComplaints(res.data);
      const statsRes = await adminAPI.getDashboard();
      setStats(statsRes.data);
      setTimeout(() => setMsg(''), 3000);
    } catch (err) {
      setMsg('❌ Update failed');
    }
  };

  return (
    <div className="dashboard-layout">
      <Navbar />
      <div className="dashboard-content">
        <div className="page-header">
          <h1>🛡️ Admin Dashboard</h1>
          <p>Manage and resolve all hostel complaints</p>
        </div>

        {/* Stats */}
        <div className="stats-grid">
          {[
            { icon: '📋', color: 'purple', val: stats.totalComplaints, label: 'Total Complaints' },
            { icon: '⏳', color: 'yellow', val: stats.pendingComplaints, label: 'Pending' },
            { icon: '🔧', color: 'blue', val: stats.inProgressComplaints, label: 'In Progress' },
            { icon: '✅', color: 'green', val: stats.resolvedComplaints, label: 'Resolved' },
            { icon: '👥', color: 'purple', val: stats.totalStudents, label: 'Students' },
            { icon: '🔧', color: 'blue', val: stats.totalStaff, label: 'Staff' },
          ].map((s, i) => (
            <div className="stat-card" key={i}>
              <div className={`stat-icon ${s.color}`}>{s.icon}</div>
              <div className="stat-info"><h3>{s.val ?? '—'}</h3><p>{s.label}</p></div>
            </div>
          ))}
        </div>

        {/* Complaints Table */}
        {msg && <div className={`alert ${msg.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>{msg}</div>}

        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
          {['ALL', 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'].map(s => (
            <button key={s} id={`admin-filter-${s.toLowerCase()}`}
              className={`btn btn-sm ${filter === s ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setFilter(s)}>
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>

        <div className="complaints-table-wrapper">
          <div className="complaints-table-header">
            <h2>All Complaints ({filtered.length})</h2>
          </div>
          {loading ? <div className="spinner" /> : (
            <table className="complaints-table" id="admin-complaints-table">
              <thead>
                <tr>
                  <th>ID</th><th>Title</th><th>Student</th><th>Category</th>
                  <th>Priority</th><th>Status</th><th>Assigned</th><th>Date</th><th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(c => (
                  <>
                    <tr key={c.id}>
                      <td><code style={{ color: 'var(--primary-light)', fontSize: '0.7rem' }}>{c.complaintId}</code></td>
                      <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{c.title}</td>
                      <td>{c.studentName}</td>
                      <td>{c.category}</td>
                      <td><span className={getBadge(c.priority)}>{c.priority}</span></td>
                      <td><span className={getBadge(c.status)}>{c.status.replace('_', ' ')}</span></td>
                      <td>{c.assignedToName || '—'}</td>
                      <td>{new Date(c.createdAt).toLocaleDateString()}</td>
                      <td>
                        <button id={`update-btn-${c.id}`} className="btn btn-secondary btn-sm"
                          onClick={() => setSelectedId(selectedId === c.id ? null : c.id)}>
                          {selectedId === c.id ? 'Close' : 'Update'}
                        </button>
                      </td>
                    </tr>
                    {selectedId === c.id && (
                      <tr key={`update-${c.id}`}>
                        <td colSpan={9} style={{ background: 'var(--bg-surface)', padding: '1rem 1.5rem' }}>
                          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                            <div style={{ flex: 1, minWidth: 150 }}>
                              <label style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', display: 'block', marginBottom: '0.375rem' }}>New Status</label>
                              <select id={`status-select-${c.id}`} value={updateData.status}
                                onChange={e => setUpdateData({ ...updateData, status: e.target.value })}
                                style={{ width: '100%', padding: '0.625rem', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--text-primary)', fontFamily: 'Inter' }}>
                                <option value="">Select status</option>
                                <option value="PENDING">Pending</option>
                                <option value="IN_PROGRESS">In Progress</option>
                                <option value="RESOLVED">Resolved</option>
                                <option value="REJECTED">Rejected</option>
                              </select>
                            </div>
                            <div style={{ flex: 1, minWidth: 150 }}>
                              <label style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', display: 'block', marginBottom: '0.375rem' }}>Assign To</label>
                              <select id={`assign-select-${c.id}`} value={updateData.assignedToId}
                                onChange={e => setUpdateData({ ...updateData, assignedToId: e.target.value })}
                                style={{ width: '100%', padding: '0.625rem', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--text-primary)', fontFamily: 'Inter' }}>
                                <option value="">Select staff</option>
                                {staff.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                              </select>
                            </div>
                            <div style={{ flex: 2, minWidth: 200 }}>
                              <label style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', display: 'block', marginBottom: '0.375rem' }}>Remarks</label>
                              <input type="text" id={`remarks-input-${c.id}`} value={updateData.remarks}
                                onChange={e => setUpdateData({ ...updateData, remarks: e.target.value })}
                                placeholder="Add remarks..."
                                style={{ width: '100%', padding: '0.625rem', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--text-primary)', fontFamily: 'Inter' }}
                              />
                            </div>
                            <button id={`save-update-${c.id}`} className="btn btn-primary btn-sm"
                              onClick={() => handleUpdate(c.id)} disabled={!updateData.status}>
                              Save
                            </button>
                          </div>
                        </td>
                      </tr>
                    )}
                  </>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
