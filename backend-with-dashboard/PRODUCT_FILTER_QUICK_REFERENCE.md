# Product Filtering - Quick Reference

## Files Modified

### 1. PageFilter.java
✅ Added `branchId` field for branch filtering

### 2. ProductRepository.java
✅ Single query with conditional filters:
- Search by name (case-insensitive)
- Filter by isActive status
- Filter by category
- **Filter by branch** ✅ NEW

### 3. ProductService.java
✅ Updated `getAll()` method to pass `branchId` to repository

### 4. ProductController.java
✅ No changes (already using PageFilter)

## API Endpoint

```
GET /products
```

## Query Parameters

| Parameter | Type | Required | Example |
|-----------|------|----------|---------|
| page | int | No | 0 |
| size | int | No | 20 |
| sortBy | string | No | name |
| sortDir | string | No | asc |
| search | string | No | pizza |
| isActive | int | No | 1 |
| categoryId | long | No | 2 |
| branchId | long | No | 5 |

## Examples

**All products in branch 5:**
```
GET /products?branchId=5
```

**Active products in branch 3, category 2, search "pizza":**
```
GET /products?branchId=3&categoryId=2&isActive=1&search=pizza
```

**All products, sorted by price descending:**
```
GET /products?sortBy=price&sortDir=desc
```

**Branch 1 products, page 2, 50 per page:**
```
GET /products?branchId=1&page=1&size=50
```

## Response
Returns a paginated list of `ProductDto` objects with standard Spring Data Page metadata.
