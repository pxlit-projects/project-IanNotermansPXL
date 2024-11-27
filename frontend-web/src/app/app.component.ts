import {Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {MatToolbar} from "@angular/material/toolbar";
import {MatCard, MatCardContent} from "@angular/material/card";
import {EditorComponent} from "./core/navbar/editor/editor.component";
import {UserComponent} from "./core/navbar/user/user.component";
import {User} from "./shared/models/user.model";
import {AuthService} from "./shared/services/auth.service";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbar, MatCardContent, MatCard, EditorComponent, UserComponent, NgIf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'

})
export class AppComponent {
  currentUser: User | null = null;
  authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

  }}
