import { useEffect, useState } from 'react';
import { RefreshCw, ChevronLeft, ChevronRight, Eye, Clock, CheckCircle, AlertCircle, Loader2, Hash, List } from 'lucide-react';
import { getLotes, getLoteStatus, getLoteChunks } from '../services/api';

const Badge = ({ status }) => {
  const map = {
    COMPLETED:   { color: 'var(--green)',  bg: 'var(--green-dim)',  label: 'Concluído' },
    PROCESSING:  { color: 'var(--accent)', bg: 'var(--accent-dim)', label: 'Processando' },
    FAILED:      { color: 'var(--red)',    bg: 'var(--red-dim)',    label: 'Erro' },
    PENDING:     { color: 'var(--yellow)', bg: 'var(--yellow-dim)', label: 'Pendente' },
    // aliases PT
    CONCLUIDO:   { color: 'var(--green)',  bg: 'var(--green-dim)',  label: 'Concluído' },
    PROCESSANDO: { color: 'var(--accent)', bg: 'var(--accent-dim)', label: 'Processando' },
    ERRO:        { color: 'var(--red)',    bg: 'var(--red-dim)',    label: 'Erro' },
    PENDENTE:    { color: 'var(--yellow)', bg: 'var(--yellow-dim)', label: 'Pendente' },
  };
  const s = map[status] ?? { color: 'var(--text-muted)', bg: 'var(--bg-3)', label: status };
  return (
    <span style={{
      fontSize: 11, fontWeight: 600, padding: '3px 8px', borderRadius: 99,
      background: s.bg, color: s.color, textTransform: 'uppercase', letterSpacing: '0.05em',
    }}>
      {s.label}
    </span>
  );
};

function ProgressBar({ value = 0, color = 'var(--accent)' }) {
  return (
    <div style={{ height: 4, background: 'var(--bg-3)', borderRadius: 99, overflow: 'hidden', minWidth: 80 }}>
      <div style={{
        height: '100%', width: `${Math.min(100, value)}%`,
        background: color === 'var(--accent)'
          ? 'linear-gradient(90deg, var(--accent), var(--purple))'
          : color,
        borderRadius: 99, transition: 'width 0.4s',
      }} />
    </div>
  );
}

function ChunksTable({ chunks }) {
  if (!chunks || chunks.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '16px 0', color: 'var(--text-muted)', fontSize: 13 }}>
        Nenhum chunk registrado ainda
      </div>
    );
  }
  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--border)' }}>
            {['Chunk', 'Total', 'Sucesso', 'Duplicados', 'Erros', 'Tempo (ms)'].map(h => (
              <th key={h} style={{
                padding: '6px 10px', textAlign: 'right', fontWeight: 600,
                color: 'var(--text-muted)', textTransform: 'uppercase', fontSize: 10,
                letterSpacing: '0.05em',
              }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {chunks.map((c, i) => (
            <tr key={c.id ?? i} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
              <td style={{ padding: '6px 10px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>#{c.chunkIndex}</td>
              <td style={{ padding: '6px 10px', textAlign: 'right', fontFamily: 'var(--font-mono)' }}>{c.totalLinhas?.toLocaleString('pt-BR')}</td>
              <td style={{ padding: '6px 10px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: 'var(--green)' }}>{c.linhasSucesso?.toLocaleString('pt-BR')}</td>
              <td style={{ padding: '6px 10px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: 'var(--yellow)' }}>{c.linhasDuplicadas?.toLocaleString('pt-BR')}</td>
              <td style={{ padding: '6px 10px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: c.linhasErro > 0 ? 'var(--red)' : 'var(--text-muted)' }}>{c.linhasErro?.toLocaleString('pt-BR')}</td>
              <td style={{ padding: '6px 10px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: 'var(--text-secondary)' }}>{c.tempoProcessamentoMs?.toLocaleString('pt-BR') ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function LoteDetail({ loteId, onClose }) {
  const [data, setData] = useState(null);
  const [chunks, setChunks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('resumo');

  useEffect(() => {
    let active = true;
    const fetchAll = async () => {
      try {
        const [statusRes, chunksRes] = await Promise.all([
          getLoteStatus(loteId),
          getLoteChunks(loteId).catch(() => ({ data: [] })),
        ]);
        if (!active) return;
        setData(statusRes.data);
        setChunks(Array.isArray(chunksRes.data) ? chunksRes.data : chunksRes.data?.chunks ?? []);
        setLoading(false);

        const st = statusRes.data?.status;
        if (st === 'PROCESSING' || st === 'PROCESSANDO' || st === 'PENDING' || st === 'PENDENTE') {
          setTimeout(fetchAll, 1800);
        }
      } catch (_) { setLoading(false); }
    };
    fetchAll();
    return () => { active = false; };
  }, [loteId]);

  const progress = data?.progressoPercentual ?? data?.porcentagem ?? data?.progresso ?? 0;
  const isProcessing = data?.status === 'PROCESSING' || data?.status === 'PROCESSANDO'
    || data?.status === 'PENDING' || data?.status === 'PENDENTE';

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.72)', zIndex: 100,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      animation: 'fadeIn 0.15s ease',
    }} onClick={onClose}>
      <div onClick={e => e.stopPropagation()} style={{
        background: 'var(--bg-2)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius-lg)', padding: 28, width: '100%', maxWidth: 560,
        animation: 'fadeIn 0.2s ease', maxHeight: '85vh', overflowY: 'auto',
      }}>
        {/* Header */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
          <div>
            <h3 style={{ fontWeight: 700, fontSize: 16 }}>Detalhes do Lote</h3>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginTop: 2 }}>#{loteId}</div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: 18 }}>✕</button>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '32px 0', color: 'var(--text-muted)' }}>
            <Loader2 size={24} className="spin" style={{ margin: '0 auto' }} />
          </div>
        ) : data ? (
          <>
            {/* Tabs */}
            <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'var(--bg-3)', borderRadius: 8, padding: 3 }}>
              {[
                { id: 'resumo', label: 'Resumo', icon: List },
                { id: 'chunks', label: `Chunks (${chunks.length})`, icon: Hash },
              ].map(({ id, label, icon: Icon }) => (
                <button key={id} onClick={() => setTab(id)} style={{
                  flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
                  padding: '7px 12px', borderRadius: 6, border: 'none', fontSize: 13, fontWeight: 500,
                  background: tab === id ? 'var(--bg-2)' : 'transparent',
                  color: tab === id ? 'var(--text-primary)' : 'var(--text-muted)',
                  transition: 'all var(--transition)',
                }}>
                  <Icon size={13} />{label}
                </button>
              ))}
            </div>

            {tab === 'resumo' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <Badge status={data.status} />
                  {isProcessing && <Loader2 size={14} color="var(--accent)" className="spin" />}
                  {data.nomeArquivo && (
                    <span style={{ fontSize: 12, color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {data.nomeArquivo}
                    </span>
                  )}
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-secondary)', marginBottom: 6 }}>
                    <span>{data.statusDescricao ?? 'Progresso'}</span>
                    <span>{progress.toFixed(1)}%</span>
                  </div>
                  <ProgressBar value={progress} />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 10 }}>
                  {[
                    { l: 'Total', v: data.totalLinhas, c: 'var(--text-primary)' },
                    { l: 'Sucesso', v: data.linhasSucesso, c: 'var(--green)' },
                    { l: 'Erros', v: data.linhasErro, c: 'var(--red)' },
                    { l: 'Chunks', v: chunks.length, c: 'var(--purple)' },
                  ].map(s => (
                    <div key={s.l} style={{ background: 'var(--bg-3)', borderRadius: 8, padding: '10px', textAlign: 'center' }}>
                      <div style={{ fontSize: 16, fontWeight: 700, color: s.c }}>{s.v?.toLocaleString('pt-BR') ?? 0}</div>
                      <div style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 2, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{s.l}</div>
                    </div>
                  ))}
                </div>

                {data.tempoTotalMs && (
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', display: 'flex', gap: 6, alignItems: 'center' }}>
                    <Clock size={11} /> Tempo total: {(data.tempoTotalMs / 1000).toFixed(2)}s
                  </div>
                )}
                {data.iniciadoEm && (
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', display: 'flex', gap: 6, alignItems: 'center' }}>
                    <CheckCircle size={11} /> Iniciado: {new Date(data.iniciadoEm).toLocaleString('pt-BR')}
                  </div>
                )}
                {data.finalizadoEm && (
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', display: 'flex', gap: 6, alignItems: 'center' }}>
                    <CheckCircle size={11} /> Finalizado: {new Date(data.finalizadoEm).toLocaleString('pt-BR')}
                  </div>
                )}
              </div>
            )}

            {tab === 'chunks' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
                  Registro de cada chunk processado pela tabela <code style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--accent)' }}>lote_processamento</code>
                </div>
                <div style={{ background: 'var(--bg-3)', borderRadius: 'var(--radius-sm)', overflow: 'hidden', border: '1px solid var(--border)' }}>
                  <ChunksTable chunks={chunks} />
                </div>
              </div>
            )}
          </>
        ) : (
          <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '24px 0' }}>Dados não encontrados</div>
        )}
      </div>
    </div>
  );
}

export default function LotesPage() {
  const [lotes, setLotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedId, setSelectedId] = useState(null);

  const load = async (p = 0) => {
    setLoading(true);
    try {
      const res = await getLotes(p, 10);
      const d = res.data;
      setLotes(d?.content ?? (Array.isArray(d) ? d : []));
      setTotalPages(d?.totalPages ?? 1);
    } catch (_) {}
    setLoading(false);
  };

  useEffect(() => { load(page); }, [page]);

  const fmt = (v) => v ? new Date(v).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—';

  return (
    <div style={{ padding: '32px', display: 'flex', flexDirection: 'column', gap: 24 }}>
      {selectedId && <LoteDetail loteId={selectedId} onClose={() => setSelectedId(null)} />}

      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.02em' }}>Lotes</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginTop: 4 }}>Histórico de importações</p>
        </div>
        <button onClick={() => load(page)} style={{
          display: 'flex', alignItems: 'center', gap: 6,
          background: 'var(--bg-3)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)', padding: '8px 14px',
          color: 'var(--text-secondary)', fontSize: 13, fontWeight: 500,
        }}>
          <RefreshCw size={13} className={loading ? 'spin' : ''} /> Atualizar
        </button>
      </div>

      <div style={{ background: 'var(--bg-2)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border)' }}>
              {['ID', 'Arquivo', 'Status', 'Total', 'Sucesso', 'Erros', 'Criado em', ''].map(h => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: 11, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading && Array.from({ length: 5 }).map((_, i) => (
              <tr key={i} style={{ borderBottom: '1px solid var(--border)' }}>
                {[40, 120, 70, 50, 50, 50, 80, 40].map((w, j) => (
                  <td key={j} style={{ padding: '14px 16px' }}>
                    <div style={{ height: 12, borderRadius: 6, width: w, background: 'var(--bg-3)', animation: 'shimmer 1.4s ease-in-out infinite', backgroundSize: '200% 100%' }} />
                  </td>
                ))}
              </tr>
            ))}
            {!loading && lotes.length === 0 && (
              <tr><td colSpan={8} style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>Nenhum lote encontrado</td></tr>
            )}
            {!loading && lotes.map((l, i) => (
              <tr key={l.id ?? i} style={{ borderBottom: '1px solid var(--border)', transition: 'background var(--transition)' }}
                onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.02)'}
                onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
              >
                <td style={{ padding: '14px 16px', fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--text-muted)' }}>
                  {String(l.id).slice(0, 8)}…
                </td>
                <td style={{ padding: '14px 16px', fontSize: 13, fontWeight: 500, maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{l.nomeArquivo ?? l.arquivo ?? '—'}</td>
                <td style={{ padding: '14px 16px' }}><Badge status={l.status} /></td>
                <td style={{ padding: '14px 16px', fontSize: 13, fontFamily: 'var(--font-mono)' }}>{l.totalLinhas?.toLocaleString('pt-BR') ?? '—'}</td>
                <td style={{ padding: '14px 16px', fontSize: 13, fontFamily: 'var(--font-mono)', color: 'var(--green)' }}>{l.linhasSucesso?.toLocaleString('pt-BR') ?? '—'}</td>
                <td style={{ padding: '14px 16px', fontSize: 13, fontFamily: 'var(--font-mono)', color: l.linhasErro > 0 ? 'var(--red)' : 'var(--text-muted)' }}>{l.linhasErro?.toLocaleString('pt-BR') ?? '—'}</td>
                <td style={{ padding: '14px 16px', fontSize: 12, color: 'var(--text-secondary)' }}>{fmt(l.createdAt ?? l.dataCriacao)}</td>
                <td style={{ padding: '14px 16px' }}>
                  <button onClick={() => setSelectedId(l.id)} style={{
                    background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6,
                    padding: '5px 8px', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: 4, fontSize: 12,
                    transition: 'all var(--transition)',
                  }}
                  onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--accent)'; e.currentTarget.style.color = 'var(--accent)'; }}
                  onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border)'; e.currentTarget.style.color = 'var(--text-secondary)'; }}
                  >
                    <Eye size={12} /> Ver
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {totalPages > 1 && (
          <div style={{ padding: '12px 16px', borderTop: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 13 }}>
            <span style={{ color: 'var(--text-muted)' }}>Página {page + 1} de {totalPages}</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} style={{
                background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6,
                padding: '6px 10px', display: 'flex', alignItems: 'center', gap: 4, fontSize: 12,
                opacity: page === 0 ? 0.5 : 1, cursor: page === 0 ? 'not-allowed' : 'pointer',
                color: page === 0 ? 'var(--text-muted)' : 'var(--text-primary)',
              }}>
                <ChevronLeft size={13} /> Anterior
              </button>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} style={{
                background: 'var(--bg-3)', border: '1px solid var(--border)', borderRadius: 6,
                padding: '6px 10px', display: 'flex', alignItems: 'center', gap: 4, fontSize: 12,
                opacity: page >= totalPages - 1 ? 0.5 : 1, cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer',
                color: page >= totalPages - 1 ? 'var(--text-muted)' : 'var(--text-primary)',
              }}>
                Próxima <ChevronRight size={13} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
