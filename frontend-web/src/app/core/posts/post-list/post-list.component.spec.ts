import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostListComponent } from './post-list.component';
import { PostService } from '../../../shared/services/post.service';
import { AuthService } from '../../../shared/services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Post } from '../../../shared/models/post.model';
import { By } from '@angular/platform-browser';

describe('PostListComponent', () => {
  let component: PostListComponent;
  let fixture: ComponentFixture<PostListComponent>;
  let mockPostService: jasmine.SpyObj<PostService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  const mockPosts: Post[] = [
    {
      id: 1,
      title: 'Post 1',
      content: 'Content 1',
      author: 'Author 1',
      createdAt: '2023-01-01T00:00:00Z',
      status: 'published',
      reviewComment: ''
    },
    {
      id: 2,
      title: 'Post 2',
      content: 'Content 2',
      author: 'Author 2',
      createdAt: '2023-01-02T00:00:00Z',
      status: 'published',
      reviewComment: ''
    }
  ];

  beforeEach(async () => {
    mockPostService = jasmine.createSpyObj('PostService', ['getAllPublishedPosts', 'updatePost']);
    mockAuthService = jasmine.createSpyObj('AuthService', [], { currentUser: { username: 'testUser', role: 'admin' } });
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule, // Required to handle animations
        PostListComponent,   // Component under test
      ],
      providers: [
        { provide: PostService, useValue: mockPostService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: MatDialog, useValue: mockDialog },
        {
          provide: ActivatedRoute, // Mock ActivatedRoute
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? '1' : null),
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostListComponent);
    component = fixture.componentInstance;

    // Mock data for the service
    mockPostService.getAllPublishedPosts.and.returnValue(of(mockPosts));

    fixture.detectChanges(); // Trigger ngOnInit
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load all posts on initialization', () => {
    expect(component.posts).toEqual(mockPosts);
    expect(component.filteredPosts).toEqual(mockPosts);
    expect(mockPostService.getAllPublishedPosts).toHaveBeenCalled();
  });

  it('should filter posts based on content', () => {
    component.filters.content = 'Content 1';
    component.applyFilters();

    expect(component.filteredPosts).toEqual([mockPosts[0]]);
  });

  it('should filter posts based on author', () => {
    component.filters.author = 'Author 2';
    component.applyFilters();

    expect(component.filteredPosts).toEqual([mockPosts[1]]);
  });

  it('should filter posts based on date', () => {
    component.filters.date = '2023-01-01';
    component.applyFilters();

    expect(component.filteredPosts).toEqual([mockPosts[0]]);
  });

  it('should call updatePost with the correct arguments and return updated post', () => {
    const updatedPost = {
      id: 1,
      title: 'Updated Post 1',
      content: 'Updated Content',
      author: 'Author 1',
      createdAt: '2023-01-01T00:00:00Z',
      status: 'published',
      reviewComment: ''
    };

    const mockDialogRef = {
      afterClosed: jasmine.createSpy('afterClosed').and.returnValue(of({ title: 'Updated Post 1' })),
    };
    mockDialog.open.and.returnValue(mockDialogRef as any);

    mockPostService.updatePost.and.returnValue(of(updatedPost)); // Return a valid Observable<Post>

    component.editPost(mockPosts[0]);

    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockDialogRef.afterClosed).toHaveBeenCalled();
    expect(mockPostService.updatePost).toHaveBeenCalledWith(
      mockPosts[0].id!,
      jasmine.objectContaining({ title: 'Updated Post 1' })
    );
  });


  it('should not update the post if dialog result is undefined', () => {
    const mockDialogRef = {
      afterClosed: jasmine.createSpy('afterClosed').and.returnValue(of(undefined)),
    };
    mockDialog.open.and.returnValue(mockDialogRef as any);

    component.editPost(mockPosts[0]);

    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockDialogRef.afterClosed).toHaveBeenCalled();
    expect(mockPostService.updatePost).not.toHaveBeenCalled();
  });

  it('should display no posts when all filters exclude all posts', () => {
    component.filters.author = 'Nonexistent Author';
    component.applyFilters();

    expect(component.filteredPosts.length).toBe(0);
  });
});
