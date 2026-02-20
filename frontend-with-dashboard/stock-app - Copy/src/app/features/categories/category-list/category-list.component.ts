import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CategoryService } from '../../../core/services/category.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { CategoryFormDialogComponent } from '../category-form-dialog/category-form-dialog.component';
import { Category } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-category-list',
  standalone: true,
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatPaginatorModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    TranslatePipe
  ]
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  loading = true;
  searchTerm = '';
  statusFilter: number | null = null;

  // Pagination
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  displayedColumns = ['id', 'name', 'description', 'isActive', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy', 'actions'];

  constructor(
    private categoryService: CategoryService,
    private notification: NotificationService,
    private ts: TranslationService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.categoryService.getAll(this.pageIndex, this.pageSize, this.searchTerm, this.statusFilter !== null ? this.statusFilter : undefined)
      .subscribe({
        next: (data) => {
          this.categories = data.content;
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

  openForm(category?: Category): void {
    this.dialog.open(CategoryFormDialogComponent, { width: '440px', data: category || null })
      .afterClosed().subscribe(result => { if (result) this.load(); });
  }

  delete(cat: Category): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.ts.t('CATEGORIES.DELETE_TITLE'),
        message: this.ts.t('CATEGORIES.DELETE_MESSAGE', { name: cat.name })
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.categoryService.delete(cat.id!).subscribe({
          next: () => {
            this.notification.success(this.ts.t('CATEGORIES.DELETED'));
            this.load();
          }
        });
      }
    });
  }
}
