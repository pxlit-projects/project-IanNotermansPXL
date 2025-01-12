import { TestBed, ComponentFixture } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConceptPostListComponent } from './concept-post-list.component';
import { PostService } from '../../../shared/services/post.service';
import { MatDialog } from '@angular/material/dialog';
import { EditPostDialogComponent } from '../edit-post-dialog/edit-post-dialog.component';

describe('ConceptPostListComponent', () => {
  let component: ConceptPostListComponent;
  let fixture: ComponentFixture<ConceptPostListComponent>;
  let mockPostService: jasmine.SpyObj<PostService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  beforeEach(async () => {
    mockPostService = jasmine.createSpyObj('PostService', ['getAllNotPublishedPosts', 'publishPost', 'updatePost']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);

    await TestBed.configureTestingModule({
      imports: [ConceptPostListComponent],
      providers: [
        { provide: PostService, useValue: mockPostService },
        { provide: MatDialog, useValue: mockDialog },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConceptPostListComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load all posts on initialization', () => {
    const mockPosts = [{ id: 1, title: 'Title 1', content: 'Content 1', author: 'Author 1', createdAt: new Date().toISOString(), status: 'CONCEPT', reviewComment: 'Review 1' }];
    mockPostService.getAllNotPublishedPosts.and.returnValue(of(mockPosts));

    component.ngOnInit();

    expect(mockPostService.getAllNotPublishedPosts).toHaveBeenCalled();
    expect(component.posts).toEqual(mockPosts);
    expect(component.filteredPosts).toEqual(mockPosts);
  });


  it('should log an error if post ID is undefined during publish', () => {
    spyOn(console, 'error');
    component.publishPost({ id: undefined, title: '', content: '', author: '', createdAt: '', status: 'CONCEPT', reviewComment: '' });

    expect(console.error).toHaveBeenCalledWith('Post ID is undefined');
  });

  it('should apply filters correctly', () => {
    const mockPosts = [{ id: 1, title: 'Title Match', content: 'Content Match', author: 'Author Match', createdAt: new Date().toISOString(), status: 'CONCEPT', reviewComment: 'Review Match' }];
    component.posts = mockPosts;
    component.filters = { content: 'Content', author: 'Author', date: '' };

    component.applyFilters();

    expect(component.filteredPosts).toEqual([mockPosts[0]]);
  });

  it('should open the edit dialog and update a post on save', () => {
    const mockPost = { id: 1, title: 'Original Title', content: 'Original Content', author: 'Author 1', createdAt: new Date().toISOString(), status: 'CONCEPT', reviewComment: 'Review Comment' };
    const updatedPost = { ...mockPost, content: 'Updated Content' };
    const dialogRef = { afterClosed: jasmine.createSpy('afterClosed').and.returnValue(of(updatedPost)) };

    mockDialog.open.and.returnValue(dialogRef as any);
    mockPostService.updatePost.and.returnValue(of(updatedPost));
    spyOn(component, 'loadAllPosts');

    component.editPost(mockPost);

    expect(mockDialog.open).toHaveBeenCalledWith(EditPostDialogComponent, { width: '400px', data: mockPost });
    expect(dialogRef.afterClosed).toHaveBeenCalled();
    expect(mockPostService.updatePost).toHaveBeenCalledWith(mockPost.id, updatedPost);
    expect(component.loadAllPosts).toHaveBeenCalled();
  });

  it('should log an error if post ID is undefined during edit', () => {
    spyOn(console, 'error');
    const dialogRef = { afterClosed: jasmine.createSpy('afterClosed').and.returnValue(of({ content: 'Updated' })) };
    mockDialog.open.and.returnValue(dialogRef as any);

    component.editPost({ id: undefined, title: '', content: '', author: '', createdAt: '', status: 'CONCEPT', reviewComment: '' });

    expect(console.error).toHaveBeenCalledWith('Post ID is undefined');
  });
});
