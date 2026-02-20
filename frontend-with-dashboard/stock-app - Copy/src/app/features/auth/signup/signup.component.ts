import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
    selector: 'app-signup',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatInputModule, MatButtonModule, MatStepperModule, MatIconModule, MatSnackBarModule],
    template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <mat-card-header>
          <div class="header-content">
            <mat-card-title>Create Account</mat-card-title>
            <mat-card-subtitle>Start your business journey</mat-card-subtitle>
          </div>
        </mat-card-header>
        <mat-card-content>
          <mat-stepper linear #stepper>
            <!-- Step 1: User Details -->
            <mat-step [stepControl]="userForm">
              <ng-template matStepLabel>Account</ng-template>
              <form [formGroup]="userForm" class="step-form">
                <div class="row">
                  <mat-form-field appearance="outline">
                    <mat-label>First Name</mat-label>
                    <input matInput formControlName="firstName" placeholder="John">
                    <mat-error *ngIf="userForm.get('firstName')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Last Name</mat-label>
                    <input matInput formControlName="lastName" placeholder="Doe">
                    <mat-error *ngIf="userForm.get('lastName')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Email Address</mat-label>
                  <input matInput formControlName="email" type="email" placeholder="john@company.com">
                  <mat-error *ngIf="userForm.get('email')?.hasError('required')">Required</mat-error>
                  <mat-error *ngIf="userForm.get('email')?.hasError('email')">Invalid email</mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Password</mat-label>
                  <input matInput formControlName="password" [type]="hidePassword ? 'password' : 'text'">
                  <button mat-icon-button matSuffix (click)="hidePassword = !hidePassword" type="button">
                    <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
                  </button>
                  <mat-error *ngIf="userForm.get('password')?.hasError('required')">Required</mat-error>
                  <mat-error *ngIf="userForm.get('password')?.hasError('minlength')">Too short</mat-error>
                </mat-form-field>

                <div class="actions">
                  <button mat-flat-button color="primary" matStepperNext>Next</button>
                </div>
              </form>
            </mat-step>

            <!-- Step 2: Tenant Details -->
            <mat-step [stepControl]="tenantForm">
              <ng-template matStepLabel>Business</ng-template>
              <form [formGroup]="tenantForm" class="step-form">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Business Name</mat-label>
                  <input matInput formControlName="tenantName" placeholder="Acme Corp">
                  <mat-error *ngIf="tenantForm.get('tenantName')?.hasError('required')">Required</mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Business Email</mat-label>
                  <input matInput formControlName="email" type="email" placeholder="contact@acme.com">
                  <mat-error *ngIf="tenantForm.get('email')?.hasError('required')">Required</mat-error>
                  <mat-error *ngIf="tenantForm.get('email')?.hasError('email')">Invalid email</mat-error>
                </mat-form-field>

                <div class="row">
                  <mat-form-field appearance="outline">
                    <mat-label>Phone</mat-label>
                    <input matInput formControlName="phone" placeholder="+1234567890">
                    <mat-error *ngIf="tenantForm.get('phone')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Address</mat-label>
                    <input matInput formControlName="address" placeholder="City, Country">
                    <mat-error *ngIf="tenantForm.get('address')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>

                <div class="actions">
                  <button mat-button matStepperPrevious>Back</button>
                  <button mat-flat-button color="primary" (click)="onSubmit()">
                    {{ loading ? 'Creating...' : 'Create Account' }}
                  </button>
                </div>
              </form>
            </mat-step>
          </mat-stepper>

          <div class="auth-footer">
            <span>Already have an account?</span>
            <a routerLink="/login">Log in</a>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
    styles: [`
    .auth-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: var(--page-bg);
      padding: 16px;
    }
    .auth-card {
      max-width: 500px;
      width: 100%;
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
    }
    .header-content {
      text-align: center;
      width: 100%;
      margin-bottom: 16px;
    }
    .step-form {
      display: flex;
      flex-direction: column;
      padding-top: 16px;
    }
    .full-width {
      width: 100%;
      margin-bottom: 8px;
    }
    .row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }
    .auth-footer {
      margin-top: 16px;
      text-align: center;
      font-size: 14px;
      color: var(--text-secondary);
      
      a {
        color: var(--primary);
        text-decoration: none;
        font-weight: 500;
        margin-left: 4px;
        &:hover { text-decoration: underline; }
      }
    }
  `]
})
export class SignupComponent {
    userForm = this.fb.group({
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]]
    });

    tenantForm = this.fb.group({
        tenantName: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        phone: ['', Validators.required],
        address: ['', Validators.required]
    });

    hidePassword = true;
    loading = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private snackBar: MatSnackBar,
        private router: Router
    ) { }

    onSubmit(): void {
        if (this.userForm.invalid || this.tenantForm.invalid) return;

        this.loading = true;
        const userData = this.userForm.value;
        const tenantData = this.tenantForm.value;

        const request = {
            firstName: userData.firstName!,
            lastName: userData.lastName!,
            email: userData.email!,
            password: userData.password!,
            tenant: {
                tenantName: tenantData.tenantName!,
                email: tenantData.email!,
                phone: tenantData.phone!,
                address: tenantData.address!
            }
        };

        this.authService.signup(request).subscribe({
            next: () => {
                this.loading = false;
                // Navigation is handled by auth service setSession
            },
            error: (err) => {
                this.loading = false;
                this.snackBar.open(err.error?.message || 'Signup failed', 'Close', { duration: 3000 });
            }
        });
    }
}
