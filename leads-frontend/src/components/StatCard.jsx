export default function StatCard({ label, value, sub, icon: Icon, color = 'var(--accent)', trend }) {
  const colorDim = color.replace('var(--', 'var(--').replace(')', '-dim)');

  return (
    <div style={{
      background: 'var(--bg-2)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius-lg)',
      padding: '20px 24px',
      display: 'flex',
      flexDirection: 'column',
      gap: 12,
      transition: 'border-color var(--transition)',
      animation: 'fadeIn 0.3s ease both',
    }}
    onMouseEnter={e => e.currentTarget.style.borderColor = 'var(--border-hover)'}
    onMouseLeave={e => e.currentTarget.style.borderColor = 'var(--border)'}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 13, color: 'var(--text-secondary)', fontWeight: 500 }}>{label}</span>
        {Icon && (
          <div style={{
            width: 36, height: 36, borderRadius: 'var(--radius-sm)',
            background: colorDim,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon size={16} color={color} strokeWidth={2} />
          </div>
        )}
      </div>
      <div>
        <div style={{ fontSize: 28, fontWeight: 700, letterSpacing: '-0.02em', color: 'var(--text-primary)', lineHeight: 1 }}>
          {value ?? '—'}
        </div>
        {sub && <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>{sub}</div>}
      </div>
      {trend !== undefined && (
        <div style={{
          fontSize: 12, color: trend >= 0 ? 'var(--green)' : 'var(--red)',
          display: 'flex', alignItems: 'center', gap: 4,
        }}>
          <span>{trend >= 0 ? '▲' : '▼'} {Math.abs(trend)}%</span>
          <span style={{ color: 'var(--text-muted)' }}>vs último lote</span>
        </div>
      )}
    </div>
  );
}
