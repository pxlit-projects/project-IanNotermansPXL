import {Component, inject} from '@angular/core';
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle
} from "@angular/material/card";

import {Post} from "../../../shared/models/post.model";
import {PostService} from "../../../shared/services/post.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MatDivider} from "@angular/material/divider";
import {DatePipe} from "@angular/common";
import {ReviewService} from "../../../shared/services/review.service";
import {AuthService} from "../../../shared/services/auth.service";
import {MatButton} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {RejectDialogComponent} from "../reject-dialog/reject-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-post-item',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardSubtitle,
    MatCardTitle,
    MatDivider,
    MatCardActions,
    MatButton,
    MatIconModule
  ],
  providers: [DatePipe],
  templateUrl: './post-item.component.html',
  styleUrl: './post-item.component.css'
})
export class PostItemComponent {
  selectedPost: Post | undefined;
  postService: PostService = inject(PostService);
  authService: AuthService = inject(AuthService);
  reviewService: ReviewService = inject(ReviewService);
  route: ActivatedRoute = inject(ActivatedRoute);
  datePipe: DatePipe = inject(DatePipe);
  dialog: MatDialog = inject(MatDialog);
  router: Router = inject(Router);

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.postService.getPostById(+postId).subscribe((post) => {
        this.selectedPost = post;
      });
    }
  }

  formatDate(date: string): string | null {
    return this.datePipe.transform(date, 'MMMM d, y, h:mm a');
  }

  isEditor(): boolean {
    const currentUser = this.authService.currentUser;
    return currentUser?.role === 'editor';
  }

  reviewPost(approve: boolean): void {
    const postId = this.selectedPost?.id;
    const username = this.authService.currentUser?.username;

    if (postId !== undefined && username !== undefined) {
      if (approve) {
        this.reviewService.reviewPost(postId, username, true, "").subscribe(() => {
          this.router.navigate(['/concepts']);
        });
      } else {
        const dialogRef = this.dialog.open(RejectDialogComponent);

        dialogRef.afterClosed().subscribe(result => {
          if (result) {
            this.reviewService.reviewPost(postId, username, false, result).subscribe(() => {
              this.router.navigate(['/concepts']);
            });
          }
        });
      }
    }
  }
}
