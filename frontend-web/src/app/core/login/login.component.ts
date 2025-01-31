import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../shared/services/auth.service';
import { FormsModule } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatFormField, MatLabel, MatSelect } from '@angular/material/select';
import { MatButton } from '@angular/material/button';
import { MatInput } from '@angular/material/input';

import {MatError} from "@angular/material/form-field";

@Component({
  selector: 'app-login',
  template: `
    <form #loginForm="ngForm" (ngSubmit)="onSubmit()" class="login-form">
      <mat-form-field appearance="fill">
        <mat-label>Username</mat-label>
        <input
          matInput
          type="text"
          [(ngModel)]="username"
          name="username"
          required
          #usernameInput="ngModel"
          />
          @if (usernameInput.invalid && usernameInput.touched) {
            <mat-error>
              Username is required.
            </mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="fill">
          <mat-label>Role</mat-label>
          <mat-select
            [(ngModel)]="role"
            name="role"
            required
            #roleInput="ngModel"
            >
            <mat-option value="editor">Editor</mat-option>
            <mat-option value="user">User</mat-option>
          </mat-select>
          @if (roleInput.invalid && roleInput.touched) {
            <mat-error>
              Role is required.
            </mat-error>
          }
        </mat-form-field>
        <button
          mat-raised-button
          color="primary"
          type="submit"
          [disabled]="loginForm.invalid"
          >
          Login
        </button>
      </form>
    `,
  styles: [
    `
      .login-form {
        display: flex;
        flex-direction: column;
        max-width: 300px;
        margin: auto;
        padding: 20px;
        border: 1px solid #ccc;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      mat-form-field {
        margin-bottom: 16px;
      }
    `,
  ],
  imports: [
    FormsModule,
    MatOption,
    MatSelect,
    MatLabel,
    MatFormField,
    MatButton,
    MatInput,
    MatError
],
  standalone: true,
})
export class LoginComponent {
  username = '';
  role = 'user';
  private authService: AuthService = inject(AuthService);
  private router: Router = inject(Router);

  onSubmit(): void {
    if (!this.username || !this.role) {
      alert('All fields are required.');
      return;
    }
    this.authService.login(this.username, this.role);
    this.router.navigate(['/']);
  }
}
