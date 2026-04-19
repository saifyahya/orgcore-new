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

import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule, TranslatePipe],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    customerName: ['', Validators.required]
  });

  hidePassword = true;
  loading = false;
  currentLang = localStorage.getItem('lang') || 'en';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) { }

  toggleLang(): void {
    const newLang = this.currentLang === 'en' ? 'ar' : 'en';
    localStorage.setItem('lang', newLang);
    window.location.reload();
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    const { email, password, customerName } = this.form.value;

    this.authService.login({ email: email!, password: password!, customerName: customerName! }).subscribe({
      next: () => {
        // Redirection handled by service
      },
      error: (err) => {
        this.loading = false;
        // In real app, translate the error too if possible
        this.snackBar.open(err.error?.message || 'Login failed', 'Close', { duration: 3000 });
      }
    });
  }
}
