import {Component, inject} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from "@angular/material/card";
import {NgIf} from "@angular/common";
import {Post} from "../../../shared/models/post.model";
import {PostService} from "../../../shared/services/post.service";
import {ActivatedRoute} from "@angular/router";
import {MatDivider} from "@angular/material/divider";

@Component({
  selector: 'app-post-item',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardSubtitle,
    MatCardTitle,
    NgIf,
    MatDivider
  ],
  templateUrl: './post-item.component.html',
  styleUrl: './post-item.component.css'
})
export class PostItemComponent {
  selectedPost: Post | undefined;
  postService: PostService = inject(PostService);
  route: ActivatedRoute = inject(ActivatedRoute);

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.postService.getPostById(+postId).subscribe((post) => {
        this.selectedPost = post;
      });
    }
  }
}
