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
import { InventoryService } from '../../../core/services/inventory.service';
import { BranchService } from '../../../core/services/branch.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog.component';
import { InventoryFormDialogComponent } from '../inventory-form-dialog/inventory-form-dialog.component';
import { Inventory, Branch } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-inventory-list', standalone: true, templateUrl: './inventory-list.component.html', styleUrls: ['./inventory-list.component.scss'],
  imports: [CommonModule, FormsModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatInputModule, MatFormFieldModule, MatDialogModule, MatProgressSpinnerModule, MatTooltipModule, MatSelectModule, TranslatePipe]
})
export class InventoryListComponent implements OnInit {
  inventory: Inventory[] = []; filtered: Inventory[] = []; branches: Branch[] = [];
  loading = true; searchTerm = ''; selectedBranch: number | null = null;
  displayedColumns = ['id', 'branch', 'product', 'category', 'quantity', 'actions'];

  constructor(private inventoryService: InventoryService, private branchService: BranchService, private notification: NotificationService, private ts: TranslationService, private dialog: MatDialog) { }

  ngOnInit(): void { this.branchService.getAll().subscribe(branches => { this.branches = branches.content; this.load(); }); }

  load(): void {
    this.loading = true;
    this.inventoryService.getAll(this.selectedBranch || undefined).subscribe({ next: (data) => { this.inventory = data.content; this.filtered = data.content; this.loading = false; }, error: () => { this.loading = false; } });
  }

  filterData(): void {
    let result = this.inventory;
    if (this.selectedBranch) result = result.filter(i => i.branch?.id === this.selectedBranch);
    if (this.searchTerm) { const t = this.searchTerm.toLowerCase(); result = result.filter(i => (i.product?.name || '').toLowerCase().includes(t)); }
    this.filtered = result;
  }

  openForm(inventory?: Inventory): void {
    this.dialog.open(InventoryFormDialogComponent, { width: '440px', data: { inventory: inventory || null, branches: this.branches } }).afterClosed().subscribe(result => { if (result) this.load(); });
  }

  delete(inv: Inventory): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: this.ts.t('INVENTORY.DELETE_TITLE'), message: this.ts.t('INVENTORY.DELETE_MESSAGE') } })
      .afterClosed().subscribe(confirmed => { if (confirmed) { this.inventoryService.delete(inv.id!).subscribe({ next: () => { this.notification.success(this.ts.t('INVENTORY.DELETED')); this.load(); } }); } });
  }
}
