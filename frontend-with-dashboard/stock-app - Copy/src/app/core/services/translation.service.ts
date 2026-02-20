import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export type Lang = 'en' | 'ar';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private translations: Record<string, Record<string, unknown>> = {};
  private _lang = signal<Lang>('en');

  lang = this._lang.asReadonly();
  isRtl = computed(() => this._lang() === 'ar');

  private loaded = new BehaviorSubject<boolean>(false);
  loaded$ = this.loaded.asObservable();

  constructor(private http: HttpClient) { }

  load(lang: Lang): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`assets/i18n/${lang}.json`).pipe(
      tap(data => {
        this.translations[lang] = data;
        this.loaded.next(true);
      })
    );
  }

  setLang(lang: Lang): void {
    if (!this.translations[lang]) {
      this.load(lang).subscribe(() => this._applyLang(lang));
    } else {
      this._applyLang(lang);
    }
  }

  private _applyLang(lang: Lang): void {
    this._lang.set(lang);
    document.documentElement.setAttribute('lang', lang);
    document.documentElement.setAttribute('dir', lang === 'ar' ? 'rtl' : 'ltr');
    localStorage.setItem('lang', lang);
  }

  t(key: string, params?: Record<string, string | number>): string {
    const parts = key.split('.');
    let val: unknown = this.translations[this._lang()] ?? {};
    for (const part of parts) {
      if (val && typeof val === 'object') {
        val = (val as Record<string, unknown>)[part];
      } else {
        return key;
      }
    }
    let str = typeof val === 'string' ? val : key;
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        str = str.replace(new RegExp(`{{${k}}}`, 'g'), String(v));
      });
    }
    return str;
  }

  initFromStorage(): void {
    const saved = localStorage.getItem('lang') as Lang | null;
    const lang: Lang = saved === 'ar' ? 'ar' : 'en';
    this._applyLang(lang);
    this.load(lang).subscribe();
  }
}
