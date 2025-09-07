// src/app/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs'; // Cần thiết cho các kiểu trả về

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // Kiểm tra cờ 'isLoggedIn' trong localStorage
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    console.log('AuthGuard: Checking login status from localStorage. Logged In:', isLoggedIn);

    if (!isLoggedIn) {
      // If not logged in, redirect to the login page '/log-in'
      console.log('AuthGuard: User is NOT logged in. Redirecting to /log-in');
      return this.router.createUrlTree(['/log-in']); // Return UrlTree for redirection
    }
    // If logged in, allow access
    console.log('AuthGuard: User IS logged in. Allowing access.');
    return true; // Allow access
  }
}
