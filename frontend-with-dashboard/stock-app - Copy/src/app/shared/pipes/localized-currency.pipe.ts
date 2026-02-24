import { Pipe, PipeTransform, ChangeDetectorRef } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { TranslationService } from '../../core/services/translation.service';
import { effect } from '@angular/core';

@Pipe({
  name: 'localizedCurrency',
  standalone: true,
  pure: false
})
export class LocalizedCurrencyPipe implements PipeTransform {
  
  constructor(private ts: TranslationService, private cd: ChangeDetectorRef) {
    effect(() => {
      ts.lang(); // track lang signal
      this.cd.markForCheck();
    });
  }

  transform(value: number | string | null | undefined, displaySymbol: boolean = true, decimals: number = 2): string {
    if (value === null || value === undefined || value === '') {
      return '';
    }

    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) {
      return '';
    }

    const lang = this.ts.lang();
    const formatted = numValue.toFixed(decimals);

    if (lang === 'ar') {
      // Arabic format: 1,234.56 دينار or 1,235 دينار
      const parts = formatted.split('.');
      const integerPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
      const display = decimals === 0 ? integerPart : `${integerPart}.${parts[1]}`;
      return displaySymbol 
        ? `${display} JOD`
        : display;
    } else {
      // English format: JOD 1,234.56 or JOD 1,235
      const parts = formatted.split('.');
      const integerPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
      const display = decimals === 0 ? integerPart : `${integerPart}.${parts[1]}`;
      return displaySymbol 
        ? `JOD ${display}`
        : display;
    }
  }
}
