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
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { forkJoin } from 'rxjs';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { ProductFormDialogComponent } from '../product-form-dialog/product-form-dialog.component';
import { Product, Category } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-product-list',
  standalone: true,
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSelectModule,
    MatPaginatorModule,
    TranslatePipe
  ]
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  loading = true;
  searchTerm = '';
  selectedCategory: number | undefined = undefined; // Fix: undefined for params
  statusFilter: number | null = null;

  // Pagination
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  displayedColumns = ['id', 'name', 'category', 'price', 'discount', 'isActive', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy', 'actions'];

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private notification: NotificationService,
    private ts: TranslationService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    this.load();
  }

  loadCategories(): void {
    // Fetch a large page for dropdown
    this.categoryService.getAll(0, 1000).subscribe({
      next: (data) => { this.categories = data.content; }
    });
  }

  load(): void {
    this.loading = true;
    this.productService.getAll(
      this.pageIndex,
      this.pageSize,
      this.searchTerm,
      this.statusFilter !== null ? this.statusFilter : undefined,
      this.selectedCategory
    ).subscribe({
      next: (data) => {
        this.products = data.content;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.load();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openForm(product?: Product): void {
    this.dialog.open(ProductFormDialogComponent, {
      width: '520px',
      data: { product: product || null, categories: this.categories }
    }).afterClosed().subscribe(result => { if (result) this.load(); });
  }

  delete(product: Product): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.ts.t('PRODUCTS.DELETE_TITLE'),
        message: this.ts.t('PRODUCTS.DELETE_MESSAGE', { name: product.name })
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.productService.delete(product.id!).subscribe({
          next: () => {
            this.notification.success(this.ts.t('PRODUCTS.DELETED'));
            this.load();
          }
        });
      }
    });
  }
}
