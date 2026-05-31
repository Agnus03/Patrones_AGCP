import api from './client';

export abstract class CrudService<T> {
  protected abstract basePath(): string;

  async listAll(): Promise<T[]> {
    return api.get<T[]>(this.basePath());
  }

  async getById(id: string): Promise<T> {
    return api.get<T>(`${this.basePath()}/${id}`);
  }
}
