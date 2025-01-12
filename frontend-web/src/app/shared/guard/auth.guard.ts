import {inject, Injectable} from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
} from '@angular/router';
import {AuthService} from "../services/auth.service";


@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  authService : AuthService = inject(AuthService);
  router : Router = inject(Router);

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const currentUser = this.authService.currentUser;
    if (currentUser) {
      const { role } = route.data;
      if (role && role !== currentUser.role) {
        this.router.navigate(['/']);
        return false;
      }
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
