import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule],
    template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <mat-card-header>
          <mat-card-title>Welcome Back</mat-card-title>
          <mat-card-subtitle>Sign in to continue</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput formControlName="email" type="email" placeholder="email@example.com">
              <mat-error *ngIf="form.get('email')?.hasError('required')">Email is required</mat-error>
              <mat-error *ngIf="form.get('email')?.hasError('email')">Invalid email</mat-error>
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput formControlName="password" [type]="hidePassword ? 'password' : 'text'">
              <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.get('password')?.hasError('required')">Password is required</mat-error>
            </mat-form-field>

            <button mat-flat-button color="primary" class="full-width btn-submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>
          
          <div class="auth-footer">
            <span>Don't have an account?</span>
            <a routerLink="/signup">Sign up</a>
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
      max-width: 400px;
      width: 100%;
      padding: 24px;
      border-radius: 16px;
      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
    }
    mat-card-header {
      margin-bottom: 24px;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    mat-card-title {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 8px;
    }
    .full-width {
      width: 100%;
      margin-bottom: 8px;
    }
    .btn-submit {
      margin-top: 16px;
      padding: 24px 0;
      font-size: 16px;
    }
    .auth-footer {
      margin-top: 24px;
      text-align: center;
      font-size: 14px;
      color: var(--text-secondary);
      
      a {
        color: var(--primary);
        text-decoration: none;
        font-weight: 500;
        margin-left: 4px;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
  `]
})
export class LoginComponent {
    form = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', Validators.required]
    });

    hidePassword = true;
    loading = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private snackBar: MatSnackBar
    ) { }

    onSubmit(): void {
        if (this.form.invalid) return;

        this.loading = true;
        const { email, password } = this.form.value;

        this.authService.login({ email: email!, password: password! }).subscribe({
            next: () => {
                // Redirection handled by service
            },
            error: (err) => {
                this.loading = false;
                this.snackBar.open(err.error?.message || 'Login failed', 'Close', { duration: 3000 });
            }
        });
    }
}
