import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PostService } from './post.service';
import { Post } from '../models/post.model';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('PostService', () => {
  let service: PostService;
  let httpMock: HttpTestingController;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  const baseUrl = environment.postApiUrl + '/api/posts';

  const mockPost: Post = new Post(
    'Sample Title',
    'Sample Content',
    'Test Author',
    new Date().toISOString(),
    'draft',
    'Sample Review Comment'
  );
  mockPost.id = 1;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['currentUser'], {
      currentUser: { username: 'testUser', role: 'admin' },
    });

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PostService,
        { provide: AuthService, useValue: authServiceMock },
      ],
    });

    service = TestBed.inject(PostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure no unmatched requests remain
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createPost', () => {
    it('should send a POST request to create a new post', () => {
      service.createPost(mockPost).subscribe((post) => {
        expect(post).toEqual(mockPost);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockPost);
      expect(req.request.headers.get('user')).toBe('testUser');
      expect(req.request.headers.get('role')).toBe('admin');
      req.flush(mockPost);
    });
  });

  describe('getPostById', () => {
    it('should retrieve a post by ID', () => {
      service.getPostById(1).subscribe((post) => {
        expect(post).toEqual(mockPost);
      });

      const req = httpMock.expectOne(`${baseUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPost);
    });
  });

  describe('getPostsByStatus', () => {
    it('should retrieve posts by status', () => {
      const status = 'draft';
      service.getPostsByStatus(status).subscribe((posts) => {
        expect(posts).toEqual([mockPost]);
      });

      const req = httpMock.expectOne(`${baseUrl}/status/${status}`);
      expect(req.request.method).toBe('GET');
      req.flush([mockPost]);
    });
  });

  describe('getAllPublishedPosts', () => {
    it('should retrieve all published posts', () => {
      service.getAllPublishedPosts().subscribe((posts) => {
        expect(posts).toEqual([mockPost]);
      });

      const req = httpMock.expectOne(`${baseUrl}/publishedPosts`);
      expect(req.request.method).toBe('GET');
      req.flush([mockPost]);
    });
  });

  describe('getAllNotPublishedPosts', () => {
    it('should retrieve all not published posts', () => {
      service.getAllNotPublishedPosts().subscribe((posts) => {
        expect(posts).toEqual([mockPost]);
      });

      const req = httpMock.expectOne(`${baseUrl}/not-published`);
      expect(req.request.method).toBe('GET');
      req.flush([mockPost]);
    });
  });

  describe('updatePost', () => {
    it('should send a PUT request to update a post', () => {
      service.updatePost(1, mockPost).subscribe((post) => {
        expect(post).toEqual(mockPost);
      });

      const req = httpMock.expectOne(`${baseUrl}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockPost);
      req.flush(mockPost);
    });
  });

  describe('publishPost', () => {
    it('should send a PUT request to publish a post', () => {
      service.publishPost(1).subscribe((post) => {
        expect(post).toEqual(mockPost);
      });

      const req = httpMock.expectOne(`${baseUrl}/1/publish`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({});
      req.flush(mockPost);
    });
  });
});
