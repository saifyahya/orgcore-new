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
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { UserService, UserPageFilter } from '../../../core/services/user.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { BranchService } from '../../../core/services/branch.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { UserFormDialogComponent } from '../user-form-dialog/user-form-dialog.component';
import { User, Branch } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-user-list',
  standalone: true,
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
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
    MatSlideToggleModule,
    TranslatePipe
  ]
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  branches: Branch[] = [];
  loading = true;
  loadingBranches = false;
  searchTerm = '';
  statusFilter: number | null = 1;
  branchFilter: number | null = null;
  sortBy = 'id';
  sortDir = 'desc';
  showAuditColumns = false;

  // Pagination
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  displayedColumns: string[] = [];
  private baseColumns = ['id', 'firstName', 'lastName', 'email', 'branch', 'isActive'];
  private auditColumns = ['createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
  private actionColumns = ['actions'];

  constructor(
    private userService: UserService,
    private branchService: BranchService,
    private notification: NotificationService,
    private ts: TranslationService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.updateDisplayedColumns();
    this.loadBranches();
    this.load();
  }

  loadBranches(): void {
    this.loadingBranches = true;
    this.branchService.getAll(0, 100).subscribe({
      next: (data) => {
        this.branches = data.content;
        this.loadingBranches = false;
      },
      error: () => {
        this.loadingBranches = false;
      }
    });
  }

  updateDisplayedColumns(): void {
    this.displayedColumns = [
      ...this.baseColumns,
      ...(this.showAuditColumns ? this.auditColumns : []),
      ...this.actionColumns
    ];
  }

  onAuditColumnsToggle(): void {
    this.updateDisplayedColumns();
  }

  load(): void {
    this.loading = true;
    const filter: UserPageFilter = {
      page: this.pageIndex,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      search: this.searchTerm || undefined,
      isActive: this.statusFilter !== null ? this.statusFilter : undefined,
      branchId: this.branchFilter !== null ? this.branchFilter : undefined
    };

    this.userService.getAll(filter).subscribe({
      next: (data) => {
        this.users = data.content;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
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

  openForm(user?: User): void {
    this.dialog.open(UserFormDialogComponent, { width: '480px', data: user || null })
      .afterClosed().subscribe(result => {
        if (result) this.load();
      });
  }

  delete(user: User): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.ts.t('USERS.DELETE_TITLE'),
        message: this.ts.t('USERS.DELETE_MESSAGE', { name: `${user.firstName} ${user.lastName}` })
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.userService.delete(user.id!).subscribe({
          next: () => {
            this.notification.success(this.ts.t('USERS.DELETED'));
            this.load();
          }
        });
      }
    });
  }
}
