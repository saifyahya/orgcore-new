# StockManager — Angular 17 Frontend

A full-featured stock, sales, and inventory management frontend built with **Angular 17 standalone components**, **Angular Material**, and connecting to a Spring Boot backend.

---

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- npm 9+
- Angular CLI 17: `npm install -g @angular/cli@17`
- Your Spring Boot backend running (default: `http://localhost:8080`)

### Installation
```bash
npm install
ng serve
```
Open [http://localhost:4200](http://localhost:4200)

---

## 🏗️ Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── models/
│   │   │   └── index.ts              # All TypeScript interfaces + enums
│   │   ├── services/
│   │   │   ├── api.service.ts        # Base HTTP service
│   │   │   ├── branch.service.ts
│   │   │   ├── category.service.ts
│   │   │   ├── product.service.ts
│   │   │   ├── sale.service.ts
│   │   │   ├── inventory.service.ts  # Also includes StockMovementService
│   │   │   ├── notification.service.ts
│   │   │   └── excel.service.ts      # Excel import/export (xlsx)
│   │   └── interceptors/
│   │       └── error.interceptor.ts  # Global HTTP error handler
│   │
│   ├── shared/
│   │   └── components/
│   │       └── confirm-dialog.component.ts
│   │
│   ├── features/
│   │   ├── dashboard/
│   │   │   └── dashboard.component.ts
│   │   ├── branches/
│   │   │   ├── branch-list/
│   │   │   └── branch-form-dialog/
│   │   ├── categories/
│   │   │   ├── category-list/
│   │   │   └── category-form-dialog/
│   │   ├── products/
│   │   │   ├── product-list/
│   │   │   └── product-form-dialog/
│   │   ├── sales/
│   │   │   ├── sale-list/            # With filters + Excel import button
│   │   │   ├── sale-form/            # Create/Edit with dynamic line items
│   │   │   ├── sale-detail/          # Read-only sale view
│   │   │   └── sale-import-dialog/   # Excel/CSV stepper import
│   │   ├── inventory/
│   │   │   ├── inventory-list/
│   │   │   └── inventory-form-dialog/
│   │   └── stock-movements/
│   │       ├── stock-movement-list/
│   │       └── stock-movement-form-dialog/
│   │
│   ├── app.component.ts              # Sidebar + toolbar layout
│   ├── app.routes.ts                 # Lazy-loaded routes
│   └── app.config.ts                 # Providers: router, http, animations
│
├── environments/
│   ├── environment.ts                # apiUrl: http://localhost:8080/api
│   └── environment.prod.ts
├── styles.scss                       # Global styles + Material overrides
└── index.html
```

---

## 🔌 Backend API Endpoints Used

| Entity | Endpoints |
|--------|-----------|
| Branches | `GET/POST /branches`, `PUT/PATCH/DELETE /branches/{id}` |
| Categories | `GET/POST /categories`, `PUT/DELETE /categories/{id}` |
| Products | `GET/POST /products`, `PUT/DELETE /products/{id}` |
| Sales | `GET/POST /sales`, `PUT/DELETE /sales/{id}`, `POST /sales/import` |
| Inventory | `GET/POST /inventory`, `PUT/DELETE /inventory/{id}` |
| Stock Movements | `GET/POST /stock-movements`, `DELETE /stock-movements/{id}` |

### Change API Base URL
Edit `src/environments/environment.ts`:
```ts
export const environment = {
  production: false,
  apiUrl: 'http://YOUR_HOST:8080/api'
};
```

---

## ✨ Features

### Dashboard
- Live stats: branch count, category count, product count, total sales, revenue, inventory lines
- Quick action buttons
- Recent sales table

### Branches
- List with search filter
- Create / Edit via dialog
- Soft-delete with confirmation

### Categories
- List with search
- Create / Edit / Delete

### Products
- List with search + category filter
- Create / Edit / Delete with category, price, discount fields

### Sales
- List with branch + date range filters
- Revenue summary
- **Create manually**: dynamic line items with auto-pricing from products
- **Edit** existing sale
- **View** sale detail
- **Import from Excel/CSV**: 2-step stepper — configure branch → preview rows → import
- Download import template

### Inventory
- List with branch filter + product search
- Color-coded quantity badges (red = low ≤5, green = OK)
- Create / Edit / Delete entries

### Stock Movements
- Full audit trail of all IN/OUT/ADJUSTMENT/TRANSFER events
- Filter by branch + type
- Record new movements with type, reason, quantity, cost, reference, note

---

## 🎨 Tech Stack

- **Angular 17** — Standalone components, lazy-loaded routes
- **Angular Material 17** — UI components
- **RxJS** — Reactive streams
- **xlsx** — Excel parsing and export
- **TypeScript 5.2**

---

## 📦 Sales Excel Import Format

The import template file has these columns:

| productId | quantity | unitPrice | lineTotal |
|-----------|----------|-----------|-----------|
| 1         | 10       | 25.00     | 250.00    |
| 2         | 5        | 15.50     | 77.50     |

Each row = one line item. All rows in a file are treated as items for a **single sale** associated with the selected branch.

---

## 🔧 CORS Configuration

Make sure your Spring Boot backend allows requests from `http://localhost:4200`:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
```
