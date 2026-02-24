import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { SaleService } from '../../../core/services/sale.service';
import { BranchService } from '../../../core/services/branch.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ExcelService } from '../../../core/services/excel.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { SaleImportDialogComponent } from '../sale-import-dialog/sale-import-dialog.component';
import { Sale, Branch } from '../../../core/models';
import { forkJoin } from 'rxjs';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { LocalizedCurrencyPipe } from '../../../shared/pipes/localized-currency.pipe';

@Component({
  selector: 'app-sale-list', standalone: true, templateUrl: './sale-list.component.html', styleUrls: ['./sale-list.component.scss'],
  imports: [CommonModule, FormsModule, RouterLink, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatInputModule, MatFormFieldModule, MatDialogModule, MatProgressSpinnerModule, MatTooltipModule, MatSelectModule, MatDatepickerModule, MatNativeDateModule, MatPaginatorModule, TranslatePipe, LocalizedCurrencyPipe]
})
export class SaleListComponent implements OnInit {
  sales: Sale[] = []; filtered: Sale[] = []; branches: Branch[] = []; loading = true;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  filters: { branchId: number | null; from: Date | null; to: Date | null } = { branchId: null, from: null, to: null };
  displayedColumns = ['id', 'branch', 'items', 'totalAmount', 'paymentMethod', 'channel', 'createdAt', 'createdBy', 'updatedBy', 'updatedAt', 'actions'];

  get totalRevenue(): number { return this.filtered.reduce((sum, s) => sum + (s.finalAmount || 0), 0); }

  constructor(private saleService: SaleService, private branchService: BranchService, private notification: NotificationService, private ts: TranslationService, private excelService: ExcelService, private dialog: MatDialog) { }

  ngOnInit(): void {
    // Set default date range to current month
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
    const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    this.filters.from = firstDay;
    this.filters.to = lastDay;
    
    forkJoin({ branches: this.branchService.getAll() }).subscribe(({ branches }) => { 
      this.branches = branches.content; 
      this.loadSales(); 
    });
  }

  loadSales(): void {
    this.loading = true;
    const branchId = this.filters.branchId ? this.filters.branchId : undefined;
    const startDate = this.filters.from ? this.filters.from.toISOString().split('T')[0] : undefined;
    const endDate = this.filters.to ? this.filters.to.toISOString().split('T')[0] : undefined;
    
    this.saleService.getAll(this.pageIndex, this.pageSize, branchId, startDate, endDate).subscribe({ 
      next: (data) => { 
        this.sales = data.content; 
        this.filtered = data.content; 
        this.totalElements = data.totalElements; 
        this.loading = false; 
      }, 
      error: () => { 
        this.loading = false; 
      } 
    });
  }

  applyFilters(): void { 
    this.pageIndex = 0; // Reset to first page when filtering
    this.loadSales(); 
  }
  
  clearFilters(): void { 
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
    const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    this.filters = { branchId: null, from: firstDay, to: lastDay }; 
    this.pageIndex = 0;
    this.loadSales(); 
  }
  
  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSales();
  }

  openImport(): void {
    this.dialog.open(SaleImportDialogComponent, { width: '560px', data: { branches: this.branches } }).afterClosed().subscribe(result => { if (result) this.loadSales(); });
  }

  delete(sale: Sale): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: this.ts.t('SALES.DELETE_TITLE'), message: this.ts.t('SALES.DELETE_MESSAGE', { id: sale.id! }) } })
      .afterClosed().subscribe(confirmed => {
        if (confirmed) { this.saleService.delete(sale.id!).subscribe({ next: () => { this.notification.success(this.ts.t('SALES.DELETED')); this.loadSales(); } }); }
      });
  }
}
