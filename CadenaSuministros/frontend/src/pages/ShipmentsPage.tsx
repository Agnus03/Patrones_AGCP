import { useState, useEffect } from 'react';
import type { Shipment } from '../types';
import { shipmentService } from '../api/shipmentService';
import { ShipmentCreate } from '../components/ShipmentCreate';

export function ShipmentsPage() {
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchShipments = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await shipmentService.listAll();
      setShipments(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchShipments();
  }, []);

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      IN_TRANSIT: 'En tránsito',
      DELIVERED: 'Entregado',
      DELAYED: 'Retrasado',
    };
    return labels[status] || status;
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      PENDING: 'var(--warning)',
      IN_TRANSIT: 'var(--primary)',
      DELIVERED: 'var(--success)',
      DELAYED: 'var(--danger)',
    };
    return colors[status] || 'var(--secondary)';
  };

  return (
    <div>
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold' }}>
          Envíos
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
          Gestión de envíos en la cadena de suministro
        </p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
        <div>
          <ShipmentCreate onSuccess={fetchShipments} />
        </div>

        <div className="card">
          <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ fontSize: '1.125rem' }}>
              Lista de Envíos ({shipments.length})
            </h3>
            <button className="btn btn-primary" onClick={fetchShipments}>
              Actualizar
            </button>
          </div>

          {loading && <p>Cargando...</p>}
          
          {error && <p style={{ color: 'var(--danger)' }}>{error}</p>}
          
          {!loading && shipments.length === 0 && (
            <p style={{ color: 'var(--text-secondary)' }}>
              No hay envíos. Crea uno para comenzar.
            </p>
          )}

          {!loading && shipments.length > 0 && (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border)' }}>
                    <th style={{ textAlign: 'left', padding: '0.75rem' }}>ID</th>
                    <th style={{ textAlign: 'left', padding: '0.75rem' }}>Estado</th>
                    <th style={{ textAlign: 'left', padding: '0.75rem' }}>Ubicación</th>
                    <th style={{ textAlign: 'left', padding: '0.75rem' }}>Actualizado</th>
                  </tr>
                </thead>
                <tbody>
                  {shipments.map((shipment) => (
                    <tr
                      key={shipment.id}
                      style={{ borderBottom: '1px solid var(--border)' }}
                    >
                      <td style={{ padding: '0.75rem', fontFamily: 'monospace', fontSize: '0.875rem' }}>
                        {shipment.id.slice(0, 8)}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <span
                          style={{
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            fontSize: '0.75rem',
                            fontWeight: 500,
                            background: getStatusColor(shipment.status),
                            color: 'white',
                          }}
                        >
                          {getStatusLabel(shipment.status)}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {shipment.currentLocation}
                      </td>
                      <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                        {new Date(shipment.updatedAt).toLocaleDateString('es-CO')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ShipmentsPage;