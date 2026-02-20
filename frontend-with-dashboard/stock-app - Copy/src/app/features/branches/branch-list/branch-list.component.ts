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
import { BranchService } from '../../../core/services/branch.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { BranchFormDialogComponent } from '../branch-form-dialog/branch-form-dialog.component';
import { Branch } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-branch-list',
  standalone: true,
  templateUrl: './branch-list.component.html',
  styleUrls: ['./branch-list.component.scss'],
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
export class BranchListComponent implements OnInit {
  branches: Branch[] = [];
  loading = true;
  searchTerm = '';
  statusFilter: number | null = null;

  // Pagination
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  displayedColumns = ['id', 'branchName', 'address', 'isActive', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy', 'actions'];

  constructor(
    private branchService: BranchService,
    private notification: NotificationService,
    private ts: TranslationService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.branchService.getAll(this.pageIndex, this.pageSize, this.searchTerm, this.statusFilter !== null ? this.statusFilter : undefined)
      .subscribe({
        next: (data) => {
          this.branches = data.content;
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

  openForm(branch?: Branch): void {
    this.dialog.open(BranchFormDialogComponent, { width: '480px', data: branch || null })
      .afterClosed().subscribe(result => { if (result) this.load(); });
  }

  delete(branch: Branch): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.ts.t('BRANCHES.DELETE_TITLE'),
        message: this.ts.t('BRANCHES.DELETE_MESSAGE', { name: branch.branchName })
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.branchService.delete(branch.id!).subscribe({
          next: () => {
            this.notification.success(this.ts.t('BRANCHES.DELETED'));
            this.load();
          }
        });
      }
    });
  }
}
