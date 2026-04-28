import { Component, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { NavItem, SidebarComponent } from './features/sidebar/sidebar/sidebar.component';
import { HeaderComponent } from './features/header/header/header.component';
import { LocalizedCurrencyPipe } from './shared/pipes/localized-currency.pipe';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    SidebarComponent,
    HeaderComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  @ViewChild('sidenav') sidenav!: MatSidenav;

  today = new Date();

  navItems: NavItem[] = [
    { icon: 'dashboard', route: '/dashboard', labelKey: 'NAV.DASHBOARD' },
    { icon: 'bar_chart', route: '/reports', labelKey: 'NAV.REPORTS' },
    { icon: 'store', route: '/branches', labelKey: 'NAV.BRANCHES' },
    { icon: 'category', route: '/categories', labelKey: 'NAV.CATEGORIES' },
    { icon: 'inventory', route: '/products', labelKey: 'NAV.PRODUCTS' },
    { icon: 'receipt_long', route: '/sales', labelKey: 'NAV.SALES' },
    { icon: 'warehouse', route: '/inventory', labelKey: 'NAV.INVENTORY' },
    { icon: 'swap_horiz', route: '/stock-movements', labelKey: 'NAV.STOCK_MOVEMENTS' }
  ];

  private authService = inject(AuthService);
  private router = inject(Router);

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  isMobile(): boolean {
    return window.innerWidth < 768;
  }

  isRtl(): boolean {
    return this.currentLang() === 'ar';
  }

  currentLang(): string {
    return localStorage.getItem('lang') || 'en';
  }

  isDark(): boolean {
    return document.body.classList.contains('dark-theme');
  }

  toggleLang(): void {
    const newLang = this.currentLang() === 'en' ? 'ar' : 'en';
    localStorage.setItem('lang', newLang);
    window.location.reload();
  }

  toggleTheme(): void {
    document.body.classList.toggle('dark-theme');
  }

  toggleSidenav(): void {
    if (this.sidenav) {
      this.sidenav.toggle();
    }
  }

  closeSidenavOnMobile(): void {
    if (this.isMobile() && this.sidenav) {
      this.sidenav.close();
    }
  }

  onLogout(): void {
    this.authService.logout();
  }

  onProfileClick(): void {
    this.router.navigate(['/profile']);
  }
}