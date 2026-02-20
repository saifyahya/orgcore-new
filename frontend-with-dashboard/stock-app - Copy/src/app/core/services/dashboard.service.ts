import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  DashboardSummary, MonthlySeries, WeeklyDaySeries,
  CategorySales, PaymentMethodSales, TopProduct
} from '../models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private base = '/dashboard';

  constructor(private api: ApiService) {}

  getSummary(branchId: number | null, startDate: string, endDate: string): Observable<DashboardSummary> {
    const params: any = { startDate, endDate };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<DashboardSummary>(`${this.base}/summary`, params);
  }

  getMonthlySales(year: number, branchId: number | null): Observable<MonthlySeries[]> {
    const params: any = { year };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<MonthlySeries[]>(`${this.base}/monthly-sales`, params);
  }

  getWeeklySalesByDay(monthDate: string, branchId: number | null): Observable<WeeklyDaySeries[]> {
    const params: any = { monthDate };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<WeeklyDaySeries[]>(`${this.base}/weekly-sales`, params);
  }

  getSalesByPaymentMethod(branchId: number | null, startDate: string, endDate: string): Observable<PaymentMethodSales[]> {
    const params: any = { startDate, endDate };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<PaymentMethodSales[]>(`${this.base}/sales-by-payment`, params);
  }

  getSalesByCategory(branchId: number | null, startDate: string, endDate: string): Observable<CategorySales[]> {
    const params: any = { startDate, endDate };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<CategorySales[]>(`${this.base}/sales-by-category`, params);
  }

  getTopProductsByQuantity(branchId: number | null, startDate: string, endDate: string): Observable<TopProduct[]> {
    const params: any = { startDate, endDate };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<TopProduct[]>(`${this.base}/top-products-qty`, params);
  }

  getTopProductsByRevenue(branchId: number | null, startDate: string, endDate: string): Observable<TopProduct[]> {
    const params: any = { startDate, endDate };
    if (branchId !== null) params.branchId = branchId;
    return this.api.get<TopProduct[]>(`${this.base}/top-products-revenue`, params);
  }

  getMonthlyProductSales(productId: number, year?: number): Observable<MonthlySeries[]> {
    return this.api.get<MonthlySeries[]>(
      `${this.base}/product-monthly-sales/${productId}`,
      year ? { year } : undefined
    );
  }
}
