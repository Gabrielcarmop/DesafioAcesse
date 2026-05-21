import { useState } from 'react';
import { LayoutDashboard, Upload, List, FileText, ChevronLeft, ChevronRight, Zap } from 'lucide-react';

const NAV = [
  { id: 'dashboard', label: 'Dashboard',    icon: LayoutDashboard },
  { id: 'upload',    label: 'Importar CSV', icon: Upload },
  { id: 'lotes',     label: 'Lotes',        icon: FileText },
  { id: 'leads',     label: 'Leads',        icon: List },
];

export default function Sidebar({ active, onNav }) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <aside style={{
      width: collapsed ? 60 : 'var(--sidebar-w)',
      minWidth: collapsed ? 60 : 'var(--sidebar-w)',
      background: 'var(--bg-2)',
      borderRight: '1px solid var(--border)',
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      position: 'sticky',
      top: 0,
      transition: 'width var(--transition), min-width var(--transition)',
      overflow: 'hidden',
      zIndex: 10,
    }}>
      {/* Logo */}
      <div style={{
        padding: collapsed ? '18px 0' : '18px 16px',
        display: 'flex',
        alignItems: 'center',
        gap: 10,
        borderBottom: '1px solid var(--border)',
        justifyContent: collapsed ? 'center' : 'flex-start',
      }}>
        <div style={{
          width: 30, height: 30, borderRadius: 8,
          background: 'var(--accent)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0,
        }}>
          <Zap size={15} color="#fff" strokeWidth={2.5} />
        </div>
        {!collapsed && (
          <div style={{ animation: 'slideIn 0.2s ease' }}>
            <div style={{ fontWeight: 700, fontSize: 14, color: 'var(--text-primary)', lineHeight: 1.2 }}>Leads Pro</div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>Importação</div>
          </div>
        )}
      </div>

      {/* Nav */}
      <nav style={{ flex: 1, padding: '10px 8px', display: 'flex', flexDirection: 'column', gap: 1 }}>
        {!collapsed && (
          <div style={{
            fontSize: 10, fontWeight: 600, color: 'var(--text-muted)',
            textTransform: 'uppercase', letterSpacing: '0.08em',
            padding: '6px 10px 8px',
          }}>
            Navegação
          </div>
        )}
        {NAV.map(({ id, label, icon: Icon }) => {
          const isActive = active === id;
          return (
            <button key={id} onClick={() => onNav(id)} style={{
              display: 'flex', alignItems: 'center', gap: 9,
              padding: collapsed ? '10px 0' : '9px 10px',
              justifyContent: collapsed ? 'center' : 'flex-start',
              borderRadius: 'var(--radius-sm)',
              border: 'none',
              background: isActive ? 'var(--accent-dim)' : 'transparent',
              color: isActive ? 'var(--accent)' : 'var(--text-secondary)',
              fontWeight: isActive ? 600 : 400,
              fontSize: 13.5,
              transition: 'all var(--transition)',
              position: 'relative',
              whiteSpace: 'nowrap',
            }}
            onMouseEnter={e => {
              if (!isActive) {
                e.currentTarget.style.background = 'var(--bg-3)';
                e.currentTarget.style.color = 'var(--text-primary)';
              }
            }}
            onMouseLeave={e => {
              if (!isActive) {
                e.currentTarget.style.background = 'transparent';
                e.currentTarget.style.color = 'var(--text-secondary)';
              }
            }}
            >
              {isActive && (
                <span style={{
                  position: 'absolute', left: 0, top: '18%', bottom: '18%',
                  width: 3, borderRadius: '0 3px 3px 0',
                  background: 'var(--accent)',
                }} />
              )}
              <Icon size={15} strokeWidth={isActive ? 2.5 : 2} />
              {!collapsed && label}
            </button>
          );
        })}
      </nav>

      {/* Collapse toggle */}
      <div style={{ padding: '10px 8px', borderTop: '1px solid var(--border)' }}>
        <button onClick={() => setCollapsed(c => !c)} style={{
          width: '100%', display: 'flex', alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'flex-start',
          gap: 8, padding: collapsed ? '9px 0' : '9px 10px',
          background: 'transparent', border: 'none',
          color: 'var(--text-muted)', borderRadius: 'var(--radius-sm)',
          fontSize: 13, transition: 'all var(--transition)',
        }}
        onMouseEnter={e => { e.currentTarget.style.background = 'var(--bg-3)'; e.currentTarget.style.color = 'var(--text-primary)'; }}
        onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)'; }}
        >
          {collapsed ? <ChevronRight size={15} /> : <><ChevronLeft size={15} /><span>Recolher</span></>}
        </button>
      </div>
    </aside>
  );
}
