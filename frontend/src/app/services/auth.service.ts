import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User, UserRegistration, UserLogin, AuthResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
    ? 'http://localhost:3001/api/auth'
    : 'https://monitor-ellas-backend.onrender.com/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private tokenSubject = new BehaviorSubject<string | null>(null);

  public currentUser$ = this.currentUserSubject.asObservable();
  public token$ = this.tokenSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadStoredAuth();
  }

  register(userData: any): Observable<any> {
    // backend agora retorna apenas message; não seta auth até verificação
    return this.http.post<any>(`${this.apiUrl}/cadastro`, userData);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap((response: AuthResponse) => {
          if (response.success && response.data) {
            // Mapear campo de verificação se vier como emailVerificado/nome
            const rawUser: any = response.data.user;
            const mappedUser: User = {
              id: rawUser.id,
              email: rawUser.email,
              nome: rawUser.nome,
              isEmailVerified: rawUser.emailVerificado ?? rawUser.isEmailVerified
            };
            response.data.user = mappedUser;
            this.setAuth(mappedUser, response.data.token);
          }
        })
      );
  }

  verifyEmail(email: string, codigo: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/verificar`, { email, codigo });
  }

  resendCode(email: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/reenviar`, { email });
  }

  logout(): void {
    this.clearAuth();
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  private setAuth(user: User, token: string) {
    this.currentUserSubject.next(user);
    this.tokenSubject.next(token);
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  }

  private clearAuth() {
    this.currentUserSubject.next(null);
    this.tokenSubject.next(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  private loadStoredAuth() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    if (token && user) {
      this.tokenSubject.next(token);
      this.currentUserSubject.next(JSON.parse(user));
    }
  }
}
