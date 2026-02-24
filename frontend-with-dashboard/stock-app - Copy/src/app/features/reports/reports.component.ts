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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { forkJoin, Subscription } from 'rxjs';
import {
  Chart, ChartConfiguration,
  CategoryScale, LinearScale, BarElement, LineElement,
  PointElement, ArcElement, Tooltip, Legend, Filler,
  LineController, BarController, PieController, DoughnutController
} from 'chart.js';

import { DashboardService } from '../../core/services/dashboard.service';
import { BranchService } from '../../core/services/branch.service';
import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { ThemeService } from '../../core/services/theme.service';
import { TranslationService } from '../../core/services/translation.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { LocalizedCurrencyPipe } from '../../shared/pipes/localized-currency.pipe';
import {
  MonthlySeries, WeeklyDaySeries,
  CategorySales, PaymentMethodSales, TopProduct, Branch, Product, Category
} from '../../core/models';
import { environment } from 'src/environments/environment';

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
    MatDatepickerModule, MatInputModule, MatNativeDateModule,
    TranslatePipe, DecimalPipe, CurrencyPipe, LocalizedCurrencyPipe
  ]
})
export class ReportsComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChildren('chartCanvas') canvasRefs!: QueryList<ElementRef<HTMLCanvasElement>>;

  loading = true;
  activeTab = 0;

  availableYears: number[] = [];
  currentYear = new Date().getFullYear();
  selectedBranch: number | null = null;
  branches: Branch[] = [];
  
  // Monthly tab filters
  monthlyYear: number = new Date().getFullYear();
  
  // Weekly tab filters
  weeklyDate: Date = new Date();
  
  // Top Products tab filters
  products: Product[] = [];
  filteredProducts: Product[] = [];
  selectedProduct: number | null = null;
  productSearchText: string = '';
  topProductsStartDate: Date = new Date(new Date().getFullYear(), 0, 1); // Jan 1 of current year
  topProductsEndDate: Date = new Date(); // Today
  
  // Categories tab filters
  allCategories: Category[] = [];
  filteredCategories: Category[] = [];
  selectedCategory: number | null = null;
  categorySearchText: string = '';
  categoriesStartDate: Date = new Date(new Date().getFullYear(), 0, 1); // Jan 1 of current year
  categoriesEndDate: Date = new Date(); // Today
  
  // Payments tab filters
  paymentMethods = ['CASH', 'CARD', 'TRANSFER', 'OTHER'];
  selectedPaymentMethod: string | null = null;
  paymentsStartDate: Date = new Date(new Date().getFullYear(), 0, 1); // Jan 1 of current year
  paymentsEndDate: Date = new Date(); // Today

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
  private tabDataLoaded = [false, false, false, false, false, false]; // Track which tabs have loaded data

  readonly PALETTE = [
    '#0ea5e9', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444',
    '#64748b', '#ec4899', '#06b6d4', '#84cc16', '#f97316'
  ];

  constructor(
    private dashSvc: DashboardService,
    private branchSvc: BranchService,
    private productSvc: ProductService,
    private categorySvc: CategoryService,
    private themeSvc: ThemeService,
    private translateSvc: TranslationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.availableYears = [2025, 2026, 2027, 2028, 2029, 2030, 2031, 2032, 2033, 2034, 2035];
    this.monthlyYear = this.currentYear;
    this.loadBranches();
    this.loadProducts();
    this.loadCategories();
    // Load data for the first tab (Overview)
    this.loadOverviewData();
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

  loadProducts(): void {
    this.productSvc.getAll(0, 10000, '', 1).subscribe({
      next: (data) => {
        this.products = data.content;
        this.filteredProducts = this.products;
      },
      error: () => {
        this.products = [];
        this.filteredProducts = [];
      }
    });
  }

  filterProducts(): void {
    const searchLower = this.productSearchText.toLowerCase();
    this.filteredProducts = this.products.filter(p => 
      p.name.toLowerCase().includes(searchLower)
    );
  }

  loadCategories(): void {
    this.categorySvc.getAll(0, 10000, '', 1).subscribe({
      next: (data) => {
        this.allCategories = data.content;
        this.filteredCategories = this.allCategories;
      },
      error: () => {
        this.allCategories = [];
        this.filteredCategories = [];
      }
    });
  }

  filterCategories(): void {
    const searchLower = this.categorySearchText.toLowerCase();
    this.filteredCategories = this.allCategories.filter(c => 
      c.name.toLowerCase().includes(searchLower)
    );
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    if (this.dataReady) this.scheduleRender();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.destroyAll();
  }

  // Tab-specific data loading methods
  loadOverviewData(): void {
    this.loading = true;
    const year = this.monthlyYear;
    const startDate = `${year}-01-01`;
    const endDate = `${year}-12-31`;

    this.subs.add(forkJoin({
      monthly: this.dashSvc.getMonthlySales(year, this.selectedBranch),
      categories: this.dashSvc.getSalesByCategory(this.selectedBranch, null, startDate, endDate),
      payments: this.dashSvc.getSalesByPaymentMethod(this.selectedBranch, null, startDate, endDate)
    }).subscribe({
      next: d => {
        this.monthly = d.monthly;
        this.categories = d.categories;
        this.payments = d.payments;
        this.loading = false;
        this.dataReady = true;
        this.tabDataLoaded[0] = true;
        this.cdr.detectChanges();
        if (this.viewReady) {
          setTimeout(() => {
            this.renderMonthlyAmount();
            this.renderMonthlyOrders();
            this.renderCategory();
            this.renderPayment();
          }, 80);
        }
      },
      error: () => { this.loading = false; }
    }));
  }

  loadMonthlyData(): void {
    this.loading = true;
    const year = this.monthlyYear;

    this.subs.add(this.dashSvc.getMonthlySales(year, this.selectedBranch).subscribe({
      next: data => {
        this.monthly = data;
        this.loading = false;
        this.tabDataLoaded[1] = true;
        this.cdr.detectChanges();
        if (this.viewReady) this.scheduleRenderMonthly();
      },
      error: () => { this.loading = false; }
    }));
  }

  loadWeeklyData(): void {
    this.loading = true;
    const weeklyDateStr = this.formatDate(this.weeklyDate);

    this.subs.add(this.dashSvc.getWeeklySalesByDay(weeklyDateStr, this.selectedBranch).subscribe({
      next: data => {
        this.weekly = data;
        this.loading = false;
        this.tabDataLoaded[2] = true;
        this.cdr.detectChanges();
        if (this.viewReady) this.scheduleRenderWeekly();
      },
      error: () => { this.loading = false; }
    }));
  }

  loadTopProductsData(): void {
    this.loading = true;
    const topStartDate = this.formatDate(this.topProductsStartDate);
    const topEndDate = this.formatDate(this.topProductsEndDate);

    this.subs.add(forkJoin({
      topQty: this.dashSvc.getTopProductsByQuantity(this.selectedBranch, this.selectedProduct, topStartDate, topEndDate),
      topRev: this.dashSvc.getTopProductsByRevenue(this.selectedBranch, this.selectedProduct, topStartDate, topEndDate)
    }).subscribe({
      next: d => {
        this.topQty = d.topQty;
        this.topRev = d.topRev;
        this.loading = false;
        this.tabDataLoaded[3] = true;
        this.cdr.detectChanges();
        if (this.viewReady) this.scheduleRenderTopProducts();
      },
      error: () => { this.loading = false; }
    }));
  }

  loadCategoriesData(): void {
    this.loading = true;
    const catStartDate = this.formatDate(this.categoriesStartDate);
    const catEndDate = this.formatDate(this.categoriesEndDate);

    this.subs.add(this.dashSvc.getSalesByCategory(this.selectedBranch, this.selectedCategory, catStartDate, catEndDate).subscribe({
      next: data => {
        this.categories = data;
        this.loading = false;
        this.tabDataLoaded[4] = true;
        this.cdr.detectChanges();
        if (this.viewReady) this.scheduleRenderCategories();
      },
      error: () => { this.loading = false; }
    }));
  }

  loadPaymentsData(): void {
    this.loading = true;
    const payStartDate = this.formatDate(this.paymentsStartDate);
    const payEndDate = this.formatDate(this.paymentsEndDate);

    this.subs.add(this.dashSvc.getSalesByPaymentMethod(this.selectedBranch, this.selectedPaymentMethod, payStartDate, payEndDate).subscribe({
      next: data => {
        this.payments = data;
        this.loading = false;
        this.tabDataLoaded[5] = true;
        this.cdr.detectChanges();
        if (this.viewReady) this.scheduleRenderPayments();
      },
      error: () => { this.loading = false; }
    }));
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onBranchChange(): void { 
    // Mark all tabs as needing reload when branch changes
    this.tabDataLoaded = [false, false, false, false, false, false];
    // Reload current tab's data
    switch (this.activeTab) {
      case 0: this.loadOverviewData(); break;
      case 1: this.loadMonthlyData(); break;
      case 2: this.loadWeeklyData(); break;
      case 3: this.loadTopProductsData(); break;
      case 4: this.loadCategoriesData(); break;
      case 5: this.loadPaymentsData(); break;
    }
  }
  
  onMonthlyYearChange(): void { 
    this.loadMonthlyData(); 
  }
  
  onWeeklyDateChange(): void { 
    this.loadWeeklyData(); 
  }
  
  onTopProductsFilterChange(): void { 
    this.loadTopProductsData(); 
  }
  
  onCategoriesFilterChange(): void {
    this.loadCategoriesData();
  }
  
  onPaymentsFilterChange(): void {
    this.loadPaymentsData();
  }

  onTabChange(i: number): void {
    // Ensure activeTab is set (redundant with two-way binding but ensures correct order)
    this.activeTab = i;
    this.loadDataForCurrentTab();
  }

  loadDataForCurrentTab(): void {
    // Load data for the current tab if not already loaded
    if (this.tabDataLoaded[this.activeTab]) {
      // Data already loaded, just render
      if (this.dataReady && this.viewReady) {
        switch (this.activeTab) {
          case 0: setTimeout(() => { this.renderMonthlyAmount(); this.renderMonthlyOrders(); this.renderCategory(); this.renderPayment(); }, 80); break;
          case 1: this.scheduleRenderMonthly(); break;
          case 2: this.scheduleRenderWeekly(); break;
          case 3: this.scheduleRenderTopProducts(); break;
          case 4: this.scheduleRenderCategories(); break;
          case 5: this.scheduleRenderPayments(); break;
        }
      }
      return;
    }

    switch (this.activeTab) {
      case 0: // Overview
        this.loadOverviewData();
        break;
      case 1: // Monthly
        this.loadMonthlyData();
        break;
      case 2: // Weekly
        this.loadWeeklyData();
        break;
      case 3: // Top Products
        this.loadTopProductsData();
        break;
      case 4: // Categories
        this.loadCategoriesData();
        break;
      case 5: // Payments
        this.loadPaymentsData();
        break;
    }
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

  private scheduleRenderMonthly(): void {
    setTimeout(() => {
      this.renderMonthlyAmountFull();
      this.renderMonthlyOrdersFull();
    }, 80);
  }

  private scheduleRenderWeekly(): void {
    setTimeout(() => {
      this.renderWeekly();
    }, 80);
  }

  private scheduleRenderTopProducts(): void {
    setTimeout(() => {
      this.renderTopProducts();
    }, 80);
  }

  private scheduleRenderCategories(): void {
    setTimeout(() => {
      this.renderCategoryFull();
    }, 80);
  }

  private scheduleRenderPayments(): void {
    setTimeout(() => {
      this.renderPaymentFull();
    }, 80);
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
        labels: this.weekly.map(w => `${this.translateSvc.t('COMMON.' + w.dayLabel)} - ${w.saleDate}`),
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

  private renderCategoryFull(): void {
    const labels = this.categories.map(c => c.categoryName);
    const data = this.categories.map(c => c.totalRevenue);
    this.build('category-full', 'canvas-category-full', {
      type: 'pie',
      data: { labels, datasets: [{ data, backgroundColor: this.PALETTE, hoverOffset: 8 }] },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { ...this.legendOpts(), position: 'right' } }
      }
    });
  }

  private renderPaymentFull(): void {
    const labels = this.payments.map(p => this.translateSvc.t('COMMON.' + p.paymentMethod));
    const data = this.payments.map(p => p.totalAmount);
    this.build('payment-full', 'canvas-payment-full', {
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

    getImageUrl(image: string): string {
      return `${environment.apiUrl}/images/${image}`;
    }
}
