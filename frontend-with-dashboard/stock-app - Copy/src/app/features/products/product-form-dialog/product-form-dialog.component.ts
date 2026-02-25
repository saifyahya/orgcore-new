import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Product, Category } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { environment } from '../../../../environments/environment';

interface DialogData { product: Product | null; categories: Category[]; }

@Component({
  selector: 'app-product-form-dialog', standalone: true, templateUrl: './product-form-dialog.component.html', styleUrls: ['./product-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, MatIconModule, TranslatePipe]
})
export class ProductFormDialogComponent implements OnInit {
  form!: FormGroup; saving = false; isEdit = false;
  selectedFile: File | null = null;
  imagePreviewUrl: string | null = null;
  selectedFileName: string | null = null;
  originalImageUrl: string | null = null;

  constructor(private fb: FormBuilder, private productService: ProductService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<ProductFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DialogData) { }

  ngOnInit(): void {
    const p = this.data.product; this.isEdit = !!p?.id;
    this.form = this.fb.group({ name: [p?.name || '', [Validators.required]], description: [p?.description || ''], categoryId: [p?.categoryDto?.id || null], price: [p?.price || null], isActive: [p?.isActive ?? 1] });
    
    // Set existing image if editing
    if (p?.image) {
      this.originalImageUrl = p.image;
      // Don't set imagePreviewUrl - it's only for newly selected files
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.selectedFile = file;
      this.selectedFileName = file.name;
      
      // Create preview URL
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.imagePreviewUrl = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(): void {
    this.selectedFile = null;
    this.selectedFileName = null;
    this.imagePreviewUrl = null;
    this.originalImageUrl = null;
  }

  save(): void {
    if (this.form.invalid) return; this.saving = true;
    
    // Only send file if it's a new upload (not the original image)
    const fileToSend = this.selectedFile ? this.selectedFile : undefined;
    
    const req = this.isEdit 
      ? this.productService.update(this.data.product!.id!, {...this.form.value,}, fileToSend) 
      : this.productService.create(this.form.value, fileToSend);
    
    req.subscribe({ 
      next: () => { 
        this.notification.success(this.ts.t(this.isEdit ? 'PRODUCTS.UPDATED' : 'PRODUCTS.CREATED')); 
        this.dialogRef.close(true); 
      }, 
      error: () => { 
        this.saving = false; 
      } 
    });
  }

  getImageUrl(image: string): string {
    return `${environment.apiUrl}/images/${image}`;
  }
}
