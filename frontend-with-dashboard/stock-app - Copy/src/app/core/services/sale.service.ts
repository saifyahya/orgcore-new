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

  getAll(page: number = 0, size: number = 10, branchId?: number, startDate?: string, endDate?: string): Observable<Page<Sale>> {
    const params: any = { page, size };
    if (branchId) params.branchId = branchId;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return this.api.get<Page<Sale>>(this.path, params);
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

  /**
   * Import sales from Excel file.
   * 
   * Excel Format (Flattened/Denormalized):
   * Each row represents ONE sale item. Sale header fields are repeated for items of the same sale.
   * 
   * Columns:
   * - branchId, totalAmount, discountAmount, taxAmount, paymentMethod, channel, externalRef (sale fields)
   * - productId, quantity, unitPrice, lineTotal (item fields)
   * 
   * Backend Implementation:
   * 1. Read all rows from Excel
   * 2. Group rows by 'externalRef' (or combination of sale fields)
   * 3. For each group:
   *    - Extract sale header from first row (branchId, totalAmount, etc.)
   *    - Collect all items (productId, quantity, unitPrice, lineTotal)
   *    - Create Sale with List<SaleItemDto>
   * 
   * Example:
   * Row 1: branchId=1, totalAmount=327.50, externalRef='POS-001', productId=1, quantity=10...
   * Row 2: branchId=1, totalAmount=327.50, externalRef='POS-001', productId=2, quantity=5...
   * → Creates 1 Sale with 2 items
   */
  importFromExcel(file: File): Observable<Sale[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Sale[]>(`${this.baseUrl}${this.path}/import`, formData);
  }

  exportToPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}${this.path}/${id}/export-pdf`, {
      responseType: 'blob'
    });
  }
}
