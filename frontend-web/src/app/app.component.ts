import {Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {EditorComponent} from "./core/navbar/editor/editor.component";
import {UserComponent} from "./core/navbar/user/user.component";
import {User} from "./shared/models/user.model";
import {AuthService} from "./shared/services/auth.service";


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, EditorComponent, UserComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'

})
export class AppComponent {
  title = 'PostApp';
  currentUser: User | null = null;
  authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

  }}
