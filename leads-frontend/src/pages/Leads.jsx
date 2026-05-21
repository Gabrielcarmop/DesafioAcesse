import { useEffect, useState, useCallback } from 'react';
import { Search, RefreshCw, ChevronLeft, ChevronRight, User, Mail, Phone, Globe, Calendar } from 'lucide-react';
import { getLeads } from '../services/api';

function useDebounce(value, delay = 400) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

export default function LeadsPage() {
  const [leads, setLeads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [origem, setOrigem] = useState('');
  const debouncedSearch = useDebounce(search);
  const debouncedOrigem = useDebounce(origem);

  const load = useCallback(async (p = 0) => {
    setLoading(true);
    try {
      const params = { page: p, size: 15 };
      if (debouncedSearch) params.nome = debouncedSearch;
      if (debouncedOrigem) params.origem = debouncedOrigem;
      const res = await getLeads(params);
      const d = res.data;
      setLeads(d?.content ?? (Array.isArray(d) ? d : []));
      setTotalPages(d?.totalPages ?? 1);
      setTotalElements(d?.totalElements ?? 0);
    } catch (_) {}
    setLoading(false);
  }, [debouncedSearch, debouncedOrigem]);

  useEffect(() => { setPage(0); }, [debouncedSearch, debouncedOrigem]);
  useEffect(() => { load(page); }, [load, page]);

  const fmt = (v) => v ? new Date(v).toLocaleDateString('pt-BR') : '—';

  return (
    <div style={{ padding: '32px', display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.02em' }}>Leads</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginTop: 4 }}>
            {totalElements > 0 ? `${totalElements.toLocaleString('pt-BR')} leads encontrados` : 'Todos os leads importados'}
          </p>
        </div>
        <button onClick={() => load(page)} style={{
          display: 'flex', alignItems: 'center', gap: 6,
          background: 'var(--bg-3)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)', padding: '8px 14px',
          color: 'var(--text-secondary)', fontSize: 13, fontWeight: 500,
          transition: 'all var(--transition)',
        }}
        onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--border-hover)'; e.currentTarget.style.color = 'var(--text-primary)'; }}
        onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border)'; e.currentTarget.style.color = 'var(--text-secondary)'; }}
        >
          <RefreshCw size={13} className={loading ? 'spin' : ''} /> Atualizar
        </button>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 12 }}>
        <div style={{
          flex: 1, display: 'flex', alignItems: 'center', gap: 10,
          background: 'var(--bg-2)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)', padding: '0 14px',
          transition: 'border-color var(--transition)',
        }}
        onFocusCapture={e => e.currentTarget.style.borderColor = 'var(--accent)'}
        onBlurCapture={e => e.currentTarget.style.borderColor = 'var(--border)'}
        >
          <Search size={14} color="var(--text-muted)" style={{ flexShrink: 0 }} />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Buscar por nome ou e-mail…"
            style={{
              flex: 1, background: 'none', border: 'none', outline: 'none',
              color: 'var(--text-primary)', fontSize: 14, padding: '10px 0',
            }}
          />
          {search && (
            <button onClick={() => setSearch('')} style={{
              background: 'none', border: 'none', color: 'var(--text-muted)',
              cursor: 'pointer', fontSize: 14, lineHeight: 1,
            }}>✕</button>
          )}
        </div>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 10,
          background: 'var(--bg-2)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)', padding: '0 14px',
          transition: 'border-color var(--transition)',
        }}
        onFocusCapture={e => e.currentTarget.style.borderColor = 'var(--accent)'}
        onBlurCapture={e => e.currentTarget.style.borderColor = 'var(--border)'}
        >
          <Globe size={14} color="var(--text-muted)" />
          <input
            value={origem}
            onChange={e => setOrigem(e.target.value)}
            placeholder="Filtrar por origem…"
            style={{
              width: 160, background: 'none', border: 'none', outline: 'none',
              color: 'var(--text-primary)', fontSize: 14, padding: '10px 0',
            }}
          />
          {origem && (
            <button onClick={() => setOrigem('')} style={{
              background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: 14,
            }}>✕</button>
          )}
        </div>
      </div>

      {/* Table */}
      <div style={{
        background: 'var(--bg-2)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius-lg)', overflow: 'hidden',
      }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border)' }}>
              {[
                { label: 'Nome', icon: User },
                { label: 'E-mail', icon: Mail },
                { label: 'Telefone', icon: Phone },
                { label: 'Origem', icon: Globe },
                { label: 'Cadastro', icon: Calendar },
              ].map(({ label, icon: Icon }) => (
                <th key={label} style={{
                  padding: '12px 16px', textAlign: 'left',
                  fontSize: 11, fontWeight: 600, color: 'var(--text-muted)',
                  textTransform: 'uppercase', letterSpacing: '0.06em',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Icon size={11} /> {label}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading && Array.from({ length: 8 }).map((_, i) => (
              <tr key={i} style={{ borderBottom: '1px solid var(--border)' }}>
                {[140, 200, 110, 90, 90].map((w, j) => (
                  <td key={j} style={{ padding: '14px 16px' }}>
                    <div style={{
                      height: 12, borderRadius: 6, width: w,
                      background: 'linear-gradient(90deg, var(--bg-3) 25%, rgba(255,255,255,0.04) 50%, var(--bg-3) 75%)',
                      backgroundSize: '200% 100%',
                      animation: 'shimmer 1.4s ease-in-out infinite',
                      animationDelay: `${i * 0.06}s`,
                    }} />
                  </td>
                ))}
              </tr>
            ))}

            {!loading && leads.length === 0 && (
              <tr>
                <td colSpan={5} style={{ padding: '64px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
                    <User size={32} style={{ opacity: 0.2 }} />
                    <div>Nenhum lead encontrado</div>
                    {(search || origem) && (
                      <button onClick={() => { setSearch(''); setOrigem(''); }} style={{
                        background: 'none', border: 'none', color: 'var(--accent)',
                        fontSize: 13, cursor: 'pointer', marginTop: 4,
                      }}>Limpar filtros</button>
                    )}
                  </div>
                </td>
              </tr>
            )}

            {!loading && leads.map((lead, i) => (
              <tr key={lead.id ?? i} style={{
                borderBottom: '1px solid var(--border)',
                transition: 'background var(--transition)',
                animation: `fadeIn 0.2s ease ${i * 0.03}s both`,
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.02)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
              >
                <td style={{ padding: '14px 16px' }}>
                  <div style={{ fontWeight: 500, fontSize: 13, color: 'var(--text-primary)' }}>
                    {lead.nome ?? '—'}
                  </div>
                </td>
                <td style={{ padding: '14px 16px' }}>
                  <div style={{ fontSize: 13, color: 'var(--text-secondary)', fontFamily: 'var(--font-mono)', fontSize: 12 }}>
                    {lead.email ?? '—'}
                  </div>
                </td>
                <td style={{ padding: '14px 16px' }}>
                  <div style={{ fontSize: 13, color: 'var(--text-secondary)', fontFamily: 'var(--font-mono)', fontSize: 12 }}>
                    {lead.telefone ?? '—'}
                  </div>
                </td>
                <td style={{ padding: '14px 16px' }}>
                  {lead.origem ? (
                    <span style={{
                      fontSize: 11, fontWeight: 600, padding: '3px 8px', borderRadius: 99,
                      background: 'var(--purple-dim)', color: 'var(--purple)',
                      textTransform: 'uppercase', letterSpacing: '0.05em',
                    }}>
                      {lead.origem}
                    </span>
                  ) : '—'}
                </td>
                <td style={{ padding: '14px 16px', fontSize: 12, color: 'var(--text-muted)' }}>
                  {fmt(lead.dataCadastro ?? lead.data_cadastro)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Pagination */}
        {totalPages > 1 && (
          <div style={{
            padding: '12px 16px', borderTop: '1px solid var(--border)',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 13,
          }}>
            <span style={{ color: 'var(--text-muted)' }}>
              Página {page + 1} de {totalPages} · {totalElements.toLocaleString('pt-BR')} leads
            </span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                style={{
                  background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6,
                  padding: '6px 10px', color: page === 0 ? 'var(--text-muted)' : 'var(--text-primary)',
                  display: 'flex', alignItems: 'center', gap: 4, fontSize: 12,
                  opacity: page === 0 ? 0.5 : 1, cursor: page === 0 ? 'not-allowed' : 'pointer',
                }}
              >
                <ChevronLeft size={13} /> Anterior
              </button>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                style={{
                  background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6,
                  padding: '6px 10px', color: page >= totalPages - 1 ? 'var(--text-muted)' : 'var(--text-primary)',
                  display: 'flex', alignItems: 'center', gap: 4, fontSize: 12,
                  opacity: page >= totalPages - 1 ? 0.5 : 1, cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer',
                }}
              >
                Próxima <ChevronRight size={13} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
