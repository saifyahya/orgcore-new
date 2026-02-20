import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Page, Product, ProductRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private path = '/products';

  constructor(private api: ApiService) { }

  getAll(page: number = 0, size: number = 10, search?: string, isActive?: number, categoryId?: number): Observable<Page<Product>> {
    const params: any = { page, size };
    if (search) params.search = search;
    if (isActive !== undefined && isActive !== null) params.isActive = isActive;
    if (categoryId !== undefined && categoryId !== null) params.categoryId = categoryId;
    return this.api.get<Page<Product>>(this.path, params);
  }

  getByCategory(categoryId: number): Observable<Product[]> {
    return this.api.get<Product[]>(`${this.path}?categoryId=${categoryId}`);
  }

  getById(id: number): Observable<Product> {
    return this.api.get<Product>(`${this.path}/${id}`);
  }

  create(product: ProductRequest): Observable<Product> {
    return this.api.post<Product>(this.path, product);
  }

  update(id: number, product: ProductRequest): Observable<Product> {
    return this.api.put<Product>(`${this.path}/${id}`, product);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  toggleActive(id: number, isActive: number): Observable<Product> {
    return this.api.patch<Product>(`${this.path}/${id}/active`, { isActive });
  }
}
