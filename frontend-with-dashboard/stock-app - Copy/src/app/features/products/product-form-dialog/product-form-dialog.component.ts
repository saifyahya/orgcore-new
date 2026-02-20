import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProductService } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Product, Category } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

interface DialogData { product: Product | null; categories: Category[]; }

@Component({
  selector: 'app-product-form-dialog', standalone: true, templateUrl: './product-form-dialog.component.html', styleUrls: ['./product-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, TranslatePipe]
})
export class ProductFormDialogComponent implements OnInit {
  form!: FormGroup; saving = false; isEdit = false;

  constructor(private fb: FormBuilder, private productService: ProductService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<ProductFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DialogData) { }

  ngOnInit(): void {
    const p = this.data.product; this.isEdit = !!p?.id;
    this.form = this.fb.group({ name: [p?.name || '', [Validators.required]], description: [p?.description || ''], categoryId: [p?.categoryDto?.id || null], price: [p?.price || null], discount: [p?.discount || null], image: [p?.image || ''], isActive: [p?.isActive ?? 1] });
  }

  save(): void {
    if (this.form.invalid) return; this.saving = true;
    const req = this.isEdit ? this.productService.update(this.data.product!.id!, this.form.value) : this.productService.create(this.form.value);
    req.subscribe({ next: () => { this.notification.success(this.ts.t(this.isEdit ? 'PRODUCTS.UPDATED' : 'PRODUCTS.CREATED')); this.dialogRef.close(true); }, error: () => { this.saving = false; } });
  }
}
