import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Page, Product, ProductRequest } from '../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private path = '/products';
  private baseUrl = environment.apiUrl;

  constructor(private api: ApiService, private http: HttpClient) { }

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

  create(product: ProductRequest, imageFile?: File): Observable<Product> {
      const formData = new FormData();
      // Append all product fields
      Object.keys(product).forEach(key => {
        const value = (product as any)[key];
        if (value !== null && value !== undefined) {
          formData.append(key, value.toString());
        }
      });
      if (imageFile) {  // Append image file
      formData.append('imageFile', imageFile);
       }
      return this.api.post<Product>(this.path, formData);
   
  }

  update(id: number, product: ProductRequest, imageFile?: File): Observable<Product> {
      const formData = new FormData();
      // Append all product fields
      Object.keys(product).forEach(key => {
        const value = (product as any)[key];
        if (value !== null && value !== undefined) {
          formData.append(key, value.toString());
        }
      });
        if (imageFile) { // Append image file
      formData.append('imageFile', imageFile);
    }
      return this.api.put<Product>(`${this.path}/${id}`, formData);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  toggleActive(id: number, isActive: number): Observable<Product> {
    return this.api.patch<Product>(`${this.path}/${id}/active`, { isActive });
  }

  importFromExcel(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}${this.path}/import`, formData);
  }

  exportToExcel(page: number = 0, size: number = 10, search?: string, isActive?: number, categoryId?: number): Observable<Blob> {
    const params: any = { page, size };
    if (search) params.search = search;
    if (isActive !== undefined && isActive !== null) params.isActive = isActive;
    if (categoryId !== undefined && categoryId !== null) params.categoryId = categoryId;
    return this.http.get(`${this.baseUrl}${this.path}/export/excel`, {
      params,
      responseType: 'blob'
    });
  }
}
