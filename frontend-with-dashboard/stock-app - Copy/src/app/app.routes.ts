import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'reports',
    loadComponent: () =>
      import('./features/reports/reports.component').then(m => m.ReportsComponent)
  },
  {
    path: 'branches',
    loadComponent: () =>
      import('./features/branches/branch-list/branch-list.component').then(m => m.BranchListComponent)
  },
  {
    path: 'categories',
    loadComponent: () =>
      import('./features/categories/category-list/category-list.component').then(m => m.CategoryListComponent)
  },
  {
    path: 'products',
    loadComponent: () =>
      import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent)
  },
  {
    path: 'sales',
    loadComponent: () =>
      import('./features/sales/sale-list/sale-list.component').then(m => m.SaleListComponent)
  },
  {
    path: 'sales/new',
    loadComponent: () =>
      import('./features/sales/sale-form/sale-form.component').then(m => m.SaleFormComponent)
  },
  {
    path: 'sales/:id',
    loadComponent: () =>
      import('./features/sales/sale-detail/sale-detail.component').then(m => m.SaleDetailComponent)
  },
  {
    path: 'sales/:id/edit',
    loadComponent: () =>
      import('./features/sales/sale-form/sale-form.component').then(m => m.SaleFormComponent)
  },
  {
    path: 'inventory',
    loadComponent: () =>
      import('./features/inventory/inventory-list/inventory-list.component').then(m => m.InventoryListComponent)
  },
  {
    path: 'stock-movements',
    loadComponent: () =>
      import('./features/stock-movements/stock-movement-list/stock-movement-list.component').then(m => m.StockMovementListComponent)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./features/auth/signup/signup.component').then(m => m.SignupComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
