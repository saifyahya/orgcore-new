# Profile API Documentation

## Overview
The Profile API provides endpoints for managing user and tenant profile information. All endpoints require authentication and operate on the current authenticated user's profile.

## Base URL
```
/profile
```

## Endpoints

### 1. Get Profile
**Endpoint:** `GET /profile`

**Description:** Retrieves the current authenticated user's profile information, combining data from both User and Tenant entities.

**Authentication:** Required

**Response:**
```json
{
  "userId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "userIsActive": 1,
  "tenantId": 1,
  "tenantName": "Acme Corporation",
  "address": "123 Main St",
  "phone": "555-1234",
  "tenantIsActive": 1,
  "createdBy": "admin@example.com",
  "createdAt": "2026-04-28T10:30:00",
  "updatedBy": "admin@example.com",
  "updatedAt": "2026-04-28T10:30:00"
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `404 Not Found` - User or tenant not found

---

### 2. Update User Profile
**Endpoint:** `PUT /profile`

**Description:** Updates the current user's profile information (first name, last name, email).

**Authentication:** Required

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com"
}
```

**Validation Rules:**
- `firstName`: Required, must not be blank
- `lastName`: Required, must not be blank
- `email`: Required, must be valid email format
- Email must be unique across all users
- First name and last name combination must be unique within the tenant

**Response:**
Same as Get Profile endpoint

**Status Codes:**
- `200 OK` - Profile updated successfully
- `400 Bad Request` - Validation error
- `409 Conflict` - Email or name combination already exists
- `404 Not Found` - User not found

**Error Examples:**
```json
{
  "error": "Email already exists"
}
```

```json
{
  "error": "A user with this first name and last name already exists in your tenant"
}
```

---

### 3. Update Tenant Profile
**Endpoint:** `PUT /profile/tenant`

**Description:** Updates the tenant's profile information (tenant name, address, phone, email).

**Authentication:** Required

**Request Body:**
```json
{
  "tenantName": "Acme Corporation Ltd",
  "address": "456 Oak Ave, Suite 100",
  "phone": "555-5678",
  "email": "contact@acme.com"
}
```

**Validation Rules:**
- `tenantName`: Required, must not be blank
- `address`: Required, must not be blank
- `phone`: Required, must not be blank
- `email`: Required, must be valid email format
- Tenant name must be unique across all active tenants
- Email must be unique across all active tenants
- Phone must be unique across all active tenants

**Response:**
Same as Get Profile endpoint

**Status Codes:**
- `200 OK` - Tenant profile updated successfully
- `400 Bad Request` - Validation error
- `409 Conflict` - Tenant name, email, or phone already exists
- `404 Not Found` - Tenant not found

**Error Examples:**
```json
{
  "error": "Tenant name already exists"
}
```

```json
{
  "error": "Email already exists"
}
```

```json
{
  "error": "Phone already exists"
}
```

---

## Data Types

### ProfileDto
Combines user and tenant information with metadata.

| Field | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |
| firstName | String | User's first name |
| lastName | String | User's last name |
| email | String | User's email |
| userIsActive | Integer | User active status (0 or 1) |
| tenantId | Long | Tenant ID |
| tenantName | String | Organization/Tenant name |
| address | String | Tenant address |
| phone | String | Tenant phone number |
| tenantIsActive | Integer | Tenant active status (0 or 1) |
| createdBy | String | Email of user who created the record |
| createdAt | String | Creation timestamp (ISO 8601) |
| updatedBy | String | Email of user who last updated |
| updatedAt | String | Last update timestamp (ISO 8601) |

### UpdateProfileDto
Used to update user profile information.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| firstName | String | Yes | Not blank |
| lastName | String | Yes | Not blank |
| email | String | Yes | Valid email format |

### UpdateTenantProfileDto
Used to update tenant profile information.

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| tenantName | String | Yes | Not blank |
| address | String | Yes | Not blank |
| phone | String | Yes | Not blank |
| email | String | Yes | Valid email format |

---

## Uniqueness Constraints

### User Profile Update
- **Email**: Must be globally unique (across all users)
- **First Name + Last Name**: Must be unique within the tenant (no two users in same tenant with same name)

### Tenant Profile Update
- **Tenant Name**: Must be unique across all active tenants
- **Email**: Must be unique across all active tenants
- **Phone**: Must be unique across all active tenants

---

## Example Usage

### Get Current User's Profile
```bash
curl -X GET http://localhost:8080/profile \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"
```

### Update User Profile
```bash
curl -X PUT http://localhost:8080/profile \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com"
  }'
```

### Update Tenant Profile
```bash
curl -X PUT http://localhost:8080/profile/tenant \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Acme Corporation Ltd",
    "address": "456 Oak Ave, Suite 100",
    "phone": "555-5678",
    "email": "contact@acme.com"
  }'
```

---

## Notes

- All endpoints require authentication (Bearer token in Authorization header)
- The current tenant ID and user email are determined from the authenticated user context
- Uniqueness checks exclude the current record (allow updating without changing values)
- All timestamps are in ISO 8601 format with UTC timezone
- Tenant ID is automatically determined from the current user's tenant context

