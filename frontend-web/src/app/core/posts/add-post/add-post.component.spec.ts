import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddPostComponent } from './add-post.component';
import { PostService } from '../../../shared/services/post.service';
import { AuthService } from '../../../shared/services/auth.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import {of, throwError} from 'rxjs';
import { User } from '../../../shared/models/user.model';
import { NoopAnimationsModule} from "@angular/platform-browser/animations";

describe('AddPostComponent', () => {
  let component: AddPostComponent;
  let fixture: ComponentFixture<AddPostComponent>;
  let mockPostService: jasmine.SpyObj<PostService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockPostService = jasmine.createSpyObj('PostService', ['createPost']);
    mockAuthService = jasmine.createSpyObj('AuthService', [], { currentUser: { username: 'testUser' } as User });
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [AddPostComponent, ReactiveFormsModule, NoopAnimationsModule],
      providers: [
        { provide: PostService, useValue: mockPostService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddPostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with default values', () => {
    const form = component.postForm;
    expect(form).toBeTruthy();
    expect(form.get('title')?.value).toBe('');
    expect(form.get('content')?.value).toBe('');
    expect(form.get('author')?.value).toBe('testUser');
    expect(form.get('isConcept')?.value).toBe(true); // Default to true
    expect(form.get('isConcept')?.disabled).toBeTrue(); // Field is disabled
  });

  it('should validate the form fields correctly', () => {
    const form = component.postForm;

    form.get('title')?.setValue('');
    form.get('content')?.setValue('');
    expect(form.valid).toBeFalse();

    form.get('title')?.setValue('Test Title');
    form.get('content')?.setValue('Test Content');
    expect(form.valid).toBeTrue();
  });

  it('should disable the author field', () => {
    const authorField = component.postForm.get('author');
    expect(authorField?.disabled).toBeTrue();
  });

  it('should submit a new post when the form is valid', () => {
    const form = component.postForm;
    form.get('title')?.setValue('Test Title');
    form.get('content')?.setValue('Test Content');
    form.get('isConcept')?.setValue(true);

    const mockResponse = { id: 1, ...form.value };
    mockPostService.createPost.and.returnValue(of(mockResponse));

    component.submitForm();

    expect(mockPostService.createPost).toHaveBeenCalledWith(jasmine.objectContaining({
      title: 'Test Title',
      content: 'Test Content',
      author: 'testUser',
      status: 'CONCEPT',
    }));
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/posts', 1]);
  });

  it('should not submit the form if it is invalid', () => {
    spyOn(console, 'log');
    component.submitForm();
    expect(mockPostService.createPost).not.toHaveBeenCalled();
    expect(console.log).not.toHaveBeenCalled();
  });

  it('should display an alert if the post creation fails', () => {
    // Arrange: Spy on the alert function
    spyOn(window, 'alert');

    // Arrange: Make the form valid
    component.postForm.get('title')?.setValue('Test Title');
    component.postForm.get('content')?.setValue('Test Content');
    component.postForm.get('isConcept')?.setValue(true);

    // Arrange: Simulate an error from the service
    const mockError = new Error('Failed to create post');
    mockPostService.createPost.and.returnValue(throwError(() => mockError));

    // Act: Call the submitForm method
    component.submitForm();

    // Assert: Check that the alert function was called with the correct message
    expect(window.alert).toHaveBeenCalledWith('Error: ' + mockError.message);

    // Assert: Ensure the createPost method was called with the expected data
    expect(mockPostService.createPost).toHaveBeenCalledWith(jasmine.objectContaining({
      title: 'Test Title',
      content: 'Test Content',
      author: 'testUser',
      status: 'CONCEPT',
    }));
  });
});
