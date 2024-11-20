import {Component, OnInit} from '@angular/core';
import {Post, PostService} from "../services/post.service";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {NgForOf, NgIf} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {MatLineModule} from "@angular/material/core";

@Component({
  selector: 'app-post',
  standalone: true,
  imports: [
    MatDividerModule,
    MatListModule,
    NgForOf,
    MatIconModule,
    NgIf,
    MatCardModule,
    MatButtonModule,
    MatLineModule
  ],
  templateUrl: './post.component.html',
  styleUrl: './post.component.css'
})
export class PostComponent implements OnInit {
  posts: Post[] = [];
  selectedPost: Post | null = null;

  constructor(private postService: PostService) {}

  ngOnInit(): void {
    this.loadAllPosts();
  }

  loadAllPosts(): void {
    this.postService.getAllPosts().subscribe((data) => {
      this.posts = data;
    });
  }

  loadPostById(id: number): void {
    this.postService.getPostById(id).subscribe((data) => {
      this.selectedPost = data;
    });
  }

  loadPostsByStatus(status: string): void {
    this.postService.getPostsByStatus(status).subscribe((data) => {
      this.posts = data;
    });
  }

  createPost(): void {
    const newPost: Post = {
      id: this.posts.length + 1,
      title: 'New Post',
      content: 'Content of the new post.',
      author: 'Author Name',
      createdAt: new Date().toISOString(),
      status: 'CONCEPT',
    };

    this.postService.createPost(newPost).subscribe(() => {
      this.loadAllPosts();
    });
  }
}

