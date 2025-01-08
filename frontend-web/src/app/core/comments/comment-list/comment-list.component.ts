import {Component, inject, Input} from '@angular/core';
import { Comment } from '../../../shared/models/comment.model';
import { CommentService } from '../../../shared/services/comment.service';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardSubtitle} from "@angular/material/card";
import {DatePipe} from "@angular/common";
import {MatButton} from "@angular/material/button";
import {AuthService} from "../../../shared/services/auth.service";
import {MatIcon} from "@angular/material/icon";
import {MatDialog} from "@angular/material/dialog";
import {EditCommentDialogComponent} from "../edit-comment-dialog/edit-comment-dialog.component";

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css'],
  imports: [
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatCardActions,
    DatePipe,
    MatCardSubtitle,
    MatButton,
    MatIcon
  ],
  standalone: true
})
export class CommentListComponent {
  @Input() comments: Comment[] = [];
  private commentService: CommentService = inject(CommentService);
  private authService: AuthService = inject(AuthService);
  currentUser: string | undefined = this.authService.currentUser?.username;
  private dialog: MatDialog = inject(MatDialog);



  deleteComment(commentId: number | undefined): void {
    if (commentId != null) {
      this.commentService.deleteComment(commentId).subscribe(() => {
        this.comments = this.comments.filter((comment) => comment.id !== commentId);
      });
    }
  }

  openEditCommentDialog(comment: Comment): void {
    const dialogRef = this.dialog.open(EditCommentDialogComponent, {
      width: '400px',
      data: { commentId: comment.id, currentText: comment.text },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        location.reload()
      }
    });
  }
}
