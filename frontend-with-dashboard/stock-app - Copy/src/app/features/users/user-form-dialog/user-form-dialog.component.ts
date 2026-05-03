import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../../core/services/user.service';
import { BranchService } from '../../../core/services/branch.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { User, Branch } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-user-form-dialog',
  standalone: true,
  templateUrl: './user-form-dialog.component.html',
  styleUrls: ['./user-form-dialog.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, TranslatePipe]
})
export class UserFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  isEdit = false;
  branches: Branch[] = [];
  loadingBranches = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private branchService: BranchService,
    private notification: NotificationService,
    private ts: TranslationService,
    public dialogRef: MatDialogRef<UserFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: User | null
  ) { }

  ngOnInit(): void {
    this.isEdit = !!this.data?.id;
    this.loadBranches();
    this.initializeForm();
  }

  initializeForm(): void {
    if (this.isEdit) {
      this.form = this.fb.group({
        firstName: [this.data?.firstName || '', [Validators.required, Validators.minLength(2)]],
        lastName: [this.data?.lastName || '', [Validators.required, Validators.minLength(2)]],
        email: [this.data?.email || '', [Validators.required, Validators.email]],
        branchId: [this.data?.branch?.id || '', Validators.required],
        isActive: [this.data?.isActive ?? 1]
      });
    } else {
      this.form = this.fb.group({
        firstName: ['', [Validators.required, Validators.minLength(2)]],
        lastName: ['', [Validators.required, Validators.minLength(2)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        branchId: ['', Validators.required],
        isActive: [1]
      });
    }
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

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;
    const req = this.isEdit
      ? this.userService.update(this.data!.id!, { ...this.data, ...this.form.value })
      : this.userService.create(this.form.value);

    req.subscribe({
      next: () => {
        this.notification.success(this.ts.t(this.isEdit ? 'USERS.UPDATED' : 'USERS.CREATED'));
        this.dialogRef.close(true);
      },
      error: () => {
        this.saving = false;
      }
    });
  }
}
