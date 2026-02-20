import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from '../services/notification.service';
import { TranslationService } from '../services/translation.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private notification: NotificationService, private ts: TranslationService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let key = 'ERRORS.UNEXPECTED';
        if (error.status === 0) key = 'ERRORS.SERVER_UNREACHABLE';
        else if (error.status === 400) key = 'ERRORS.BAD_REQUEST';
        else if (error.status === 404) key = 'ERRORS.NOT_FOUND';
        else if (error.status === 409) key = 'ERRORS.CONFLICT';
        else if (error.status === 500) key = 'ERRORS.SERVER_ERROR';
        this.notification.error(error.error?.message || this.ts.t(key));
        return throwError(() => error);
      })
    );
  }
}
