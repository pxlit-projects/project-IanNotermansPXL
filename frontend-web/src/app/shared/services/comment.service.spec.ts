import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommentService } from './comment.service';
import { environment } from '../../../environments/environment';
import { Comment } from '../models/comment.model';

describe('CommentService', () => {
  let service: CommentService;
  let httpMock: HttpTestingController;

  const mockComment: Comment = new Comment(
    1,
    'testUser',
    'Sample comment',
    new Date().toISOString()
  );
  mockComment.id = 1;

  const baseUrl = environment.commentApiUrl + '/api/comments';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CommentService],
    });
    service = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure no unmatched requests remain
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCommentsByPostId', () => {
    it('should retrieve comments by post ID', () => {
      const postId = 1;
      service.getCommentsByPostId(postId).subscribe((comments) => {
        expect(comments).toEqual([mockComment]);
      });

      const req = httpMock.expectOne(`${baseUrl}/${postId}`);
      expect(req.request.method).toBe('GET');
      req.flush([mockComment]);
    });

    it('should handle an error when retrieving comments', () => {
      const postId = 1;
      service.getCommentsByPostId(postId).subscribe({
        next: () => fail('Should have failed with an error'),
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(`${baseUrl}/${postId}`);
      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('addComment', () => {
    it('should add a new comment', () => {
      const request = { postId: 1, text: 'New comment', commenter: 'testUser', addedAt: new Date().toISOString() };

      service.addComment(request).subscribe((comment) => {
        expect(comment).toEqual(mockComment);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockComment);
    });

    it('should handle an error when adding a comment', () => {
      const request = { postId: 1, text: 'New comment', commenter: 'testUser', addedAt: new Date().toISOString() };

      service.addComment(request).subscribe({
        next: () => fail('Should have failed with an error'),
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(baseUrl);
      req.flush('Error', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('updateComment', () => {
    it('should update a comment', () => {
      const commentId = 1;
      const text = 'Updated comment';

      service.updateComment(commentId, text).subscribe((comment) => {
        expect(comment).toEqual(mockComment);
      });

      const req = httpMock.expectOne(`${baseUrl}/${commentId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ text });
      req.flush(mockComment);
    });

    it('should handle an error when updating a comment', () => {
      const commentId = 1;
      const text = 'Updated comment';

      service.updateComment(commentId, text).subscribe({
        next: () => fail('Should have failed with an error'),
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(`${baseUrl}/${commentId}`);
      req.flush('Error', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('deleteComment', () => {
    it('should delete a comment', () => {
      const commentId = 1;

      service.deleteComment(commentId).subscribe((response) => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/${commentId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null); // Simulate no content response
    });

    it('should handle an error when deleting a comment', () => {
      const commentId = 1;

      service.deleteComment(commentId).subscribe({
        next: () => fail('Should have failed with an error'),
        error: (error) => {
          expect(error).toBeTruthy();
        },
      });

      const req = httpMock.expectOne(`${baseUrl}/${commentId}`);
      req.flush('Error', { status: 403, statusText: 'Forbidden' });
    });
  });
});
