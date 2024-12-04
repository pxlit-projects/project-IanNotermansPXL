import {Component, inject} from '@angular/core';
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatButton} from "@angular/material/button";
import {MatInput} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {MatDialogActions, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-reject-dialog',
  standalone: true,
  imports: [
    MatFormField,
    MatButton,
    MatInput,
    FormsModule,
    MatDialogActions,
    MatLabel
  ],
  templateUrl: './reject-dialog.component.html',
  styleUrl: './reject-dialog.component.css'
})
export class RejectDialogComponent {
  comment: string = '';
  dialog: MatDialogRef<RejectDialogComponent> = inject(MatDialogRef);


  onCancel(): void {
    this.dialog.close();
  }

  onSubmit(): void {
    this.dialog.close(this.comment);
  }
}
