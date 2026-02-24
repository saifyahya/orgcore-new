import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
import { forkJoin } from 'rxjs';
import { StockMovementService } from '../../../core/services/inventory.service';
import { BranchService } from '../../../core/services/branch.service';
import { ProductService } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { StockMovementFormDialogComponent } from '../stock-movement-form-dialog/stock-movement-form-dialog.component';
import { StockMovement, Branch, Product, StockMovementType } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { LocalizedCurrencyPipe } from '../../../shared/pipes/localized-currency.pipe';

@Component({
  selector: 'app-stock-movement-list', standalone: true, templateUrl: './stock-movement-list.component.html', styleUrls: ['./stock-movement-list.component.scss'],
  imports: [CommonModule, FormsModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatInputModule, MatFormFieldModule, MatDialogModule, MatProgressSpinnerModule, MatTooltipModule, MatSelectModule, TranslatePipe, LocalizedCurrencyPipe]
})
export class StockMovementListComponent implements OnInit {
  movements: StockMovement[] = []; branches: Branch[] = []; products: Product[] = []; loading = true;
  filters: { branchId: number | null; type: string | null } = { branchId: null, type: null };
  movementTypes = Object.values(StockMovementType);
  displayedColumns = ['id', 'branchId', 'productId', 'type', 'reason', 'quantity', 'unitCost', 'note', 'createdAt', 'actions'];

  constructor(private stockService: StockMovementService, private branchService: BranchService, private productService: ProductService, private notification: NotificationService, private ts: TranslationService, private dialog: MatDialog) { }

  ngOnInit(): void {
    forkJoin({ branches: this.branchService.getAll(), products: this.productService.getAll() }).subscribe({ next: ({ branches, products }) => { this.branches = branches.content; this.products = products.content; this.loadData(); } });
  }

  loadData(): void {
    this.loading = true;
    const params: any = {};
    if (this.filters.branchId) params.branchId = this.filters.branchId;
    if (this.filters.type) params.type = this.filters.type;
    this.stockService.getAll(params).subscribe({ next: (data) => { this.movements = data.content; this.loading = false; }, error: () => { this.loading = false; } });
  }

  getBranchName(id?: number): string { return this.branches.find(b => b.id === id)?.branchName || `#${id}`; }
  getProductName(id?: number): string { return this.products.find(p => p.id === id)?.name || `#${id}`; }

  openForm(): void {
    this.dialog.open(StockMovementFormDialogComponent, { width: '520px', data: { branches: this.branches, products: this.products } }).afterClosed().subscribe(result => { if (result) this.loadData(); });
  }

  delete(movement: StockMovement): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: this.ts.t('STOCK_MOVEMENTS.DELETE_TITLE'), message: this.ts.t('STOCK_MOVEMENTS.DELETE_MESSAGE') } })
      .afterClosed().subscribe(confirmed => { if (confirmed) { this.stockService.delete(movement.id!).subscribe({ next: () => { this.notification.success(this.ts.t('STOCK_MOVEMENTS.DELETED')); this.loadData(); } }); } });
  }
}
