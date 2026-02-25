import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DecimalPipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { forkJoin, Subscription } from 'rxjs';

import { DashboardService } from '../../core/services/dashboard.service';
import { SaleService } from '../../core/services/sale.service';
import { BranchService } from '../../core/services/branch.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { LocalizedCurrencyPipe } from '../../shared/pipes/localized-currency.pipe';
import { DashboardSummary, Branch } from '../../core/models';

interface KpiCard {
  key: string;
  value: string | number;
  icon: string;
  color: string;
  route: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatCardModule, MatIconModule, MatButtonModule,
    MatProgressSpinnerModule, MatSelectModule, MatFormFieldModule,
    TranslatePipe, DecimalPipe, CurrencyPipe, LocalizedCurrencyPipe
  ]
})
export class DashboardComponent implements OnInit, OnDestroy {

  loading = true;

  availableYears: number[] = [];
  currentYear = new Date().getFullYear();
  selectedYear: number | null = null;
  selectedBranch: number | null = null;
  branches: Branch[] = [];

  summary: DashboardSummary | null = null;
  kpiCards: KpiCard[] = [];
  recentSales: any[] = [];

  private subs = new Subscription();

  constructor(
    private dashSvc: DashboardService,
    private saleSvc: SaleService,
    private branchSvc: BranchService
  ) { }

  ngOnInit(): void {
    this.availableYears = [2025, 2026, 2027, 2028, 2029, 2030, 2031, 2032, 2033, 2034, 2035];
    this.selectedYear = this.currentYear;
    this.loadBranches();
    this.loadData();
  }

  loadBranches(): void {
    this.branchSvc.getAll(0, 100, '', 1).subscribe({
      next: (data) => {
        this.branches = data.content;
      },
      error: () => {
        this.branches = [];
      }
    });
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadData(): void {
    this.loading = true;

    const year = this.selectedYear ?? this.currentYear;
    const startDate = `${year}-01-01`;
    const endDate = `${year}-12-31`;

    this.subs.add(forkJoin({
      summary: this.dashSvc.getSummary(this.selectedBranch, startDate, endDate),
      sales: this.saleSvc.getAll(0, 1000000,this.selectedBranch ? this.selectedBranch : undefined, startDate, endDate)
    }).subscribe({
      next: d => {
        this.summary = d.summary;
        this.recentSales = (d.sales.content ?? []).slice(0, 10);
        this.buildKpiCards();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    }));
  }

  onYearChange(): void { this.loadData(); }
  onBranchChange(): void { this.loadData(); }

  buildKpiCards(): void {
    const s = this.summary!;
    this.kpiCards = [
      { key: 'DASHBOARD.ANALYTICS.KPI_TOTAL_SALES', value: this.fmtAmt(s.totalSalesAmount), icon: 'payments', color: '#0ea5e9', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_TOTAL_ORDERS', value: s.totalOrders, icon: 'receipt_long', color: '#8b5cf6', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_PRODUCTS', value: s.totalProducts, icon: 'shopping_cart', color: '#10b981', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_AVG_ORDER', value: this.fmtAmt(s.avgOrderValue), icon: 'receipt', color: '#f59e0b', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_DISCOUNT', value: this.fmtAmt(s.totalDiscountAmount), icon: 'discount', color: '#ef4444', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_TAX', value: this.fmtAmt(s.totalTaxAmount), icon: 'calculate', color: '#64748b', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_FINAL_AMOUNT', value: this.fmtAmt(s.finalAmount), icon: 'account_balance_wallet', color: '#ec4899', route: '/sales' },
      { key: 'DASHBOARD.ANALYTICS.KPI_CATEGORIES', value: s.totalCategories, icon: 'category', color: '#06b6d4', route: '/categories' },
      { key: 'DASHBOARD.ANALYTICS.KPI_BRANCHES', value: s.activeBranches, icon: 'store', color: '#84cc16', route: '/branches' },
    ];
  }

  fmtAmt(v: number): string {
    if (!v) return '0 <span class="currency">JOD</span>';
    // Format with up to 4 decimal places, with thousands separators
    return v.toLocaleString('en-US', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 4
    }) + ' <span class="currency">JOD</span>';
  }
}
