import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LocalizedCurrencyPipe } from 'src/app/shared/pipes/localized-currency.pipe';
import { TranslatePipe } from 'src/app/shared/pipes/translate.pipe';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    TranslatePipe
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  @Input() today!: Date;
  @Input() isRtl = false;
  @Input() isDark = false;
  @Input() currentLang = 'en';

  @Output() menuToggle = new EventEmitter<void>();
  @Output() langToggle = new EventEmitter<void>();
  @Output() themeToggle = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  onMenuToggle(): void {
    this.menuToggle.emit();
  }

  onLangToggle(): void {
    this.langToggle.emit();
  }

  onThemeToggle(): void {
    this.themeToggle.emit();
  }

  onLogout(): void {
    this.logout.emit();
  }
}