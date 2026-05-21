import { useEffect, useState } from 'react';
import { BarChart2, Users, Package, AlertTriangle, RefreshCw, TrendingUp } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import StatCard from '../components/StatCard';
import { getDashboard, getLotes } from '../services/api';

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{
      background: 'var(--bg-3)', border: '1px solid var(--border)',
      borderRadius: 'var(--radius-sm)', padding: '8px 12px', fontSize: 12,
    }}>
      <div style={{ color: 'var(--text-secondary)', marginBottom: 4 }}>{label}</div>
      {payload.map(p => (
        <div key={p.dataKey} style={{ color: p.color, fontWeight: 600 }}>
          {p.dataKey === 'sucesso' ? 'Sucesso' : p.dataKey === 'erro' ? 'Erros' : p.dataKey}: {p.value?.toLocaleString('pt-BR')}
        </div>
      ))}
    </div>
  );
};

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [recentLotes, setRecentLotes] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const [dashRes, lotesRes] = await Promise.all([
        getDashboard().catch(() => null),
        getLotes(0, 6).catch(() => null),
      ]);
      if (dashRes?.data) setStats(dashRes.data);
      const content = lotesRes?.data?.content ?? lotesRes?.data ?? [];
      setRecentLotes(Array.isArray(content) ? content.slice(0, 6) : []);
    } catch (_) {}
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const chartData = recentLotes
    .filter(l => l.totalLinhas != null)
    .map(l => ({
      name: `Lote ${l.id ?? '?'}`,
      sucesso: l.linhasSucesso ?? l.totalLinhas ?? 0,
      erro: l.linhasErro ?? 0,
    }))
    .reverse();

  return (
    <div style={{ padding: '32px 32px', display: 'flex', flexDirection: 'column', gap: 28 }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.02em' }}>Dashboard</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginTop: 4 }}>
            Visão geral do sistema de importação de leads
          </p>
        </div>
        <button onClick={load} style={{
          display: 'flex', alignItems: 'center', gap: 6,
          background: 'var(--bg-3)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)', padding: '8px 14px',
          color: 'var(--text-secondary)', fontSize: 13, fontWeight: 500,
          transition: 'all var(--transition)',
        }}
        onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--border-hover)'; e.currentTarget.style.color = 'var(--text-primary)'; }}
        onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border)'; e.currentTarget.style.color = 'var(--text-secondary)'; }}
        >
          <RefreshCw size={13} className={loading ? 'spin' : ''} />
          Atualizar
        </button>
      </div>

      {/* Stat Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
        <StatCard label="Total de Leads" value={stats?.totalLeads?.toLocaleString('pt-BR') ?? (loading ? '...' : '0')} icon={Users} color="var(--accent)" sub="leads importados" />
        <StatCard label="Lotes Processados" value={stats?.totalLotes?.toLocaleString('pt-BR') ?? (loading ? '...' : '0')} icon={Package} color="var(--purple)" sub="lotes concluídos" />
        <StatCard label="Taxa de Erro" value={stats?.taxaErro != null ? `${stats.taxaErro.toFixed(1)}%` : (loading ? '...' : '0%')} icon={AlertTriangle} color={stats?.taxaErro > 5 ? 'var(--red)' : 'var(--green)'} sub="média geral" />
        <StatCard label="Leads (último lote)" value={stats?.ultimoLoteLeads?.toLocaleString('pt-BR') ?? (loading ? '...' : '0')} icon={TrendingUp} color="var(--green)" sub="sucesso" />
      </div>

      {/* Chart + Status */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: 16 }}>
        {/* Bar Chart */}
        <div style={{
          background: 'var(--bg-2)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)', padding: '24px',
        }}>
          <div style={{ marginBottom: 20 }}>
            <div style={{ fontWeight: 600, fontSize: 15 }}>Leads por Lote</div>
            <div style={{ color: 'var(--text-secondary)', fontSize: 13, marginTop: 2 }}>Últimos lotes processados</div>
          </div>
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={chartData} barSize={28} barGap={4}>
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.03)' }} />
                <Bar dataKey="sucesso" fill="var(--accent)" radius={[4,4,0,0]} />
                <Bar dataKey="erro" fill="var(--red)" radius={[4,4,0,0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div style={{ height: 220, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: 13 }}>
              <div style={{ textAlign: 'center' }}>
                <BarChart2 size={32} style={{ opacity: 0.3, margin: '0 auto 8px' }} />
                <div>Nenhum dado ainda</div>
              </div>
            </div>
          )}
        </div>

        {/* Recent Lotes */}
        <div style={{
          background: 'var(--bg-2)', border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)', padding: '24px', overflow: 'hidden',
        }}>
          <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>Lotes Recentes</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {recentLotes.length === 0 && (
              <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: '32px 0' }}>
                Nenhum lote ainda
              </div>
            )}
            {recentLotes.map((lote, i) => {
              const status = lote.status ?? 'DESCONHECIDO';
              const colorMap = {
                CONCLUIDO: 'var(--green)', PROCESSANDO: 'var(--accent)',
                ERRO: 'var(--red)', PENDENTE: 'var(--yellow)',
              };
              const bgMap = {
                CONCLUIDO: 'var(--green-dim)', PROCESSANDO: 'var(--accent-dim)',
                ERRO: 'var(--red-dim)', PENDENTE: 'var(--yellow-dim)',
              };
              return (
                <div key={lote.id ?? i} style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '10px 12px', background: 'var(--bg-3)', borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border)',
                }}>
                  <div>
                    <div style={{ fontWeight: 500, fontSize: 13 }}>Lote #{lote.id}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                      {lote.totalLinhas?.toLocaleString('pt-BR') ?? '?'} linhas
                    </div>
                  </div>
                  <span style={{
                    fontSize: 11, fontWeight: 600, padding: '3px 8px',
                    borderRadius: 99, background: bgMap[status] ?? 'var(--bg-4)',
                    color: colorMap[status] ?? 'var(--text-muted)',
                    textTransform: 'uppercase', letterSpacing: '0.05em',
                  }}>
                    {status}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
