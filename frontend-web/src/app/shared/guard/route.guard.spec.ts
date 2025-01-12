import { TestBed } from '@angular/core/testing';
import { routeGuard } from './route.guard';
import { AddPostComponent } from '../../core/posts/add-post/add-post.component';
import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { PostService } from '../services/post.service';
import { AuthService } from '../services/auth.service';
import { of } from 'rxjs';

describe('routeGuard', () => {
  let component: AddPostComponent;
  let mockConfirm: jasmine.Spy;
  let mockRoute: ActivatedRouteSnapshot;
  let mockState: RouterStateSnapshot;
  let mockPostService: jasmine.SpyObj<PostService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockPostService = jasmine.createSpyObj('PostService', ['createPost']);
    mockAuthService = jasmine.createSpyObj('AuthService', [], { currentUser: { username: 'mockUser' } });
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, AddPostComponent],
      providers: [
        { provide: PostService, useValue: mockPostService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
        FormBuilder,
      ],
    });

    // Create the component using Angular's DI system
    const fixture = TestBed.createComponent(AddPostComponent);
    component = fixture.componentInstance;

    // Initialize the form and mock dependencies
    component.postForm = new FormGroup({
      title: new FormControl(''),
      content: new FormControl(''),
      author: new FormControl(''),
      isConcept: new FormControl(false),
    });
    component.isFormSubmitted = false;

    // Mock window.confirm
    mockConfirm = spyOn(window, 'confirm').and.returnValue(true);

    // Mock ActivatedRouteSnapshot and RouterStateSnapshot
    mockRoute = {} as ActivatedRouteSnapshot;
    mockState = {} as RouterStateSnapshot;
  });

  it('should allow navigation if the form is not dirty and not submitted', () => {
    component.postForm.markAsPristine();
    component.isFormSubmitted = false;

    const result = routeGuard(component, mockRoute, mockState, mockState);
    expect(result).toBe(true);
  });

  it('should allow navigation if the form is dirty but submitted', () => {
    component.postForm.get('title')?.setValue('Changed Title');
    component.postForm.markAsDirty();
    component.isFormSubmitted = true;

    const result = routeGuard(component, mockRoute, mockState, mockState);
    expect(result).toBe(true);
  });

  it('should prompt the user if the form is dirty and not submitted', () => {
    component.postForm.get('title')?.setValue('New Title');
    component.postForm.markAsDirty();
    component.isFormSubmitted = false;

    mockConfirm.and.returnValue(true);
    const result = routeGuard(component, mockRoute, mockState, mockState);

    expect(mockConfirm).toHaveBeenCalledWith('Do you really want to leave?');
    expect(result).toBe(true);
  });

  it('should prevent navigation if the user cancels the prompt', () => {
    component.postForm.get('title')?.setValue('New Title');
    component.postForm.markAsDirty();
    component.isFormSubmitted = false;

    mockConfirm.and.returnValue(false);
    const result = routeGuard(component, mockRoute, mockState, mockState);

    expect(mockConfirm).toHaveBeenCalledWith('Do you really want to leave?');
    expect(result).toBe(false);
  });
});
