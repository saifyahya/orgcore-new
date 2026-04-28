// Profile DTOs matching backend
export interface ProfileDto {
  // User fields
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  userIsActive: number;

  // Tenant fields
  tenantId: number;
  tenantName: string;
  address: string;
  phone: string;
  tenantIsActive: number;

  // Metadata
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
}

export interface UpdateProfileDto {
  firstName: string;
  lastName: string;
  email: string;
}

export interface UpdateTenantProfileDto {
  tenantName: string;
  address: string;
  phone: string;
  email: string;
}
