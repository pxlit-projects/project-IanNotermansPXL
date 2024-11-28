import {Component, inject} from '@angular/core';
import {MatToolbar} from "@angular/material/toolbar";
import {Router, RouterLink} from "@angular/router";
import {MatButton} from "@angular/material/button";
import {AuthService} from "../../../shared/services/auth.service";

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [
    MatToolbar,
    RouterLink,
    MatButton
  ],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent {
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);

  logout(): void {
    this.authService.logout();
  }
}
