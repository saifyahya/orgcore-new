import { Pipe, PipeTransform, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { TranslationService } from '../../core/services/translation.service';
import { effect } from '@angular/core';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false
})
export class TranslatePipe implements PipeTransform {
  private lastValue = '';
  private lastKey = '';

  constructor(private ts: TranslationService, private cd: ChangeDetectorRef) {
    effect(() => {
      ts.lang(); // track lang signal
      this.cd.markForCheck();
    });
  }

  transform(key: string, params?: Record<string, string | number>): string {
    return this.ts.t(key, params);
  }
}
