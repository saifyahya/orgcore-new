# API Testing Guide - Product Image Upload

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token for authentication
- Test image file (e.g., `test-product.jpg`)

## Testing with cURL (Windows PowerShell)

### 1. Create Product with Image

```powershell
curl -X POST "http://localhost:8080/api/products" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -F "name=Test Product" `
  -F "description=This is a test product" `
  -F "categoryId=1" `
  -F "price=99.99" `
  -F "isActive=1" `
  -F "imageFile=@C:\path\to\your\image.jpg"
```

### 2. Update Product with New Image

```powershell
curl -X PUT "http://localhost:8080/api/products/1" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -F "name=Updated Product" `
  -F "price=149.99" `
  -F "imageFile=@C:\path\to\new-image.jpg"
```

### 3. Get Product (Returns Encrypted Image)

```powershell
curl -X GET "http://localhost:8080/api/products/1" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "id": 1,
  "name": "Test Product",
  "description": "This is a test product",
  "categoryDto": { ... },
  "image": "iVBORw0KGgoAAAANSUhEUgAA...(base64 encrypted data)...",
  "price": 99.99,
  "isActive": 1,
  ...
}
```

### 4. Download Image Directly (if you know filename)

```powershell
curl -X GET "http://localhost:8080/api/images/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg" `
  --output downloaded-image.jpg
```

### 5. Decrypt Encrypted Image

```powershell
curl -X POST "http://localhost:8080/api/images/decrypt" `
  -H "Content-Type: application/json" `
  -d '{\"encryptedData\": \"YOUR_ENCRYPTED_BASE64_DATA\"}' `
  --output decrypted-image.jpg
```

## Testing with Postman

### Create Product with Image

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/products`
3. **Headers**:
   - `Authorization: Bearer YOUR_JWT_TOKEN`
4. **Body** (form-data):
   - name: `Test Product` (text)
   - description: `Test description` (text)
   - categoryId: `1` (text)
   - price: `99.99` (text)
   - isActive: `1` (text)
   - imageFile: (file) - select your image file

### Update Product

1. **Method**: PUT
2. **URL**: `http://localhost:8080/api/products/1`
3. **Headers**:
   - `Authorization: Bearer YOUR_JWT_TOKEN`
4. **Body** (form-data):
   - name: `Updated Product` (text)
   - price: `149.99` (text)
   - imageFile: (file) - select new image file

### Get Product

1. **Method**: GET
2. **URL**: `http://localhost:8080/api/products/1`
3. **Headers**:
   - `Authorization: Bearer YOUR_JWT_TOKEN`

### Decrypt Image

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/images/decrypt`
3. **Headers**:
   - `Content-Type: application/json`
4. **Body** (raw JSON):
```json
{
  "encryptedData": "paste-encrypted-base64-data-here"
}
```

## Testing with JavaScript (Browser Console)

### Upload Product with Image

```javascript
// HTML: <input type="file" id="fileInput">

const fileInput = document.getElementById('fileInput');
const formData = new FormData();
formData.append('name', 'Test Product');
formData.append('description', 'Test description');
formData.append('categoryId', '1');
formData.append('price', '99.99');
formData.append('isActive', '1');
formData.append('imageFile', fileInput.files[0]);

fetch('http://localhost:8080/api/products', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log('Created product:', data))
.catch(error => console.error('Error:', error));
```

### Get and Display Product Image

```javascript
// Fetch product
fetch('http://localhost:8080/api/products/1', {
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  }
})
.then(response => response.json())
.then(product => {
  console.log('Product:', product);
  
  // Decrypt image
  return fetch('http://localhost:8080/api/images/decrypt', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ encryptedData: product.image })
  });
})
.then(response => response.blob())
.then(blob => {
  const imageUrl = URL.createObjectURL(blob);
  console.log('Image URL:', imageUrl);
  
  // Display in image tag
  const img = document.createElement('img');
  img.src = imageUrl;
  document.body.appendChild(img);
})
.catch(error => console.error('Error:', error));
```

## Common Issues & Solutions

### Issue: "File cannot be empty"
**Solution**: Ensure you're actually selecting a file in the file input

### Issue: "Category not found"
**Solution**: Make sure the categoryId exists in your database

### Issue: 401 Unauthorized
**Solution**: Your JWT token might be expired or invalid

### Issue: 413 Payload Too Large
**Solution**: Your image exceeds 10MB. Compress it or increase the limit in application.yaml

### Issue: Image not displaying
**Solution**: 
- Check that the encrypted data is being returned
- Verify the decrypt endpoint is accessible
- Check browser console for errors

### Issue: "Could not create directory"
**Solution**: Ensure the application has write permissions in the directory

## Verification Checklist

- [ ] Application starts without errors
- [ ] Upload directory is created (`uploads/products/`)
- [ ] Can create product with image
- [ ] Can create product without image
- [ ] Can update product and replace image
- [ ] Old image is deleted when replaced
- [ ] Can retrieve product with encrypted image data
- [ ] Can decrypt and display image
- [ ] Can download image directly via /api/images/{fileName}
- [ ] Image is deleted when product is deleted

## Sample Test Scenario

1. **Create a test category** (if not exists)
   ```sql
   INSERT INTO category (name, tenant_id, is_active, created_by, created_at, updated_by, updated_at)
   VALUES ('Test Category', 1, 1, 'admin', GETDATE(), 'admin', GETDATE());
   ```

2. **Create product with image** using Postman/cURL

3. **Verify file is stored** in `uploads/products/` directory

4. **Get product** and verify encrypted image is returned

5. **Update product** with new image

6. **Verify old image is deleted** and new one is stored

7. **Delete product** and verify image file is also deleted

## Performance Tips

1. **Cache decrypted images** in frontend to avoid repeated decryption
2. **Use CDN** in production for better performance
3. **Implement lazy loading** for product images
4. **Consider thumbnail generation** for list views
5. **Optimize images** before upload (compress, resize)
