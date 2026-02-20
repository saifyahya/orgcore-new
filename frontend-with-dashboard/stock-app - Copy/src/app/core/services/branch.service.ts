import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Branch, BranchRequest, Page } from '../models';

@Injectable({ providedIn: 'root' })
export class BranchService {
  private path = '/branches';

  constructor(private api: ApiService) { }

  getAll(page: number = 0, size: number = 10, search?: string, isActive?: number): Observable<Page<Branch>> {
    const params: any = { page, size };
    if (search) params.search = search;
    if (isActive !== undefined && isActive !== null) params.isActive = isActive;
    return this.api.get<Page<Branch>>(this.path, params);
  }

  getById(id: number): Observable<Branch> {
    return this.api.get<Branch>(`${this.path}/${id}`);
  }

  create(branch: BranchRequest): Observable<Branch> {
    return this.api.post<Branch>(this.path, branch);
  }

  update(id: number, branch: BranchRequest): Observable<Branch> {
    return this.api.put<Branch>(`${this.path}/${id}`, branch);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  toggleActive(id: number, isActive: number): Observable<Branch> {
    return this.api.patch<Branch>(`${this.path}/${id}/active`, { isActive });
  }
}
