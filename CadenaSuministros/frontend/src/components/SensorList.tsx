import { useState } from 'react';
import type { SensorReading, SensorReadingRequest } from '../types';
import { sensorService } from '../api/sensorService';

interface SensorListProps {
  readings: SensorReading[];
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export function SensorList({ readings, loading, error, onRefresh }: SensorListProps) {
  if (loading) {
    return (
      <div className="card">
        <p>Cargando lecturas...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card">
        <p style={{ color: 'var(--danger)' }}>Error: {error}</p>
        <button className="btn btn-primary" onClick={onRefresh} style={{ marginTop: '1rem' }}>
          Reintentar
        </button>
      </div>
    );
  }

  if (readings.length === 0) {
    return (
      <div className="card">
        <p style={{ color: 'var(--text-secondary)' }}>
          No hay lecturas registradas.
        </p>
      </div>
    );
  }

  return (
    <div className="card">
      <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ fontSize: '1.125rem' }}>
          Lecturas de Sensores ({readings.length})
        </h3>
        <button className="btn btn-primary" onClick={onRefresh}>
          Actualizar
        </button>
      </div>
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid var(--border)' }}>
              <th style={{ textAlign: 'left', padding: '0.75rem' }}>Fecha</th>
              <th style={{ textAlign: 'left', padding: '0.75rem' }}>Shipment</th>
              <th style={{ textAlign: 'right', padding: '0.75rem' }}>Temp (°C)</th>
              <th style={{ textAlign: 'right', padding: '0.75rem' }}>Humedad (%)</th>
              <th style={{ textAlign: 'right', padding: '0.75rem' }}>Lat</th>
              <th style={{ textAlign: 'right', padding: '0.75rem' }}>Lng</th>
            </tr>
          </thead>
          <tbody>
            {readings.map((reading) => (
              <tr
                key={reading.id}
                style={{ borderBottom: '1px solid var(--border)' }}
              >
                <td style={{ padding: '0.75rem', whiteSpace: 'nowrap' }}>
                  {new Date(reading.timestamp).toLocaleString('es-CO')}
                </td>
                <td style={{ padding: '0.75rem', fontFamily: 'monospace', fontSize: '0.875rem' }}>
                  {reading.shipmentId.slice(0, 8)}
                </td>
                <td
                  style={{
                    padding: '0.75rem',
                    textAlign: 'right',
                    color: isTemperatureAlert(reading.temperatureC) ? 'var(--danger)' : 'inherit',
                    fontWeight: isTemperatureAlert(reading.temperatureC) ? 'bold' : 'normal',
                  }}
                >
                  {reading.temperatureC?.toFixed(1) ?? '-'}
                </td>
                <td style={{ padding: '0.75rem', textAlign: 'right' }}>
                  {reading.humidityPct?.toFixed(1) ?? '-'}
                </td>
                <td style={{ padding: '0.75rem', textAlign: 'right', fontFamily: 'monospace', fontSize: '0.875rem' }}>
                  {reading.latitude?.toFixed(4) ?? '-'}
                </td>
                <td style={{ padding: '0.75rem', textAlign: 'right', fontFamily: 'monospace', fontSize: '0.875rem' }}>
                  {reading.longitude?.toFixed(4) ?? '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function isTemperatureAlert(temp: number | null | undefined): boolean {
  if (temp === null || temp === undefined) return false;
  return temp > 30 || temp < 2;
}

export default SensorList;