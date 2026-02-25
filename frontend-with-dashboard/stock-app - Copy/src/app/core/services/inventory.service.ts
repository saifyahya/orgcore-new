import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Inventory, InventoryRequest, Page, StockMovement, StockMovementRequest } from '../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private path = '/inventories';
  private baseUrl = environment.apiUrl;

  constructor(private api: ApiService, private http: HttpClient) { }

  getAll(page: number = 0, size: number = 10, branchId?: number, search?: string): Observable<Page<Inventory>> {
    const params: any = { page, size };
    if (branchId) params['branchId'] = branchId;
    if (search) params['search'] = search;
    return this.api.get<Page<Inventory>>(this.path, params);
  }

  getByBranchAndProduct(branchId: number, productId: number): Observable<Inventory> {
    return this.api.get<Inventory>(`${this.path}/branch/${branchId}/product/${productId}`);
  }

  create(inventory: InventoryRequest): Observable<Inventory> {
    return this.api.post<Inventory>(this.path, inventory);
  }

  update(id: number, inventory: InventoryRequest): Observable<Inventory> {
    return this.api.put<Inventory>(`${this.path}/${id}`, inventory);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  importFromExcel(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}${this.path}/import`, formData);
  }

  exportToExcel(page: number = 0, size: number = 10, branchId?: number, search?: string): Observable<Blob> {
    const params: any = { page, size };
    if (branchId) params['branchId'] = branchId;
    if (search) params['search'] = search;
    return this.http.get(`${this.baseUrl}${this.path}/export/excel`, {
      params,
      responseType: 'blob'
    });
  }
}

@Injectable({ providedIn: 'root' })
export class StockMovementService {
  private path = '/stock-movements';

  constructor(private api: ApiService) { }

  getAll(params?: { branchId?: number; productId?: number; type?: string }): Observable<Page<StockMovement>> {
    const query: Record<string, string | number | boolean> = {};
    if (params?.branchId) query['branchId'] = params.branchId;
    if (params?.productId) query['productId'] = params.productId;
    if (params?.type) query['type'] = params.type;
    return this.api.get<Page<StockMovement>>(this.path, query);
  }

  getById(id: number): Observable<StockMovement> {
    return this.api.get<StockMovement>(`${this.path}/${id}`);
  }

  create(movement: StockMovementRequest): Observable<StockMovement> {
    return this.api.post<StockMovement>(this.path, movement);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
