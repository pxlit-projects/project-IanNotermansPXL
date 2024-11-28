import {Component, inject} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {MatToolbar} from "@angular/material/toolbar";
import {MatButton} from "@angular/material/button";
import {AuthService} from "../../../shared/services/auth.service";

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [
    RouterLink,
    MatToolbar,
    MatButton
  ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent {
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);

  logout(): void {
    this.authService.logout();
  }
}
