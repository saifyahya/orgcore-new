import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProfileService } from 'src/app/core/services/profile.service';
import { ProfileDto, UpdateProfileDto, UpdateTenantProfileDto } from 'src/app/core/models/profile.model';
import { TranslatePipe } from 'src/app/shared/pipes/translate.pipe';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    FormsModule,
    ReactiveFormsModule,
    TranslatePipe
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  loading = false;
  saving = false;
  profileData: ProfileDto | null = null;

  // Store original values for change detection
  private originalValues = {
    firstName: '',
    lastName: '',
    userEmail: '',
    tenantName: '',
    address: '',
    phone: '',
    tenantEmail: ''
  };

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadProfile();
  }

  private initializeForm(): void {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      userEmail: ['', [Validators.required, Validators.email]],
      tenantName: ['', [Validators.required, Validators.minLength(2)]],
      address: ['', [Validators.required, Validators.minLength(5)]],
      phone: ['', [Validators.required, Validators.pattern(/^[+]?[0-9\s-().]+$/)]],
      tenantEmail: ['', [Validators.required, Validators.email]]
    });
  }

  private loadProfile(): void {
    this.loading = true;
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profileData = profile;
        const formValues = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          userEmail: profile.email,
          tenantName: profile.tenantName,
          address: profile.address,
          phone: profile.phone,
          tenantEmail: profile.email
        };
        this.profileForm.patchValue(formValues);
        this.originalValues = { ...formValues };
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load profile:', error);
        this.snackBar.open('Failed to load profile', 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  private hasUserChanges(): boolean {
    const formValue = this.profileForm.value;
    return (
      formValue.firstName !== this.originalValues.firstName ||
      formValue.lastName !== this.originalValues.lastName ||
      formValue.userEmail !== this.originalValues.userEmail
    );
  }

  private hasTenantChanges(): boolean {
    const formValue = this.profileForm.value;
    return (
      formValue.tenantName !== this.originalValues.tenantName ||
      formValue.address !== this.originalValues.address ||
      formValue.phone !== this.originalValues.phone ||
      formValue.tenantEmail !== this.originalValues.tenantEmail
    );
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.snackBar.open('Please fill all required fields correctly', 'Close', { duration: 5000 });
      return;
    }

    const userChanges = this.hasUserChanges();
    const tenantChanges = this.hasTenantChanges();

    if (!userChanges && !tenantChanges) {
      this.snackBar.open('No changes to save', 'Close', { duration: 3000 });
      return;
    }

    this.saving = true;
    const requests: any[] = [];

    // Add user update request if user fields changed
    if (userChanges) {
      const userUpdate: UpdateProfileDto = {
        firstName: this.profileForm.get('firstName')!.value,
        lastName: this.profileForm.get('lastName')!.value,
        email: this.profileForm.get('userEmail')!.value
      };
      requests.push(this.profileService.updateUserProfile(userUpdate));
    }

    // Add tenant update request if tenant fields changed
    if (tenantChanges) {
      const tenantUpdate: UpdateTenantProfileDto = {
        tenantName: this.profileForm.get('tenantName')!.value,
        address: this.profileForm.get('address')!.value,
        phone: this.profileForm.get('phone')!.value,
        email: this.profileForm.get('tenantEmail')!.value
      };
      requests.push(this.profileService.updateTenantProfile(tenantUpdate));
    }

    // Execute request(s) in parallel
    if (requests.length === 1) {
      requests[0].subscribe({
        next: (profile: ProfileDto) => this.handleSaveSuccess(profile),
        error: (error: any) => this.handleSaveError(error)
      });
    } else {
      // If both user and tenant changed, execute both requests
      forkJoin(requests).subscribe({
        next: (responses: ProfileDto[]) => {
          // Use the last response which contains the full profile
          this.handleSaveSuccess(responses[responses.length - 1]);
        },
        error: (error: any) => this.handleSaveError(error)
      });
    }
  }

  private handleSaveSuccess(profile: ProfileDto): void {
    this.profileData = profile;
    this.saving = false;
    
    // Update original values to reflect the saved state
    const formValues = this.profileForm.value;
    this.originalValues = { ...formValues };
    
    this.snackBar.open('Profile updated successfully', 'Close', { duration: 3000 });
  }

  private handleSaveError(error: any): void {
    console.error('Failed to update profile:', error);
    this.snackBar.open('Failed to update profile', 'Close', { duration: 5000 });
    this.saving = false;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
