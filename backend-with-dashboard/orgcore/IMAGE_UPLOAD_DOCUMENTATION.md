# Product Image Upload Feature - Documentation

## Overview
This feature implements a secure file upload system for product images with encryption for data transmission. Images are stored on the server, and when retrieved, they are encrypted using AES encryption before being sent to the frontend.

## Components

### 1. FileStorageService
**Location:** `src/main/java/com/engineering/orgcore/service/FileStorageService.java`

**Responsibilities:**
- Store uploaded image files to the server
- Load image files from storage
- Delete image files
- Encrypt images for secure transmission (AES encryption)
- Decrypt encrypted image data

**Key Methods:**
- `storeFile(MultipartFile file)`: Stores uploaded file and returns the file path
- `loadFile(String fileName)`: Loads file from storage
- `deleteFile(String fileName)`: Deletes file from storage
- `getEncryptedImage(String imagePath)`: Returns base64-encoded encrypted image data
- `decryptImage(String encryptedBase64)`: Decrypts encrypted image data

**Configuration:**
- Upload directory: `uploads/products` (configurable in `application.yaml`)
- Encryption: AES with 128-bit key
- Files are stored with UUID-based names to avoid conflicts

### 2. Product Entity Updates
**Location:** `src/main/java/com/engineering/orgcore/entity/Product.java`

**Changes:**
- `image` field stores the relative path to the image file

### 3. ProductService Updates
**Location:** `src/main/java/com/engineering/orgcore/service/ProductService.java`

**Changes:**
- `create()`: Now accepts `MultipartFile imageFile` parameter
- `update()`: Now accepts `MultipartFile imageFile` parameter, replaces old image if new one provided
- `delete()`: Deletes associated image file when product is deleted
- `toDto()`: Returns encrypted image data instead of file path

### 4. ProductController Updates
**Location:** `src/main/java/com/engineering/orgcore/controller/ProductController.java`

**Changes:**
- Endpoints now accept `multipart/form-data` content type
- `create()`: Accepts `@ModelAttribute CreateProductDto` and `@RequestParam MultipartFile imageFile`
- `update()`: Accepts `@ModelAttribute CreateProductDto` and `@RequestParam MultipartFile imageFile`

### 5. ImageController
**Location:** `src/main/java/com/engineering/orgcore/controller/ImageController.java`

**Purpose:** Provides endpoints to retrieve and decrypt images

**Endpoints:**
- `GET /api/images/{fileName}`: Download/view image file directly
- `POST /api/images/decrypt`: Decrypt encrypted image data

## API Usage Examples

### Create Product with Image
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Product Name" \
  -F "description=Product Description" \
  -F "categoryId=1" \
  -F "price=99.99" \
  -F "isActive=1" \
  -F "imageFile=@/path/to/image.jpg"
```

### Update Product with New Image
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Updated Product Name" \
  -F "price=149.99" \
  -F "imageFile=@/path/to/new-image.jpg"
```

### Get Product (Returns Encrypted Image)
```bash
curl -X GET http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 1,
  "name": "Product Name",
  "description": "Product Description",
  "categoryDto": {...},
  "image": "base64-encoded-encrypted-image-data",
  "price": 99.99,
  "isActive": 1,
  "createdBy": "user@example.com",
  "createdAt": "2026-02-20T22:00:00",
  "updatedBy": "user@example.com",
  "updatedAt": "2026-02-20T22:00:00"
}
```

### View Image Directly (Unencrypted)
```bash
curl -X GET http://localhost:8080/api/images/{fileName}
```

### Decrypt Image (from encrypted data)
```bash
curl -X POST http://localhost:8080/api/images/decrypt \
  -H "Content-Type: application/json" \
  -d '{"encryptedData": "base64-encoded-encrypted-data"}'
```

## Frontend Integration

### 1. Creating/Updating Product with Image

```javascript
// Create FormData object
const formData = new FormData();
formData.append('name', 'Product Name');
formData.append('description', 'Product Description');
formData.append('categoryId', '1');
formData.append('price', '99.99');
formData.append('isActive', '1');

// Append image file from input
const imageInput = document.getElementById('imageInput');
if (imageInput.files[0]) {
  formData.append('imageFile', imageInput.files[0]);
}

// Send request
fetch('http://localhost:8080/api/products', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

### 2. Displaying Encrypted Image

**Option A: Decrypt on Backend and Display**
```javascript
// Get product with encrypted image
fetch('http://localhost:8080/api/products/1', {
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
.then(response => response.json())
.then(product => {
  // Decrypt the image
  fetch('http://localhost:8080/api/images/decrypt', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ encryptedData: product.image })
  })
  .then(response => response.blob())
  .then(blob => {
    const imageUrl = URL.createObjectURL(blob);
    document.getElementById('productImage').src = imageUrl;
  });
});
```

**Option B: Display Directly (if you know the filename)**
```javascript
// If you store the filename separately or can extract it
const imageUrl = `http://localhost:8080/api/images/${fileName}`;
document.getElementById('productImage').src = imageUrl;
```

### 3. React Example

```jsx
import React, { useState, useEffect } from 'react';

function ProductImage({ productId }) {
  const [imageUrl, setImageUrl] = useState(null);
  
  useEffect(() => {
    // Fetch product
    fetch(`http://localhost:8080/api/products/${productId}`, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
    .then(res => res.json())
    .then(product => {
      // Decrypt image
      fetch('http://localhost:8080/api/images/decrypt', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ encryptedData: product.image })
      })
      .then(res => res.blob())
      .then(blob => {
        const url = URL.createObjectURL(blob);
        setImageUrl(url);
      });
    });
    
    // Cleanup
    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [productId]);
  
  return imageUrl ? <img src={imageUrl} alt="Product" /> : <div>Loading...</div>;
}
```

## Configuration

### application.yaml
```yaml
# File Upload Configuration
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

# Custom file storage location
file:
  upload-dir: uploads/products
```

## Security Features

1. **AES Encryption**: Images are encrypted using AES algorithm before transmission
2. **UUID Filenames**: Files are stored with unique names to prevent conflicts and guessing
3. **Path Traversal Protection**: File paths are validated to prevent directory traversal attacks
4. **File Size Limits**: Maximum file size is enforced (10MB by default)
5. **Multipart Configuration**: Proper configuration for handling file uploads

## File Storage Structure

```
project-root/
└── uploads/
    └── products/
        ├── a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
        ├── b2c3d4e5-f6a7-8901-bcde-f12345678901.png
        └── ...
```

## Error Handling

- **Empty File**: Returns `IllegalArgumentException` if file is empty
- **Invalid Path**: Throws exception if filename contains path traversal sequences (..)
- **Storage Error**: Throws `RuntimeException` if file cannot be stored
- **Not Found**: Returns null or 404 if image file doesn't exist
- **Encryption Error**: Returns null if encryption/decryption fails

## Best Practices

1. **Always validate file types** on the frontend before upload
2. **Implement file size checks** on the frontend for better UX
3. **Clean up blob URLs** when components unmount to prevent memory leaks
4. **Cache decrypted images** to avoid repeated decryption calls
5. **Use appropriate image formats** (JPEG for photos, PNG for graphics)
6. **Consider image optimization** before upload (resize, compress)

## Troubleshooting

### Images not uploading
- Check file size doesn't exceed 10MB
- Verify `multipart/form-data` content type is set
- Ensure `imageFile` parameter name matches in frontend

### Images not displaying
- Verify encrypted data is being returned in product response
- Check decrypt endpoint is accessible
- Ensure proper CORS configuration

### Directory creation error
- Check application has write permissions to create upload directory
- Verify path is valid for the operating system
