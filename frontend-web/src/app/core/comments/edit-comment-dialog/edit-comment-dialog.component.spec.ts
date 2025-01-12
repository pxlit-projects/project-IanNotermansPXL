import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EditCommentDialogComponent } from './edit-comment-dialog.component';
import { CommentService } from '../../../shared/services/comment.service';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import {NoopAnimationsModule} from "@angular/platform-browser/animations";

describe('EditCommentDialogComponent', () => {
  let component: EditCommentDialogComponent;
  let fixture: ComponentFixture<EditCommentDialogComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<EditCommentDialogComponent>>;
  let mockCommentService: jasmine.SpyObj<CommentService>;

  beforeEach(async () => {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockCommentService = jasmine.createSpyObj('CommentService', ['updateComment']);

    await TestBed.configureTestingModule({
      imports: [
        EditCommentDialogComponent,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: { commentId: 1, currentText: 'Initial Comment' } },
        { provide: CommentService, useValue: mockCommentService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditCommentDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with provided comment text', () => {
    expect(component.updatedComment).toBe('Initial Comment');
  });

  it('should close the dialog with false when closeDialog is called', () => {
    component.closeDialog();
    expect(mockDialogRef.close).toHaveBeenCalledWith(false);
  });

  it('should call updateComment and close the dialog with the updated comment when saveChanges is called', () => {
    const updatedText = 'Updated Comment';
    const updatedComment = { id: 1, text: updatedText, postId: 1, commenter: 'John Doe', addedAt: '2025-01-12T12:00:00Z' };

    component.updatedComment = updatedText;

    // Mock the service to return the updated comment
    mockCommentService.updateComment.and.returnValue(of(updatedComment));

    component.saveChanges();

    // Verify the service method is called with the correct arguments
    expect(mockCommentService.updateComment).toHaveBeenCalledWith(1, updatedText);

    // Verify the dialog is closed with the updated comment
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should not call updateComment if the updatedComment is empty', () => {
    component.updatedComment = '   ';
    component.saveChanges();
    expect(mockCommentService.updateComment).not.toHaveBeenCalled();
    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });
});
