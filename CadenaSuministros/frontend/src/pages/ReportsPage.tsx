import { useState, useEffect } from 'react';
import type { Shipment } from '../types';
import { shipmentService } from '../api/shipmentService';
import { reportService } from '../api/reportService';

export function ReportsPage() {
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [loadingShipments, setLoadingShipments] = useState(true);
  
  const [selectedShipmentId, setSelectedShipmentId] = useState<string>('');
  const [loadingReport, setLoadingReport] = useState(false);
  const [report, setReport] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadShipments = async () => {
      try {
        const data = await shipmentService.listAll();
        setShipments(data);
      } catch (err) {
        console.error('Error loading shipments:', err);
      } finally {
        setLoadingShipments(false);
      }
    };
    loadShipments();
  }, []);

  const generateReport = async () => {
    if (!selectedShipmentId) return;
    
    setLoadingReport(true);
    setError(null);
    setReport(null);
    
    try {
      const data = await reportService.generateDeliveryReport(selectedShipmentId);
      setReport(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al generar reporte');
    } finally {
      setLoadingReport(false);
    }
  };

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      IN_TRANSIT: 'En tránsito',
      DELIVERED: 'Entregado',
      DELAYED: 'Retrasado',
    };
    return labels[status] || status;
  };

  return (
    <div>
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold' }}>
          Reportes de Entrega
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
          Genera reportes con estadísticas ambientales de tus envíos
        </p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
        {/* Panel de Generación */}
        <div className="card">
          <h3 style={{ marginBottom: '1rem', fontSize: '1.125rem' }}>
            Generar Reporte
          </h3>
          
          <div className="form-group">
            <label htmlFor="shipmentSelect">Seleccionar Envío</label>
            <select
              id="shipmentSelect"
              value={selectedShipmentId}
              onChange={(e) => {
                setSelectedShipmentId(e.target.value);
                setReport(null);
                setError(null);
              }}
              disabled={loadingShipments}
            >
              <option value="">
                {loadingShipments ? 'Cargando...' : '-- Seleccionar --'}
              </option>
              {shipments.map((shipment) => (
                <option key={shipment.id} value={shipment.id}>
                  {shipment.id.slice(0, 8)} - {getStatusLabel(shipment.status)} ({shipment.currentLocation})
                </option>
              ))}
            </select>
          </div>

          <button
            className="btn btn-primary"
            onClick={generateReport}
            disabled={!selectedShipmentId || loadingReport}
            style={{ width: '100%' }}
          >
            {loadingReport ? 'Generando...' : 'Generar Reporte'}
          </button>

          {error && (
            <p style={{ color: 'var(--danger)', marginTop: '1rem' }}>
              {error}
            </p>
          )}
        </div>

        {/* Panel del Reporte */}
        <div className="card">
          <h3 style={{ marginBottom: '1rem', fontSize: '1.125rem' }}>
            Reporte de Entrega
          </h3>
          
          {!report && !error && (
            <p style={{ color: 'var(--text-secondary)' }}>
              Selecciona un envío y genera el reporte para ver los resultados.
            </p>
          )}

          {report && (
            <div>
              {/* Encabezado */}
              <div style={{ 
                padding: '1rem', 
                background: 'var(--background)', 
                borderRadius: '8px',
                marginBottom: '1rem'
              }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>ID Reporte</p>
                    <p style={{ fontFamily: 'monospace' }}>{report.reportId?.slice(0, 8) || 'N/A'}</p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Estado</p>
                    <p style={{ fontWeight: 500 }}>{report.deliveryStatus || 'N/A'}</p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Origen</p>
                    <p>{report.origin || 'N/A'}</p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Destino</p>
                    <p>{report.destination || 'N/A'}</p>
                  </div>
                </div>
              </div>

              {/* Estadísticas */}
              <h4 style={{ marginBottom: '0.75rem', marginTop: '1rem' }}>
                Estadísticas Ambientales
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div style={{ padding: '1rem', background: 'var(--background)', borderRadius: '8px' }}>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Temperatura Promedio</p>
                  <p style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
                    {report.averageTemperature?.toFixed(1) ?? '0'}°C
                  </p>
                </div>
                <div style={{ padding: '1rem', background: 'var(--background)', borderRadius: '8px' }}>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Humedad Promedio</p>
                  <p style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
                    {report.averageHumidity?.toFixed(1) ?? '0'}%
                  </p>
                </div>
              </div>

              {/* Alertas */}
              <h4 style={{ marginBottom: '0.75rem', marginTop: '1rem' }}>
                Alertas
              </h4>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div style={{ 
                  padding: '0.75rem', 
                  borderRadius: '8px',
                  background: report.temperatureAlert ? 'rgba(239, 68, 68, 0.1)' : 'rgba(34, 197, 94, 0.1)',
                  border: `1px solid ${report.temperatureAlert ? 'var(--danger)' : 'var(--success)'}`
                }}>
                  <p style={{ fontSize: '0.875rem', fontWeight: 500 }}>
                    {report.temperatureAlert ? '⚠️ Alerta Temperatura' : '✓ Temperatura OK'}
                  </p>
                </div>
                <div style={{ 
                  padding: '0.75rem', 
                  borderRadius: '8px',
                  background: report.humidityAlert ? 'rgba(239, 68, 68, 0.1)' : 'rgba(34, 197, 94, 0.1)',
                  border: `1px solid ${report.humidityAlert ? 'var(--danger)' : 'var(--success)'}`
                }}>
                  <p style={{ fontSize: '0.875rem', fontWeight: 500 }}>
                    {report.humidityAlert ? '⚠️ Alerta Humedad' : '✓ Humedad OK'}
                  </p>
                </div>
              </div>

              {/* Observaciones */}
              {report.observations && (
                <>
                  <h4 style={{ marginBottom: '0.75rem', marginTop: '1rem' }}>
                    Observaciones
                  </h4>
                  <p style={{ color: 'var(--text-secondary)' }}>
                    {report.observations}
                  </p>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ReportsPage;