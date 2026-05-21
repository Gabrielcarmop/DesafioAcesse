import { useState, useRef, useEffect } from 'react';
import { Upload, FileText, CheckCircle, AlertCircle, X, Loader2, Zap } from 'lucide-react';
import { uploadLote, getLoteStatus } from '../services/api';

const STATUS_LABEL = {
  PENDING:    'Aguardando na fila…',
  PROCESSING: 'Processando chunks…',
  COMPLETED:  'Concluído',
  FAILED:     'Falhou',
};

const isDone   = (s) => s === 'COMPLETED';
const isFailed = (s) => s === 'FAILED';
const isActive = (s) => s === 'PENDING' || s === 'PROCESSING';

export default function UploadPage({ onNavigate }) {
  const [dragging,   setDragging]   = useState(false);
  const [file,       setFile]       = useState(null);
  const [uploadPct,  setUploadPct]  = useState(0);
  const [phase,      setPhase]      = useState('idle'); // idle | uploading | polling | done | failed
  const [loteId,     setLoteId]     = useState(null);
  const [loteStatus, setLoteStatus] = useState(null);
  const [errorMsg,   setErrorMsg]   = useState('');

  const inputRef   = useRef(null);
  const pollRef    = useRef(null);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      clearTimeout(pollRef.current);
    };
  }, []);

  const reset = () => {
    clearTimeout(pollRef.current);
    setFile(null); setUploadPct(0); setPhase('idle');
    setLoteId(null); setLoteStatus(null); setErrorMsg('');
  };

  const runPoll = async (id) => {
    if (!mountedRef.current) return;
    try {
      const res = await getLoteStatus(id);
      const data = res.data;
      if (!mountedRef.current) return;
      setLoteStatus(data);
      if (isDone(data?.status))        setPhase('done');
      else if (isFailed(data?.status)) setPhase('failed');
      else pollRef.current = setTimeout(() => runPoll(id), 1500);
    } catch (_) {
      if (mountedRef.current)
        pollRef.current = setTimeout(() => runPoll(id), 2000);
    }
  };

  const handleFile = (f) => {
    if (!f) return;
    if (!f.name.toLowerCase().endsWith('.csv')) {
      setErrorMsg('Apenas arquivos .csv são aceitos.');
      return;
    }
    setFile(f);
    setErrorMsg('');
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    handleFile(e.dataTransfer.files[0]);
  };

  const handleSubmit = async () => {
    if (!file) return;
    setPhase('uploading');
    setUploadPct(0);
    try {
      const res = await uploadLote(file, setUploadPct);
      const id = res.data?.id ?? res.data;
      setLoteId(id);
      setLoteStatus(null);
      setPhase('polling');
      runPoll(id);
    } catch (e) {
      setErrorMsg(e.response?.data?.message ?? 'Erro ao enviar o arquivo.');
      setPhase('idle');
    }
  };

  const progress    = loteStatus?.progressoPercentual ?? 0;
  const total       = loteStatus?.totalLinhas         ?? 0;
  const processadas = loteStatus?.linhasProcessadas   ?? 0;
  const ok          = loteStatus?.linhasSucesso       ?? 0;
  const erros       = loteStatus?.linhasErro          ?? 0;
  const statusLabel = STATUS_LABEL[loteStatus?.status] ?? 'Aguardando…';

  return (
    <div style={{ padding: '32px', maxWidth: 660, margin: '0 auto', display: 'flex', flexDirection: 'column', gap: 22 }}>

      <div>
        <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.02em' }}>Importar CSV</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginTop: 4 }}>
          Faça upload de um arquivo CSV com até 100 mil linhas de leads
        </p>
      </div>

      <div style={{
        background: 'var(--accent-dim)', border: '1px solid rgba(66,99,235,0.18)',
        borderRadius: 'var(--radius-sm)', padding: '9px 14px',
        display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: 'var(--accent)',
      }}>
        <Zap size={13} />
        <span>Colunas esperadas: <code style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }}>nome, email, telefone, origem, data_cadastro</code></span>
      </div>

      {/* ── Dropzone ── */}
      {phase === 'idle' && (
        <>
          <div
            onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            onClick={() => !file && inputRef.current?.click()}
            style={{
              border: `2px dashed ${dragging ? 'var(--accent)' : file ? 'var(--green)' : 'var(--border)'}`,
              borderRadius: 'var(--radius-lg)',
              background: dragging ? 'var(--accent-dim)' : file ? 'var(--green-dim)' : 'var(--bg-2)',
              padding: '44px 32px', textAlign: 'center',
              cursor: file ? 'default' : 'pointer',
              transition: 'all var(--transition)',
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12,
            }}
          >
            <input ref={inputRef} type="file" accept=".csv" style={{ display: 'none' }}
              onChange={e => handleFile(e.target.files[0])} />

            {file ? (
              <>
                <div style={{ width: 48, height: 48, borderRadius: 12, background: 'var(--green-dim)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <FileText size={22} color="var(--green)" />
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 15 }}>{file.name}</div>
                  <div style={{ color: 'var(--text-secondary)', fontSize: 13, marginTop: 2 }}>
                    {(file.size / 1024 / 1024).toFixed(2)} MB
                  </div>
                </div>
                <button onClick={(e) => { e.stopPropagation(); reset(); }} style={{
                  display: 'flex', alignItems: 'center', gap: 4, background: 'transparent',
                  border: 'none', color: 'var(--text-muted)', fontSize: 12, cursor: 'pointer',
                }}>
                  <X size={12} /> Remover
                </button>
              </>
            ) : (
              <>
                <div style={{ width: 48, height: 48, borderRadius: 12, background: 'var(--bg-3)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Upload size={21} color="var(--text-muted)" />
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 15 }}>Arraste o arquivo aqui</div>
                  <div style={{ color: 'var(--text-secondary)', fontSize: 13, marginTop: 2 }}>
                    ou clique para selecionar · apenas .csv · máx 50 MB
                  </div>
                </div>
              </>
            )}
          </div>

          {errorMsg && (
            <div style={{
              background: 'var(--red-dim)', border: '1px solid rgba(229,72,77,0.2)',
              borderRadius: 'var(--radius-sm)', padding: '10px 14px',
              display: 'flex', alignItems: 'center', gap: 8, color: 'var(--red)', fontSize: 13,
            }}>
              <AlertCircle size={14} /> {errorMsg}
            </div>
          )}

          {file && (
            <button onClick={handleSubmit} style={{
              background: 'var(--accent)', border: 'none', borderRadius: 'var(--radius-sm)',
              color: '#fff', fontWeight: 600, fontSize: 14, padding: '12px 24px',
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
              transition: 'background var(--transition)',
            }}
            onMouseEnter={e => e.currentTarget.style.background = 'var(--accent-hover)'}
            onMouseLeave={e => e.currentTarget.style.background = 'var(--accent)'}
            >
              <Upload size={15} /> Importar Leads
            </button>
          )}
        </>
      )}

      {/* ── Enviando arquivo ── */}
      {phase === 'uploading' && (
        <div style={{
          background: 'var(--bg-2)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)', padding: 24,
          display: 'flex', flexDirection: 'column', gap: 16,
          animation: 'fadeIn 0.25s ease',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <Loader2 size={17} color="var(--accent)" className="spin" />
            <span style={{ fontWeight: 600 }}>Enviando arquivo…</span>
          </div>
          <BarRow label={file?.name} pct={uploadPct} />
        </div>
      )}

      {/* ── Progresso do processamento ── */}
      {(phase === 'polling' || phase === 'done' || phase === 'failed') && (
        <div style={{
          background: 'var(--bg-2)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)', padding: 24,
          display: 'flex', flexDirection: 'column', gap: 20,
          animation: 'fadeIn 0.25s ease',
        }}>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              {phase === 'polling' && <Loader2 size={17} color="var(--accent)" className="spin" />}
              {phase === 'done'    && <CheckCircle size={17} color="var(--green)" />}
              {phase === 'failed'  && <AlertCircle size={17} color="var(--red)" />}
              <span style={{ fontWeight: 700, fontSize: 15 }}>
                {phase === 'polling' ? 'Processando lote…'
                  : phase === 'done' ? 'Importação concluída!'
                  : 'Importação com erros'}
              </span>
            </div>
            {loteId && (
              <span style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                #{String(loteId).slice(0, 8)}…
              </span>
            )}
          </div>

          {/* Barra de progresso */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-secondary)' }}>
              <span>{statusLabel}</span>
              <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                {phase === 'done' ? '100.0' : progress.toFixed(1)}%
              </span>
            </div>
            <div style={{ height: 8, background: 'var(--bg-3)', borderRadius: 99, overflow: 'hidden' }}>
              <div style={{
                height: '100%',
                width: `${phase === 'done' ? 100 : progress}%`,
                background: phase === 'failed'
                  ? 'var(--red)'
                  : 'linear-gradient(90deg, var(--accent), var(--purple))',
                borderRadius: 99,
                transition: 'width 0.5s ease',
              }} />
            </div>
            {total > 0 && (
              <div style={{ fontSize: 11, color: 'var(--text-muted)', textAlign: 'right' }}>
                {processadas.toLocaleString('pt-BR')} / {total.toLocaleString('pt-BR')} linhas
              </div>
            )}
          </div>

          {/* Stats */}
          {(total > 0 || ok > 0 || erros > 0) && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
              {[
                { label: 'Total',   value: total, color: 'var(--text-primary)' },
                { label: 'Sucesso', value: ok,    color: 'var(--green)' },
                { label: 'Erros',   value: erros, color: erros > 0 ? 'var(--red)' : 'var(--text-muted)' },
              ].map(s => (
                <div key={s.label} style={{
                  background: 'var(--bg-3)', borderRadius: 'var(--radius-sm)', padding: '14px', textAlign: 'center',
                }}>
                  <div style={{ fontSize: 22, fontWeight: 700, color: s.color, letterSpacing: '-0.02em' }}>
                    {s.value.toLocaleString('pt-BR')}
                  </div>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 3, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    {s.label}
                  </div>
                </div>
              ))}
            </div>
          )}

          {(phase === 'done' || phase === 'failed') && (
            <div style={{ display: 'flex', gap: 10 }}>
              <button onClick={reset} style={{
                flex: 1, background: 'var(--bg-3)', border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)', color: 'var(--text-primary)',
                fontWeight: 500, padding: '10px', fontSize: 13,
              }}>
                Nova Importação
              </button>
              {phase === 'done' && (
                <button onClick={() => onNavigate('leads')} style={{
                  flex: 1, background: 'var(--accent)', border: 'none',
                  borderRadius: 'var(--radius-sm)', color: '#fff',
                  fontWeight: 600, padding: '10px', fontSize: 13,
                }}>
                  Ver Leads →
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function BarRow({ label, pct }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-secondary)' }}>
        <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '80%' }}>{label}</span>
        <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{pct}%</span>
      </div>
      <div style={{ height: 6, background: 'var(--bg-3)', borderRadius: 99, overflow: 'hidden' }}>
        <div style={{
          height: '100%', width: `${pct}%`,
          background: 'linear-gradient(90deg, var(--accent), var(--purple))',
          borderRadius: 99, transition: 'width 0.25s ease',
        }} />
      </div>
    </div>
  );
}
