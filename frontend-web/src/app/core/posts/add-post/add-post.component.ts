import {Component, inject} from '@angular/core';
import {PostService} from "../../../shared/services/post.service";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatError, MatFormField, MatLabel} from "@angular/material/form-field";

import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {MatCheckbox} from "@angular/material/checkbox";
import {Router} from "@angular/router";
import {AuthService} from "../../../shared/services/auth.service";
import {User} from "../../../shared/models/user.model";

@Component({
  selector: 'app-add-post',
  standalone: true,
  imports: [
    MatFormField,
    FormsModule,
    ReactiveFormsModule,
    MatInput,
    MatButton,
    MatError,
    MatLabel,
    MatCheckbox
],
  templateUrl: './add-post.component.html',
  styleUrl: './add-post.component.css'
})
export class AddPostComponent {
  postService: PostService = inject(PostService);
  fb: FormBuilder = inject(FormBuilder);
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);
  currentUser: User | null = this.authService.currentUser;

  postForm: FormGroup = this.fb.group({
    title: ['', Validators.required],
    content: ['', Validators.required],
    author: [{ value: this.currentUser?.username || '', disabled: true }, Validators.required],
    isConcept: [false], // Add a control for the checkbox
  });

  submitForm(): void {
    if (this.postForm.valid) {
      const newPost = this.postForm.value;
      newPost.createdAt = new Date().toISOString(); // Set the current date and time
      newPost.status = newPost.isConcept ? 'CONCEPT' : 'PUBLISHED'; // Set the status based on the checkbox
      newPost.author = this.currentUser?.username || ''; // Set the author to the current user's username
      this.postService.createPost(newPost).subscribe((result) => {
        console.log('Post created:', result);
        this.router.navigate(['/posts', result.id]);
      });
    }
  }
}
