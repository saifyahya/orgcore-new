import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { InventoryService } from '../../../core/services/inventory.service';
import { ProductService } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Inventory, Branch, Product } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

interface DialogData { inventory: Inventory | null; branches: Branch[]; }

@Component({
  selector: 'app-inventory-form-dialog', standalone: true, templateUrl: './inventory-form-dialog.component.html', styleUrls: ['./inventory-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, TranslatePipe]
})
export class InventoryFormDialogComponent implements OnInit {
  form!: FormGroup; saving = false; isEdit = false; products: Product[] = [];

  constructor(private fb: FormBuilder, private inventoryService: InventoryService, private productService: ProductService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<InventoryFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DialogData) { }

  ngOnInit(): void {
    const inv = this.data.inventory; this.isEdit = !!inv?.id;
    this.form = this.fb.group({ branchId: [inv?.branch?.id || null, Validators.required], productId: [inv?.product?.id || null, Validators.required], quantity: [inv?.quantity ?? 0, [Validators.required, Validators.min(0)]] });
    this.productService.getAll().subscribe(p => this.products = p.content);
  }

  save(): void {
    if (this.form.invalid) return; this.saving = true;
    const req = this.isEdit ? this.inventoryService.update(this.data.inventory!.id!, this.form.value) : this.inventoryService.create(this.form.value);
    req.subscribe({ next: () => { this.notification.success(this.ts.t(this.isEdit ? 'INVENTORY.UPDATED' : 'INVENTORY.CREATED')); this.dialogRef.close(true); }, error: () => { this.saving = false; } });
  }
}
