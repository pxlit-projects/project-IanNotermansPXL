import {Component, inject} from '@angular/core';
import {MatLine} from "@angular/material/core";
import {MatList, MatListItem} from "@angular/material/list";
import {NgForOf} from "@angular/common";
import {Post} from "../../../shared/models/post.model";
import {PostService} from "../../../shared/services/post.service";
import {RouterLink} from "@angular/router";
import {MatButton} from "@angular/material/button";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    MatLine,
    MatList,
    MatListItem,
    NgForOf,
    RouterLink,
    MatButton,
    MatFormField,
    FormsModule,
    MatInput,
    MatLabel
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

  ngOnInit(): void {
    this.loadAllPosts();
  }

  loadAllPosts(): void {
    this.postService.getPostsByStatus("PUBLISHED").subscribe((data) => {
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
}
