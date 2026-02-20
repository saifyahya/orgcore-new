import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthResponse {
    token: string;
}

export interface AuthRequest {
    tenant: {
        tenantName: string;
        address: string;
        email: string; // tenant email
        phone: string;
    };
    firstName: string;
    lastName: string;
    email: string; // user email
    password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private apiUrl = `${environment.apiUrl}/auth`;
    private tokenKey = 'auth_token';

    // Signal to track auth state if needed
    isAuthenticatedStr = signal<boolean>(!!this.getToken());

    constructor(private http: HttpClient, private router: Router) { }

    login(credentials: { email: string; password: string }): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
            tap(response => this.setSession(response))
        );
    }

    signup(data: AuthRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, data).pipe(
            tap(response => this.setSession(response))
        );
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        this.isAuthenticatedStr.set(false);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isAuthenticated(): boolean {
        // Basic check, ideally decode token and check expiry
        const token = this.getToken();
        return !!token;
    }

    private setSession(authResult: AuthResponse): void {
        localStorage.setItem(this.tokenKey, authResult.token);
        this.isAuthenticatedStr.set(true);
        this.router.navigate(['/dashboard']);
    }
}
