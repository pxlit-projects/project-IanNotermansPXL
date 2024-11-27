import { Component } from '@angular/core';
import {RouterLink} from "@angular/router";
import {MatToolbar} from "@angular/material/toolbar";
import {MatButton} from "@angular/material/button";

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

}
