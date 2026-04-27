import { useState, useEffect } from 'react';
import type { SensorReading } from '../types';
import { sensorService } from '../api/sensorService';
import { SensorList } from '../components/SensorList';
import { SensorRegister } from '../components/SensorRegister';

export function SensorsPage() {
  const [readings, setReadings] = useState<SensorReading[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReadings = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await sensorService.listAll();
      setReadings(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar lecturas');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReadings();
  }, []);

  return (
    <div>
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold' }}>
          Sensores IoT
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
          Registro y monitoreo de sensores de temperatura, humedad y ubicación GPS
        </p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
        <div>
          <SensorRegister onSuccess={fetchReadings} />
        </div>
        <div>
          <SensorList
            readings={readings}
            loading={loading}
            error={error}
            onRefresh={fetchReadings}
          />
        </div>
      </div>
    </div>
  );
}

export default SensorsPage;