import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddCommentDialogComponent } from './add-comment-dialog.component';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CommentService } from '../../../shared/services/comment.service';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import {NoopAnimationsModule} from "@angular/platform-browser/animations";
import { Comment } from '../../../shared/models/comment.model';

describe('AddCommentDialogComponent', () => {
  let component: AddCommentDialogComponent;
  let fixture: ComponentFixture<AddCommentDialogComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<AddCommentDialogComponent>>;
  let mockCommentService: jasmine.SpyObj<CommentService>;

  let mockComment = { id: 1, postId: 1, commenter: "commenter" , text: 'Test comment', addedAt: '2025-01-12T12:00:00Z' };

  beforeEach(async () => {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockCommentService = jasmine.createSpyObj('CommentService', ['addComment']);

    await TestBed.configureTestingModule({
      imports: [AddCommentDialogComponent, FormsModule, NoopAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: { postId: 1 } },
        { provide: CommentService, useValue: mockCommentService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddCommentDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should close the dialog with false when closeDialog is called', () => {
    component.closeDialog();
    expect(mockDialogRef.close).toHaveBeenCalledWith(false);
  });

  it('should not call addComment if commentText is empty', () => {
    component.commentText = '';
    component.addComment();
    expect(mockCommentService.addComment).not.toHaveBeenCalled();
    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('should call addComment and close the dialog with the returned comment when a valid comment is added', () => {
    const commentRequest = { postId: 1, text: 'Test comment' };
    mockCommentService.addComment.and.returnValue(of(mockComment));

    component.commentText = 'Test comment';
    component.addComment();

    expect(mockCommentService.addComment).toHaveBeenCalledWith(commentRequest);
  });
});
