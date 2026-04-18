import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { LocalizedCurrencyPipe } from 'src/app/shared/pipes/localized-currency.pipe';
import { TranslatePipe } from 'src/app/shared/pipes/translate.pipe';

export interface NavItem {
  icon: string;
  route: string;
  labelKey: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatIconModule,
    MatDividerModule,
    MatListModule,
  TranslatePipe  ],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  @Input() navItems: NavItem[] = [];
  @Input() isMobile = false;
  @Input() opened = true;

  @Output() itemClicked = new EventEmitter<void>();
}