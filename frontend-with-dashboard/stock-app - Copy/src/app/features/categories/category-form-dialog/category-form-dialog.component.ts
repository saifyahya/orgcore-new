import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CategoryService } from '../../../core/services/category.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Category } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({ selector: 'app-category-form-dialog', standalone: true, templateUrl: './category-form-dialog.component.html', styleUrls: ['./category-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, TranslatePipe]
})
export class CategoryFormDialogComponent implements OnInit {
  form!: FormGroup; saving = false; isEdit = false;

  constructor(private fb: FormBuilder, private categoryService: CategoryService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<CategoryFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: Category | null) {}

  ngOnInit(): void {
    this.isEdit = !!this.data?.id;
    this.form = this.fb.group({ name: [this.data?.name || '', [Validators.required]], description: [this.data?.description || ''], isActive: [this.data?.isActive ?? 1] });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;
    const req = this.isEdit ? this.categoryService.update(this.data!.id!, this.form.value) : this.categoryService.create(this.form.value);
    req.subscribe({ next: () => { this.notification.success(this.ts.t(this.isEdit ? 'CATEGORIES.UPDATED' : 'CATEGORIES.CREATED')); this.dialogRef.close(true); }, error: () => { this.saving = false; } });
  }
}
