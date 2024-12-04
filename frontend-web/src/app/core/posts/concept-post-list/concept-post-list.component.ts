import {Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {Post} from "../../../shared/models/post.model";
import {PostService} from "../../../shared/services/post.service";
import {RouterModule} from "@angular/router";
import {MatCard, MatCardActions, MatCardHeader, MatCardSubtitle, MatCardTitle} from "@angular/material/card";
import {MatDialog} from "@angular/material/dialog";
import {EditPostDialogComponent} from "../edit-post-dialog/edit-post-dialog.component";
import {NgClass} from "@angular/common";

@Component({
  selector: 'app-concept-post-list',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
    RouterModule,
    MatCard,
    MatCardActions,
    MatCardHeader,
    MatCardSubtitle,
    MatCardTitle,
    MatLabel,
    NgClass,

  ],
  templateUrl: './concept-post-list.component.html',
  styleUrl: './concept-post-list.component.css'
})
export class ConceptPostListComponent {
  posts: Post[] = [];
  filteredPosts: Post[] = [];
  filters = {
    content: '',
    author: '',
    date: ''
  };
  postService: PostService = inject(PostService);
  dialog: MatDialog = inject(MatDialog);

  ngOnInit(): void {
    this.loadAllPosts();
  }

  loadAllPosts(): void {
    this.postService.getAllNotPublishedPosts().subscribe((data) => {
      this.posts = data;
      this.filteredPosts = data;
    });
  }

  publishPost(post: Post): void {
    if (post.id !== undefined) {
      this.postService.publishPost(post.id).subscribe(() => {
        this.loadAllPosts();
      });
    } else {
      console.error('Post ID is undefined');
    }
  }

  applyFilters(): void {
    this.filteredPosts = this.posts.filter(post => {
      const matchesContent = post.content?.toLowerCase().includes(this.filters.content.toLowerCase());
      const matchesAuthor = post.author?.toLowerCase().includes(this.filters.author.toLowerCase());
      const matchesDate = !this.filters.date || new Date(post.createdAt).toDateString() === new Date(this.filters.date).toDateString();
      return matchesContent && matchesAuthor && matchesDate;
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'status-approved';
      case 'CONCEPT':
        return 'status-concept';
      case 'REJECTED':
        return 'status-rejected';
      default:
        return '';
    }
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
