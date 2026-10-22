import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { complaintAPI } from '../api';

export default function ComplaintDetail() {
  const { id } = useParams();
  const [complaint, setComplaint] = useState(null);
  const [loading, setLoading] = useState(true);
  const role = localStorage.getItem('role');

  useEffect(() => {
    complaintAPI.getById(id)
      .then(res => setComplaint(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="page-bg" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}><div className="spinner" /></div>;
  if (!complaint) return <div className="page-bg auth-container"><p>Complaint not found</p></div>;

  const getBadge = (val) => `badge badge-${val.toLowerCase()}`;

  return (
    <div className="page-bg" style={{ minHeight: '100vh', padding: '2rem' }}>
      <div style={{ maxWidth: 700, margin: '0 auto' }}>
        <div style={{ marginBottom: '1.5rem' }}>
          <Link to={role === 'STUDENT' ? '/my-complaints' : '/admin'} className="btn btn-secondary btn-sm">
            ← Back
          </Link>
        </div>

        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
            <div>
              <code style={{ color: 'var(--primary-light)', fontSize: '0.8rem' }}>{complaint.complaintId}</code>
              <h1 style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '0.5rem' }}>{complaint.title}</h1>
            </div>
            <span className={getBadge(complaint.status)}>{complaint.status.replace('_', ' ')}</span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
            {[
              ['Category', complaint.category],
              ['Priority', <span className={getBadge(complaint.priority)}>{complaint.priority}</span>],
              ['Raised By', complaint.studentName],
              ['Assigned To', complaint.assignedToName || 'Not assigned'],
              ['Created', new Date(complaint.createdAt).toLocaleString()],
              ['Updated', new Date(complaint.updatedAt).toLocaleString()],
            ].map(([label, value]) => (
              <div key={label}>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '0.25rem' }}>{label}</p>
                <p style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{value}</p>
              </div>
            ))}
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '0.5rem' }}>Description</p>
            <p style={{ color: 'var(--text-secondary)', lineHeight: 1.7, background: 'var(--bg-surface)', padding: '1rem', borderRadius: 12, border: '1px solid var(--border)' }}>
              {complaint.description}
            </p>
          </div>

          {complaint.adminRemarks && (
            <div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '0.5rem' }}>Admin Remarks</p>
              <p style={{ color: 'var(--success)', background: 'rgba(0,230,118,0.05)', padding: '1rem', borderRadius: 12, border: '1px solid rgba(0,230,118,0.2)' }}>
                {complaint.adminRemarks}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
