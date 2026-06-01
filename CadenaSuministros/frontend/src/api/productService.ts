import api from './client';
import type { Product } from '../types';
import { CrudService } from './CrudService';

class ProductService extends CrudService<Product> {
  protected basePath(): string {
    return '/products';
  }

  async create(sku: string, name: string): Promise<Product> {
    return api.post<Product>(this.basePath(), { sku, name });
  }
}

export const productService = new ProductService();
export default productService;
