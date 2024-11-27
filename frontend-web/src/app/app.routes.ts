import { Routes } from '@angular/router';
import {PostListComponent} from "./core/posts/post-list/post-list.component";
import {PostItemComponent} from "./core/posts/post-item/post-item.component";
import {AddPostComponent} from "./core/posts/add-post/add-post.component";
import {LoginComponent} from "./core/login/login.component";
import {AuthGuard} from "./auth.guard";

export const routes: Routes = [
  { path: 'posts', component: PostListComponent, canActivate: [AuthGuard] },
  { path: 'posts/:id', component: PostItemComponent, canActivate: [AuthGuard] },
  { path: 'add-post', component: AddPostComponent, canActivate: [AuthGuard], data: { role: 'editor' } },
  { path: '', redirectTo: '/posts', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
];
