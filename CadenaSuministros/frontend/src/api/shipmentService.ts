import api from '../api/client';
import type { Shipment } from '../types';

export const shipmentService = {
  async getById(id: string): Promise<Shipment> {
    return api.get<Shipment>(`/shipments/${id}`);
  },

  async listAll(): Promise<Shipment[]> {
    return api.get<Shipment[]>('/shipments');
  },
};

export default shipmentService;