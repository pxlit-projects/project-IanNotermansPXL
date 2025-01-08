import {Component, inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialogContent, MatDialogActions} from '@angular/material/dialog';
import { CommentService } from '../../../shared/services/comment.service';
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";

@Component({
  selector: 'app-edit-comment-dialog',
  templateUrl: './edit-comment-dialog.component.html',
  styleUrls: ['./edit-comment-dialog.component.css'],
  imports: [
    MatDialogContent,
    MatLabel,
    MatFormField,
    FormsModule,
    MatInput,
    MatDialogActions,
    MatButton
  ],
  standalone: true
})
export class EditCommentDialogComponent {
  dialogRef = inject(MatDialogRef<EditCommentDialogComponent>);
  data = inject<{ commentId: number; currentText: string }>(MAT_DIALOG_DATA);
  commentService = inject(CommentService);
  updatedComment: string = this.data?.currentText ?? '';

  saveChanges(): void {
    if (this.updatedComment.trim()) {
      this.commentService.updateComment(this.data.commentId, this.updatedComment).subscribe(() => {
        this.dialogRef.close(true);
      });
    }
  }

  closeDialog(): void {
    this.dialogRef.close(false);
  }
}
