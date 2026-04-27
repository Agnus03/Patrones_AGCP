import { useState, useEffect } from 'react';
import ProductCreate from '../components/ProductCreate';
import productService from '../api/productService';
import type { Product } from '../types';

export function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    loadProducts();
  }, [refreshKey]);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await productService.listAll();
      setProducts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar');
    } finally {
      setLoading(false);
    }
  };

  const handleSuccess = () => {
    setRefreshKey((k) => k + 1);
  };

  return (
    <div>
      <h1 style={{ marginBottom: '1.5rem', fontSize: '1.875rem', fontWeight: 'bold' }}>
        Productos
      </h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
        <div>
          <ProductCreate onSuccess={handleSuccess} />
        </div>

        <div>
          {loading ? (
            <div className="card">
              <p>Cargando productos...</p>
            </div>
          ) : error ? (
            <div className="card">
              <p style={{ color: 'var(--danger)' }}>Error: {error}</p>
              <button className="btn btn-primary" onClick={loadProducts} style={{ marginTop: '1rem' }}>
                Reintentar
              </button>
            </div>
          ) : products.length === 0 ? (
            <div className="card">
              <p style={{ color: 'var(--text-secondary)' }}>
                No hay productos registrados. Crea uno para comenzar.
              </p>
            </div>
          ) : (
            <div className="card">
              <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ fontSize: '1.125rem' }}>
                  Productos ({products.length})
                </h3>
                <button className="btn btn-primary" onClick={loadProducts}>
                  Actualizar
                </button>
              </div>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--border)' }}>
                      <th style={{ textAlign: 'left', padding: '0.75rem' }}>ID</th>
                      <th style={{ textAlign: 'left', padding: '0.75rem' }}>SKU</th>
                      <th style={{ textAlign: 'left', padding: '0.75rem' }}>Nombre</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map((product) => (
                      <tr
                        key={product.id}
                        style={{ borderBottom: '1px solid var(--border)' }}
                      >
                        <td style={{ padding: '0.75rem', fontFamily: 'monospace', fontSize: '0.875rem' }}>
                          {product.id.slice(0, 8)}
                        </td>
                        <td style={{ padding: '0.75rem' }}>
                          {product.sku}
                        </td>
                        <td style={{ padding: '0.75rem' }}>
                          {product.name}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ProductsPage;