import { Routes } from '@angular/router';
import { guestGuard } from './core/guards/guest.guard';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/signup/signup.component').then(m => m.SignupComponent)
  },

  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'reports',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/reports/reports.component').then(m => m.ReportsComponent)
  },
  {
    path: 'branches',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/branches/branch-list/branch-list.component').then(m => m.BranchListComponent)
  },
  {
    path: 'categories',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/categories/category-list/category-list.component').then(m => m.CategoryListComponent)
  },
  {
    path: 'users',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/users/user-list/user-list.component').then(m => m.UserListComponent)
  },
  {
    path: 'products',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent)
  },
  {
    path: 'sales',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/sales/sale-list/sale-list.component').then(m => m.SaleListComponent)
  },
  {
    path: 'sales/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/sales/sale-form/sale-form.component').then(m => m.SaleFormComponent)
  },
  {
    path: 'sales/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/sales/sale-detail/sale-detail.component').then(m => m.SaleDetailComponent)
  },
  {
    path: 'sales/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/sales/sale-form/sale-form.component').then(m => m.SaleFormComponent)
  },
  {
    path: 'inventory',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/inventory/inventory-list/inventory-list.component').then(m => m.InventoryListComponent)
  },
  {
    path: 'stock-movements',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/stock-movements/stock-movement-list/stock-movement-list.component').then(m => m.StockMovementListComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },

  {
    path: '**',
    redirectTo: 'dashboard'
  }
];