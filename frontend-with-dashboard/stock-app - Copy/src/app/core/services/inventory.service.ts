import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Inventory, InventoryRequest, Page, StockMovement, StockMovementRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private path = '/inventories';

  constructor(private api: ApiService) { }

  getAll(branchId?: number): Observable<Page<Inventory>> {
    const params: Record<string, number> = {};
    if (branchId) params['branchId'] = branchId;
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
