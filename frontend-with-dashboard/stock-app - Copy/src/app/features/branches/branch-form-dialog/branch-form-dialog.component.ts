import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BranchService } from '../../../core/services/branch.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Branch } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-branch-form-dialog',
  standalone: true,
  templateUrl: './branch-form-dialog.component.html',
  styleUrls: ['./branch-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, TranslatePipe]
})
export class BranchFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  isEdit = false;

  constructor(private fb: FormBuilder, private branchService: BranchService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<BranchFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: Branch | null) {}

  ngOnInit(): void {
    this.isEdit = !!this.data?.id;
    this.form = this.fb.group({ branchName: [this.data?.branchName || '', [Validators.required, Validators.minLength(2)]], address: [this.data?.address || ''], isActive: [this.data?.isActive ?? 1] });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;
    const req = this.isEdit ? this.branchService.update(this.data!.id!, this.form.value) : this.branchService.create(this.form.value);
    req.subscribe({ next: () => { this.notification.success(this.ts.t(this.isEdit ? 'BRANCHES.UPDATED' : 'BRANCHES.CREATED')); this.dialogRef.close(true); }, error: () => { this.saving = false; } });
  }
}
