import api from '../api/client';
import type { SensorReading, SensorReadingRequest } from '../types';

export const sensorService = {
  async listAll(): Promise<SensorReading[]> {
    return api.get<SensorReading[]>('/sensors');
  },

  async create(request: SensorReadingRequest): Promise<SensorReading> {
    return api.post<SensorReading>('/sensors/readings', request);
  },
};

export default sensorService;