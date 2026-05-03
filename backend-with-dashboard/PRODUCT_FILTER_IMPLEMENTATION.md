# Product Filtering Implementation Summary

## Changes Made

### 1. **PageFilter DTO** (`PageFilter.java`)
Added `branchId` field to the PageFilter class:
```java
private Long branchId; // optional branch filtering
```

The PageFilter now supports:
- `page` - Page number (default: 0)
- `size` - Page size (default: 20, max: 200)
- `sortBy` - Sort field (default: "id")
- `sortDir` - Sort direction: "asc" or "desc" (default: "desc")
- `search` - General search term (optional)
- `isActive` - Filter by active status (optional)
- `categoryId` - Filter by category (optional)
- `branchId` - Filter by branch (optional) ✅ NEW

### 2. **ProductRepository** (`ProductRepository.java`)
Updated with a single comprehensive query that supports all filters:
```java
@Query("""
SELECT DISTINCT p FROM Product p
LEFT JOIN p.inventories inv
LEFT JOIN inv.branch b
LEFT JOIN p.category c
WHERE p.tenantId = :tenantId
AND (
      :q IS NULL
   OR :q = ''
   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
)
AND (
      :isActive IS NULL
   OR p.isActive = :isActive
)
AND (
      :categoryId IS NULL
   OR p.category.id = :categoryId
)
AND (
      :branchId IS NULL
   OR b.id = :branchId
)
""")
Page<Product> findAllByTenantId(
        @Param("tenantId") Long tenantId,
        @Param("q") String search,
        @Param("categoryId") Long categoryId,
        @Param("isActive") Integer isActive,
        @Param("branchId") Long branchId,
        Pageable pageable
);
```

**Key Features:**
- Single query with conditional filters
- Uses `DISTINCT` to avoid duplicate products when joining with inventories
- `LEFT JOIN` ensures products without inventories are still returned
- Supports null/empty parameters (makes filters optional)

### 3. **ProductService** (`ProductService.java`)
Updated the `getAll` method to pass `branchId`:
```java
@Transactional(readOnly = true)
public Page<ProductDto> getAll(Long tenantId, PageFilter pageFilter) {
    Page<Product> page = productRepository.findAllByTenantId(
                tenantId,
                pageFilter.getSearch(),
                pageFilter.getCategoryId(),
                pageFilter.getIsActive(),
                pageFilter.getBranchId(),  // ✅ NEW
                pageFilter.toPageable()
        );
    return page.map(this::toDto);
}
```

### 4. **ProductController** (`ProductController.java`)
No changes needed - already uses `@ModelAttribute PageFilter pageFilter`

## API Usage Examples

### Get all products (no filtering)
```
GET /products?page=0&size=20&sortBy=id&sortDir=desc
```

### Search by name
```
GET /products?page=0&size=20&search=burger
```

### Filter by category
```
GET /products?page=0&size=20&categoryId=1
```

### Filter by active status
```
GET /products?page=0&size=20&isActive=1
```

### Filter by branch
```
GET /products?page=0&size=20&branchId=5
```

### Combined filters - Search in a branch, category 2, active products
```
GET /products?page=0&size=20&search=pizza&branchId=3&categoryId=2&isActive=1&sortBy=name&sortDir=asc
```

### Complex filter - Active products in branch 1, sorted by price
```
GET /products?page=0&size=50&branchId=1&isActive=1&sortBy=price&sortDir=desc
```

## Response Example
```json
{
  "content": [
    {
      "id": 1,
      "name": "Product Name",
      "description": "Description",
      "code": "PROD001",
      "categoryDto": {...},
      "image": "path/to/image.jpg",
      "price": 99.99,
      "isActive": 1,
      "createdBy": "admin@example.com",
      "createdAt": "2024-04-28T10:30:00",
      "updatedBy": "admin@example.com",
      "updatedAt": "2024-04-28T11:45:00"
    }
  ],
  "pageable": {...},
  "totalElements": 45,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

## Key Benefits

✅ **Single Query** - One database query handles all filter combinations
✅ **Optional Filters** - All filters are optional, can be used independently
✅ **Performance** - Uses DISTINCT and proper JOINs to avoid N+1 queries
✅ **Flexible** - Supports pagination, sorting, and search
✅ **Multi-tenant** - Filters by tenantId for data isolation
