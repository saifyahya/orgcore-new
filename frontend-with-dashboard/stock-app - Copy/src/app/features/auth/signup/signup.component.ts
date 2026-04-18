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

import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
    selector: 'app-signup',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatInputModule, MatButtonModule, MatStepperModule, MatIconModule, MatSnackBarModule, TranslatePipe],
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.scss']
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
    currentLang = localStorage.getItem('lang') || 'en';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private snackBar: MatSnackBar,
        private router: Router
    ) { }

    toggleLang(): void {
        const newLang = this.currentLang === 'en' ? 'ar' : 'en';
        localStorage.setItem('lang', newLang);
        window.location.reload();
    }

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
