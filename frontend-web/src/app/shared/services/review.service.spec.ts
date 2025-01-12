import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReviewService } from './review.service';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  const baseUrl = environment.reviewApiUrl + '/api/review';

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj('AuthService', [], {
      currentUser: { username: 'testUser', role: 'editor' },
    });

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ReviewService,
        { provide: AuthService, useValue: authServiceMock },
      ],
    });

    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure no unmatched requests remain
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('reviewPost', () => {
    it('should send a POST request to review a post', () => {
      const postId = 1;
      const editor = 'testEditor';
      const approved = true;
      const reviewComment = 'Looks good!';

      service.reviewPost(postId, editor, approved, reviewComment).subscribe((response) => {
        expect(response).toEqual({ success: true });
      });

      const req = httpMock.expectOne(`${baseUrl}/${postId}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ editor, approved, reviewComment });
      expect(req.request.headers.get('user')).toBe('testUser');
      expect(req.request.headers.get('role')).toBe('editor');
      req.flush({ success: true });
    });

    it('should handle errors correctly', () => {
      const postId = 1;
      const editor = 'testEditor';
      const approved = false;
      const reviewComment = 'Needs more detail.';

      service.reviewPost(postId, editor, approved, reviewComment).subscribe({
        next: () => fail('Should have failed with an error'),
        error: (error) => {
          expect(error.status).toBe(400);
          expect(error.statusText).toBe('Bad Request');
        },
      });

      const req = httpMock.expectOne(`${baseUrl}/${postId}`);
      req.flush('Error', { status: 400, statusText: 'Bad Request' });
    });
  });
});
