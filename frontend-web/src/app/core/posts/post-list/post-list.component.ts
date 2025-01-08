import {Component, inject} from '@angular/core';
import {Post} from "../../../shared/models/post.model";
import {PostService} from "../../../shared/services/post.service";
import {RouterLink} from "@angular/router";
import {MatButton} from "@angular/material/button";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {
  MatCard,
  MatCardActions,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle
} from "@angular/material/card";
import {AuthService} from "../../../shared/services/auth.service";
import {EditPostDialogComponent} from "../edit-post-dialog/edit-post-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    RouterLink,
    MatButton,
    MatFormField,
    FormsModule,
    MatInput,
    MatLabel,
    MatCard,
    MatCardHeader,
    MatCardActions,
    MatCardSubtitle,
    MatCardTitle
  ],
  templateUrl: './post-list.component.html',
  styleUrl: './post-list.component.css'
})
export class PostListComponent {
  posts: Post[] = [];
  filteredPosts: Post[] = [];
  filters = {
    content: '',
    author: '',
    date: ''
  };
  postService: PostService = inject(PostService);
  authService: AuthService = inject(AuthService);
  dialog: MatDialog = inject(MatDialog);

  ngOnInit(): void {
    this.loadAllPosts();
  }

  loadAllPosts(): void {
    this.postService.getAllPublishedPosts().subscribe((data) => {
      this.posts = data;
      this.filteredPosts = data;
    });
  }

  applyFilters(): void {
    this.filteredPosts = this.posts.filter(post => {
      const matchesContent = post.content?.toLowerCase().includes(this.filters.content.toLowerCase());
      const matchesAuthor = post.author?.toLowerCase().includes(this.filters.author.toLowerCase());
      const matchesDate = !this.filters.date || new Date(post.createdAt).toDateString() === new Date(this.filters.date).toDateString();
      return matchesContent && matchesAuthor && matchesDate;
    });
  }

  editPost(post: Post): void {
    const dialogRef = this.dialog.open(EditPostDialogComponent, {
      width: '400px',
      data: post
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Update the post with the new data
        if (post.id !== undefined) {
          Object.assign(post, result);
          this.postService.updatePost(post.id, post).subscribe(() => {
            this.loadAllPosts();
          });
        }
        else {
          console.error('Post ID is undefined');
        }
      }
    });
  }
}
