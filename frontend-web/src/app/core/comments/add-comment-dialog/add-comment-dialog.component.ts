import {Component, inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialogContent, MatDialogActions} from '@angular/material/dialog';
import { CommentService } from '../../../shared/services/comment.service';
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MatInput} from "@angular/material/input";

@Component({
  selector: 'app-add-comment-dialog',
  templateUrl: './add-comment-dialog.component.html',
  styleUrls: ['./add-comment-dialog.component.css'],
  imports: [
    MatDialogContent,
    MatLabel,
    MatFormField,
    FormsModule,
    MatDialogActions,
    MatButton,
    MatInput
  ],
  standalone: true
})
export class AddCommentDialogComponent {
  dialogRef = inject(MatDialogRef<AddCommentDialogComponent>);
  data = inject<{ postId: number }>(MAT_DIALOG_DATA);
  commentService = inject(CommentService);
  commentText: string = '';

  addComment(): void {
    if (this.commentText.trim()) {
      const request = {
        postId: this.data.postId,
        text: this.commentText.trim(),
      };

      this.commentService.addComment(request).subscribe(() => {
        this.dialogRef.close(true); // Close the dialog and return success
      });
    }
  }

  closeDialog(): void {
    this.dialogRef.close(false); // Close the dialog without adding a comment
  }
}
