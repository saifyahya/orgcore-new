import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Sale, SaleRequest, Page } from '../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SaleService {
  private path = '/sales';
  private baseUrl = environment.apiUrl;

  constructor(private api: ApiService, private http: HttpClient) { }

  getAll(params?: { branchId?: number; from?: string; to?: string }): Observable<Page<Sale>> {
    const query: Record<string, string | number | boolean> = {};
    if (params?.branchId) query['branchId'] = params.branchId;
    if (params?.from) query['from'] = params.from;
    if (params?.to) query['to'] = params.to;
    return this.api.get<Page<Sale>>(this.path, query);
  }

  getById(id: number): Observable<Sale> {
    return this.api.get<Sale>(`${this.path}/${id}`);
  }

  create(sale: SaleRequest): Observable<Sale> {
    return this.api.post<Sale>(this.path, sale);
  }

  update(id: number, sale: SaleRequest): Observable<Sale> {
    return this.api.put<Sale>(`${this.path}/${id}`, sale);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  importFromExcel(file: File, branchId: number): Observable<Sale[]> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('branchId', String(branchId));
    return this.http.post<Sale[]>(`${this.baseUrl}${this.path}/import`, formData);
  }
}
