import {Component, inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Post} from '../../../shared/models/post.model';
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";

@Component({
  selector: 'app-edit-post-dialog',
  templateUrl: './edit-post-dialog.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatDialogTitle,
    MatDialogContent,
    MatInput,
    MatDialogActions,
    MatButton,
    MatLabel
  ],
  styleUrls: ['./edit-post-dialog.component.css']
})
export class EditPostDialogComponent {
  private dialogRef = inject<MatDialogRef<EditPostDialogComponent>>(MatDialogRef);
  public data = inject<Post>(MAT_DIALOG_DATA);
  private fb = inject(FormBuilder);

  editPostForm: FormGroup;

  constructor() {
    this.editPostForm = this.fb.group({
      title: [this.data.title, Validators.required],
      content: [this.data.content, Validators.required],
    });
  }


  save(): void {
    if (this.editPostForm.valid) {
      this.dialogRef.close(this.editPostForm.value);
    }
  }

  close(): void {
    this.dialogRef.close();
  }
}
