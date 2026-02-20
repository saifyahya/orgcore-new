import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Category, CategoryRequest, Page } from '../models';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private path = '/categories';

  constructor(private api: ApiService) { }

  getAll(page: number = 0, size: number = 10, search?: string, isActive?: number): Observable<Page<Category>> {
    const params: any = { page, size };
    if (search) params.search = search;
    if (isActive !== undefined && isActive !== null) params.isActive = isActive;
    return this.api.get<Page<Category>>(this.path, params);
  }

  getById(id: number): Observable<Category> {
    return this.api.get<Category>(`${this.path}/${id}`);
  }

  create(category: CategoryRequest): Observable<Category> {
    return this.api.post<Category>(this.path, category);
  }

  update(id: number, category: CategoryRequest): Observable<Category> {
    return this.api.put<Category>(`${this.path}/${id}`, category);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
