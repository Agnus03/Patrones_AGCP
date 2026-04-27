import api from '../api/client';
import type { Product } from '../types';

export const productService = {
  async getById(id: string): Promise<Product> {
    return api.get<Product>(`/products/${id}`);
  },

  async listAll(): Promise<Product[]> {
    return api.get<Product[]>('/products');
  },

  async create(sku: string, name: string): Promise<Product> {
    return api.post<Product>('/products', { sku, name });
  },
};

export default productService;