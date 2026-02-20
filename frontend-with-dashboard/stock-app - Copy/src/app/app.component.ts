import { Component, OnInit, signal } from '@angular/core';
import { RouterModule, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { BidiModule } from '@angular/cdk/bidi';
import { TranslationService } from './core/services/translation.service';
import { ThemeService } from './core/services/theme.service';
import { TranslatePipe } from './shared/pipes/translate.pipe';

interface NavItem { labelKey: string; icon: string; route: string; }

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule, MatTooltipModule, MatDividerModule, TranslatePipe, BidiModule]
})
export class AppComponent implements OnInit {
  isMobile = signal(false);
  today = new Date();
  currentLang = this.ts.lang;
  isRtl = this.ts.isRtl;
  isDark = this.themeService.isDark;

  navItems: NavItem[] = [
    { labelKey: 'NAV.DASHBOARD', icon: 'dashboard', route: '/dashboard' },
    { labelKey: 'NAV.REPORTS', icon: 'bar_chart', route: '/reports' },
    { labelKey: 'NAV.BRANCHES', icon: 'store', route: '/branches' },
    { labelKey: 'NAV.CATEGORIES', icon: 'category', route: '/categories' },
    { labelKey: 'NAV.PRODUCTS', icon: 'inventory', route: '/products' },
    { labelKey: 'NAV.SALES', icon: 'point_of_sale', route: '/sales' },
    { labelKey: 'NAV.INVENTORY', icon: 'warehouse', route: '/inventory' },
    { labelKey: 'NAV.STOCK_MOVEMENTS', icon: 'swap_vert', route: '/stock-movements' }
  ];

  constructor(private breakpointObserver: BreakpointObserver, private ts: TranslationService, private themeService: ThemeService) { }

  ngOnInit() {
    this.breakpointObserver.observe([Breakpoints.Handset, Breakpoints.TabletPortrait])
      .subscribe(result => this.isMobile.set(result.matches));
  }

  toggleLang(): void { this.ts.setLang(this.ts.lang() === 'en' ? 'ar' : 'en'); }
  toggleTheme(): void { this.themeService.toggle(); }
}
