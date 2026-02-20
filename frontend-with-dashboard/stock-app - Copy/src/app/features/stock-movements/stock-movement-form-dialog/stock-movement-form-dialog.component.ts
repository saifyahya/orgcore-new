import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StockMovementService } from '../../../core/services/inventory.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Branch, Product, StockMovementType, StockMovementReason, ReferenceType } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

interface DialogData { branches: Branch[]; products: Product[]; }

@Component({ selector: 'app-stock-movement-form-dialog', standalone: true, templateUrl: './stock-movement-form-dialog.component.html', styleUrls: ['./stock-movement-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, TranslatePipe]
})
export class StockMovementFormDialogComponent implements OnInit {
  form!: FormGroup; saving = false;
  movementTypes = Object.values(StockMovementType); movementReasons = Object.values(StockMovementReason); refTypes = Object.values(ReferenceType);

  constructor(private fb: FormBuilder, private stockService: StockMovementService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<StockMovementFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DialogData) {}

  ngOnInit(): void {
    this.form = this.fb.group({ branchId: [null, Validators.required], productId: [null, Validators.required], type: [StockMovementType.IN, Validators.required], reason: [StockMovementReason.PURCHASE, Validators.required], quantity: [1, [Validators.required, Validators.min(1)]], unitCost: [null], refType: [null], refId: [''], note: [''] });
  }

  save(): void {
    if (this.form.invalid) return; this.saving = true;
    const v = this.form.value;
    this.stockService.create({ ...v, refId: v.refId || undefined, refType: v.refType || undefined, note: v.note || undefined }).subscribe({
      next: () => { this.notification.success(this.ts.t('STOCK_MOVEMENTS.RECORDED')); this.dialogRef.close(true); },
      error: () => { this.saving = false; }
    });
  }
}
