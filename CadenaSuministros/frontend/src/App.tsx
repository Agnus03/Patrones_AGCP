import { useState } from 'react';
import SensorsPage from './pages/SensorsPage';
import ShipmentsPage from './pages/ShipmentsPage';
import ReportsPage from './pages/ReportsPage';
import ProductsPage from './pages/ProductsPage';

type Page = 'sensors' | 'shipments' | 'reports' | 'products';

function App() {
  const [currentPage, setCurrentPage] = useState<Page>('products');

  const navItems: { id: Page; label: string }[] = [
    { id: 'products', label: 'Productos' },
    { id: 'sensors', label: 'Sensores' },
    { id: 'shipments', label: 'Envíos' },
    { id: 'reports', label: 'Reportes' },
  ];

  return (
    <div>
      <nav
        style={{
          background: 'var(--surface)',
          borderBottom: '1px solid var(--border)',
          padding: '1rem',
          marginBottom: '2rem',
        }}
      >
        <div className="container">
          <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
            <h1 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>
              Cadena de Suministros
            </h1>
            <div style={{ display: 'flex', gap: '1.5rem' }}>
              {navItems.map((item) => (
                <button
                  key={item.id}
                  onClick={() => setCurrentPage(item.id)}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    fontSize: '1rem',
                    color: currentPage === item.id ? 'var(--primary)' : 'var(--text-secondary)',
                    fontWeight: currentPage === item.id ? 500 : 400,
                    cursor: 'pointer',
                    padding: '0.25rem 0',
                  }}
                >
                  {item.label}
                </button>
              ))}
            </div>
          </div>
        </div>
      </nav>

      <main className="container">
        {currentPage === 'products' && <ProductsPage />}
        {currentPage === 'sensors' && <SensorsPage />}
        {currentPage === 'shipments' && <ShipmentsPage />}
        {currentPage === 'reports' && <ReportsPage />}
      </main>
    </div>
  );
}

export default App;