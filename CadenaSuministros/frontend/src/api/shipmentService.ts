import api from '../api/client';
import type { Shipment, ShipmentInfo, ShipmentEvent } from '../types';
import { CrudService } from './CrudService';

class ShipmentService extends CrudService<Shipment> {
  protected basePath(): string {
    return '/shipments';
  }

  async create(data: { productId: string; status: string; currentLocation: string }): Promise<Shipment> {
    return api.post<Shipment>('/shipments', data);
  }

  async listAllInfo(): Promise<ShipmentInfo[]> {
    return api.get<ShipmentInfo[]>('/shipments/info');
  }

  async getHistory(shipmentId: string): Promise<ShipmentEvent[]> {
    return api.get<ShipmentEvent[]>(`/shipments/${shipmentId}/history`);
  }

  async updateStatus(shipmentId: string, status: string): Promise<Shipment> {
    return api.patch<Shipment>(`/shipments/${shipmentId}/status`, { status });
  }

  async updateLocation(shipmentId: string, currentLocation: string): Promise<Shipment> {
    return api.patch<Shipment>(`/shipments/${shipmentId}/location`, { currentLocation });
  }
}

export const shipmentService = new ShipmentService();
export default shipmentService;
