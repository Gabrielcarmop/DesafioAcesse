import { useState } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import UploadPage from './pages/Upload';
import LotesPage from './pages/Lotes';
import LeadsPage from './pages/Leads';

export default function App() {
  const [page, setPage] = useState('dashboard');

  const pages = {
    dashboard: <Dashboard />,
    upload: <UploadPage onNavigate={setPage} />,
    lotes: <LotesPage />,
    leads: <LeadsPage />,
  };

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <Sidebar active={page} onNav={setPage} />
      <main style={{
        flex: 1,
        overflowY: 'auto',
        background: 'var(--bg)',
      }}>
        {pages[page] ?? pages.dashboard}
      </main>
    </div>
  );
}
