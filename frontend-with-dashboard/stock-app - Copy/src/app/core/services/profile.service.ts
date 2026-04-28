import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ProfileDto, UpdateProfileDto, UpdateTenantProfileDto } from '../models/profile.model';

@Injectable({ providedIn: 'root' })
export class ProfileService extends ApiService {

  /**
   * Get current user's profile
   * Combines User and Tenant information
   */
  getProfile(): Observable<ProfileDto> {
    return this.get<ProfileDto>('/profile');
  }

  /**
   * Update current user's profile information
   * Updates: firstName, lastName, email
   */
  updateUserProfile(request: UpdateProfileDto): Observable<ProfileDto> {
    return this.put<ProfileDto>('/profile', request);
  }

  /**
   * Update tenant's profile information
   * Updates: tenantName, address, phone, email
   */
  updateTenantProfile(request: UpdateTenantProfileDto): Observable<ProfileDto> {
    return this.put<ProfileDto>('/profile/tenant', request);
  }
}
