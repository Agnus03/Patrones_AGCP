import { useState, useEffect } from 'react';
import type { Shipment, SensorReadingRequest } from '../types';
import { shipmentService } from '../api/shipmentService';

interface SensorRegisterProps {
  onSuccess: () => void;
}

export function SensorRegister({ onSuccess }: SensorRegisterProps) {
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [loadingShipments, setLoadingShipments] = useState(true);
  
  const [formData, setFormData] = useState<SensorReadingRequest>({
    shipmentId: '',
    temperatureC: null,
    humidityPct: null,
    latitude: null,
    longitude: null,
  });
  const [loading, setLoading] = useState(false);
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await fetch('/api/sensors/readings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      setFormData({
        shipmentId: '',
        temperatureC: null,
        humidityPct: null,
        latitude: null,
        longitude: null,
      });
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al registrar');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: keyof SensorReadingRequest) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = e.target.value;
    setFormData((prev) => ({
      ...prev,
      [field]: value === '' ? null : parseFloat(value),
    }));
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
    <div className="card">
      <h3 style={{ marginBottom: '1rem', fontSize: '1.125rem' }}>
        Registrar Lectura
      </h3>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="shipmentId">Seleccionar Envío</label>
          <select
            id="shipmentId"
            value={formData.shipmentId}
            onChange={(e) =>
              setFormData((prev) => ({ ...prev, shipmentId: e.target.value }))
            }
            required
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
          {shipments.length === 0 && !loadingShipments && (
            <p style={{ fontSize: '0.875rem', color: 'var(--warning)', marginTop: '0.25rem' }}>
              No hay envíos disponibles. Crea un envío primero.
            </p>
          )}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label htmlFor="temperatureC">Temperatura (°C)</label>
            <input
              id="temperatureC"
              type="number"
              step="0.1"
              value={formData.temperatureC ?? ''}
              onChange={handleChange('temperatureC')}
              placeholder="25.0"
            />
          </div>

          <div className="form-group">
            <label htmlFor="humidityPct">Humedad (%)</label>
            <input
              id="humidityPct"
              type="number"
              step="0.1"
              value={formData.humidityPct ?? ''}
              onChange={handleChange('humidityPct')}
              placeholder="60.0"
            />
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div className="form-group">
            <label htmlFor="latitude">Latitud</label>
            <input
              id="latitude"
              type="number"
              step="0.0001"
              value={formData.latitude ?? ''}
              onChange={handleChange('latitude')}
              placeholder="4.7110"
            />
          </div>

          <div className="form-group">
            <label htmlFor="longitude">Longitud</label>
            <input
              id="longitude"
              type="number"
              step="0.0001"
              value={formData.longitude ?? ''}
              onChange={handleChange('longitude')}
              placeholder="-74.0721"
            />
          </div>
        </div>

        {error && (
          <div style={{ color: 'var(--danger)', marginBottom: '1rem' }}>
            {error}
          </div>
        )}

        <button
          type="submit"
          className="btn btn-primary"
          disabled={loading || loadingShipments || shipments.length === 0}
          style={{ width: '100%' }}
        >
          {loading ? 'Registrando...' : 'Registrar Lectura'}
        </button>
      </form>
    </div>
  );
}

export default SensorRegister;