import api from '../api/client';
import type { DeliveryReport } from '../types';

export const reportService = {
  async generateDeliveryReport(shipmentId: string): Promise<DeliveryReport> {
    return api.post<DeliveryReport>(`/reports/delivery/${shipmentId}`);
  },
};

export default reportService;