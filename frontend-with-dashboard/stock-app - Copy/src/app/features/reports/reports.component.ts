import {
  Component, OnInit, OnDestroy, AfterViewInit,
  ViewChildren, QueryList, ElementRef, ChangeDetectorRef
} from '@angular/core';
import { CommonModule, DecimalPipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { forkJoin, Subscription } from 'rxjs';
import {
  Chart, ChartConfiguration,
  CategoryScale, LinearScale, BarElement, LineElement,
  PointElement, ArcElement, Tooltip, Legend, Filler,
  LineController, BarController, PieController, DoughnutController
} from 'chart.js';

import { DashboardService } from '../../core/services/dashboard.service';
import { BranchService } from '../../core/services/branch.service';
import { ThemeService } from '../../core/services/theme.service';
import { TranslationService } from '../../core/services/translation.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import {
  MonthlySeries, WeeklyDaySeries,
  CategorySales, PaymentMethodSales, TopProduct, Branch
} from '../../core/models';

Chart.register(
  CategoryScale, LinearScale, BarElement, LineElement,
  PointElement, ArcElement, Tooltip, Legend, Filler,
  LineController, BarController, PieController, DoughnutController
);

type ChartKey =
  | 'monthly-amount' | 'monthly-orders'
  | 'weekly'
  | 'category' | 'payment'
  | 'top-products';

@Component({
  selector: 'app-reports',
  standalone: true,
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss'],
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatCardModule, MatIconModule, MatButtonModule,
    MatProgressSpinnerModule, MatTabsModule,
    MatSelectModule, MatFormFieldModule, MatButtonToggleModule,
    TranslatePipe, DecimalPipe, CurrencyPipe
  ]
})
export class ReportsComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChildren('chartCanvas') canvasRefs!: QueryList<ElementRef<HTMLCanvasElement>>;

  loading = true;
  activeTab = 0;

  availableYears: number[] = [];
  currentYear = new Date().getFullYear();
  selectedYear: number | null = null;
  selectedMonth: number = new Date().getMonth() + 1;
  selectedBranch: number | null = null;
  branches: Branch[] = [];

  monthly: MonthlySeries[] = [];
  weekly: WeeklyDaySeries[] = [];
  categories: CategorySales[] = [];
  payments: PaymentMethodSales[] = [];
  topQty: TopProduct[] = [];
  topRev: TopProduct[] = [];

  topMode: 'qty' | 'rev' = 'qty';

  private charts = new Map<string, Chart>();
  private subs = new Subscription();
  private viewReady = false;
  private dataReady = false;

  readonly PALETTE = [
    '#0ea5e9', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444',
    '#64748b', '#ec4899', '#06b6d4', '#84cc16', '#f97316'
  ];

  constructor(
    private dashSvc: DashboardService,
    private branchSvc: BranchService,
    private themeSvc: ThemeService,
    private translateSvc: TranslationService,
    private cdr: ChangeDetectorRef
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

  ngAfterViewInit(): void {
    this.viewReady = true;
    if (this.dataReady) this.scheduleRender();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.destroyAll();
  }

  loadData(): void {
    this.loading = true;
    this.dataReady = false;

    const year = this.selectedYear ?? this.currentYear;
    const startDate = `${year}-01-01`;
    const endDate = `${year}-12-31`;
    const monthDate = `${year}-${String(this.selectedMonth).padStart(2, '0')}-01`;

    this.subs.add(forkJoin({
      monthly: this.dashSvc.getMonthlySales(year, this.selectedBranch),
      weekly: this.dashSvc.getWeeklySalesByDay(monthDate, this.selectedBranch),
      categories: this.dashSvc.getSalesByCategory(this.selectedBranch, startDate, endDate),
      payments: this.dashSvc.getSalesByPaymentMethod(this.selectedBranch, startDate, endDate),
      topQty: this.dashSvc.getTopProductsByQuantity(this.selectedBranch, startDate, endDate),
      topRev: this.dashSvc.getTopProductsByRevenue(this.selectedBranch, startDate, endDate)
    }).subscribe({
      next: d => {
        this.monthly = d.monthly;
        this.weekly = d.weekly;
        this.categories = d.categories;
        this.payments = d.payments;
        this.topQty = d.topQty;
        this.topRev = d.topRev;
        this.loading = false;
        this.dataReady = true;
        this.cdr.detectChanges();
        if (this.viewReady) this.scheduleRender();
      },
      error: () => { this.loading = false; }
    }));
  }

  onYearChange(): void { this.loadData(); }
  onMonthChange(): void { this.loadData(); }
  onBranchChange(): void { this.loadData(); }

  onTabChange(i: number): void {
    this.activeTab = i;
    if (this.dataReady) this.scheduleRender();
  }

  onTopModeChange(): void {
    if (this.dataReady) setTimeout(() => this.renderTopProducts(), 60);
  }

  get currentTopList(): TopProduct[] {
    return this.topMode === 'qty' ? this.topQty : this.topRev;
  }

  maxOf(list: TopProduct[], field: 'totalQuantity' | 'totalRevenue'): number {
    return Math.max(1, ...list.map(p => p[field]));
  }

  private scheduleRender(): void {
    setTimeout(() => this.renderAll(), 80);
  }

  private getCanvas(id: string): HTMLCanvasElement | null {
    if (!this.canvasRefs) return null;
    const ref = this.canvasRefs.find(r => r.nativeElement.id === id);
    return ref ? ref.nativeElement : null;
  }

  private build(key: string, id: string, cfg: ChartConfiguration): void {
    const el = this.getCanvas(id);
    if (!el) return;
    this.charts.get(key)?.destroy();
    this.charts.set(key, new Chart(el, cfg));
  }

  private destroyAll(): void {
    this.charts.forEach(c => c.destroy());
    this.charts.clear();
  }

  private gridColor = () => this.themeSvc.isDark() ? 'rgba(255,255,255,0.07)' : 'rgba(0,0,0,0.06)';
  private labelColor = () => this.themeSvc.isDark() ? '#94a3b8' : '#475569';

  private axisOpts() {
    return {
      grid: { color: this.gridColor() },
      ticks: { color: this.labelColor() }
    };
  }

  private legendOpts() {
    return { labels: { color: this.labelColor(), boxWidth: 12, padding: 14 } };
  }

  private renderAll(): void {
    this.renderMonthlyAmount();
    this.renderMonthlyOrders();
    this.renderMonthlyAmountFull();
    this.renderMonthlyOrdersFull();
    this.renderWeekly();
    this.renderCategory();
    this.renderPayment();
    this.renderTopProducts();
  }

  private renderMonthlyAmount(): void {
    this.build('monthly-amount', 'canvas-monthly-amount', {
      type: 'line',
      data: {
        labels: this.monthly.map(m => this.translateSvc.t('COMMON.' + m.monthLabel)),
        datasets: [{
          label: this.translateSvc.t('DASHBOARD.ANALYTICS.SALES_AMOUNT'),
          data: this.monthly.map(m => m.totalAmount),
          borderColor: '#0ea5e9',
          backgroundColor: 'rgba(14,165,233,0.12)',
          fill: true, tension: 0.4,
          pointBackgroundColor: '#0ea5e9', pointRadius: 4
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: this.axisOpts(), y: this.axisOpts() }
      }
    });
  }

  private renderMonthlyOrders(): void {
    this.build('monthly-orders', 'canvas-monthly-orders', {
      type: 'bar',
      data: {
        labels: this.monthly.map(m => this.translateSvc.t('COMMON.' + m.monthLabel)),
        datasets: [{
          label: this.translateSvc.t('DASHBOARD.ANALYTICS.INVOICES_COUNT'),
          data: this.monthly.map(m => m.orderCount),
          backgroundColor: '#38bdf8',
          borderRadius: 6, borderSkipped: false
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: this.axisOpts(), y: this.axisOpts() }
      }
    });
  }

  private renderMonthlyAmountFull(): void {
    this.build('monthly-amount-full', 'canvas-monthly-amount-full', {
      type: 'line',
      data: {
        labels: this.monthly.map(m => this.translateSvc.t('COMMON.' + m.monthLabel)),
        datasets: [{
          label: this.translateSvc.t('DASHBOARD.ANALYTICS.SALES_AMOUNT'),
          data: this.monthly.map(m => m.totalAmount),
          borderColor: '#0ea5e9',
          backgroundColor: 'rgba(14,165,233,0.12)',
          fill: true, tension: 0.4,
          pointBackgroundColor: '#0ea5e9', pointRadius: 4
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: this.axisOpts(), y: this.axisOpts() }
      }
    });
  }

  private renderMonthlyOrdersFull(): void {
    this.build('monthly-orders-full', 'canvas-monthly-orders-full', {
      type: 'bar',
      data: {
        labels: this.monthly.map(m => this.translateSvc.t('COMMON.' + m.monthLabel)),
        datasets: [{
          label: this.translateSvc.t('DASHBOARD.ANALYTICS.INVOICES_COUNT'),
          data: this.monthly.map(m => m.orderCount),
          backgroundColor: '#38bdf8',
          borderRadius: 6, borderSkipped: false
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: this.axisOpts(), y: this.axisOpts() }
      }
    });
  }

  private renderWeekly(): void {
    this.build('weekly', 'canvas-weekly', {
      type: 'bar',
      data: {
        labels: this.weekly.map(w => this.translateSvc.t('COMMON.' + w.dayLabel)),
        datasets: [
          {
            label: this.translateSvc.t('DASHBOARD.ANALYTICS.AMOUNT'), 
            data: this.weekly.map(w => w.totalAmount),
            backgroundColor: '#0ea5e9', borderRadius: 6, yAxisID: 'yAmt'
          },
          {
            label: this.translateSvc.t('DASHBOARD.ANALYTICS.INVOICES_COUNT'), 
            data: this.weekly.map(w => w.orderCount),
            backgroundColor: '#8b5cf6', borderRadius: 6, yAxisID: 'yOrd'
          }
        ]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: this.legendOpts() },
        scales: {
          x: this.axisOpts(),
          yAmt: { ...this.axisOpts(), position: 'left' },
          yOrd: { ...{ grid: { display: false }, ticks: { color: this.labelColor() } }, position: 'right' }
        }
      }
    });
  }

  private renderCategory(): void {
    const labels = this.categories.map(c => c.categoryName);
    const data = this.categories.map(c => c.totalRevenue);
    this.build('category', 'canvas-category', {
      type: 'pie',
      data: { labels, datasets: [{ data, backgroundColor: this.PALETTE, hoverOffset: 8 }] },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { ...this.legendOpts(), position: 'right' } }
      }
    });
  }

  private renderPayment(): void {
    const labels = this.payments.map(p => this.translateSvc.t('COMMON.' + p.paymentMethod));
    const data = this.payments.map(p => p.totalAmount);
    this.build('payment', 'canvas-payment', {
      type: 'doughnut',
      data: { labels, datasets: [{ data, backgroundColor: this.PALETTE, hoverOffset: 8, borderWidth: 2 }] },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: { legend: { ...this.legendOpts(), position: 'right' } }
      }
    } as ChartConfiguration<'doughnut'>);
  }

  renderTopProducts(): void {
    const src = this.topMode === 'qty' ? this.topQty : this.topRev;
    const labels = src.map(p => p.productName);
    const data = this.topMode === 'qty' ? src.map(p => p.totalQuantity) : src.map(p => p.totalRevenue);
    const colors = src.map((_, i) => this.PALETTE[i % this.PALETTE.length]);

    this.build('top-products', 'canvas-top-products', {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: this.topMode === 'qty' 
            ? this.translateSvc.t('DASHBOARD.ANALYTICS.UNITS_SOLD') 
            : this.translateSvc.t('DASHBOARD.ANALYTICS.REVENUE'),
          data, backgroundColor: colors, borderRadius: 6, borderSkipped: false
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { ...this.axisOpts(), ticks: { ...this.axisOpts().ticks, font: { size: 11 }, maxRotation: 45, minRotation: 0 } },
          y: this.axisOpts()
        }
      }
    });
  }

  get totalCatRevenue(): number {
    return this.categories.reduce((s, c) => s + c.totalRevenue, 0) || 1;
  }

  catPct(c: CategorySales): number {
    return Math.round((c.totalRevenue / this.totalCatRevenue) * 100);
  }

  topBarPct(p: TopProduct): number {
    const max = this.maxOf(this.currentTopList, this.topMode === 'qty' ? 'totalQuantity' : 'totalRevenue');
    return Math.round(((this.topMode === 'qty' ? p.totalQuantity : p.totalRevenue) / max) * 100);
  }
}
