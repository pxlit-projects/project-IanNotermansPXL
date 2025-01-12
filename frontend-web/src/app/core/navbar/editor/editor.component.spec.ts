import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideLocationMocks } from '@angular/common/testing';
import { RouterTestingHarness } from '@angular/router/testing';
import { EditorComponent } from './editor.component';
import { AuthService } from '../../../shared/services/auth.service';
import { By } from '@angular/platform-browser';

describe('EditorComponent', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    // Mock AuthService
    authServiceMock = jasmine.createSpyObj('AuthService', ['logout']);
    authServiceMock.logout.and.returnValue(undefined);

    await TestBed.configureTestingModule({
      imports: [EditorComponent], // Import the standalone component
      providers: [
        provideRouter([
          { path: 'posts', component: EditorComponent },
          { path: 'add-post', component: EditorComponent },
          { path: 'concepts', component: EditorComponent },
        ]),
        provideLocationMocks(), // Mock location services
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should create the component', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/posts'); // Navigate to ensure route is activated
    const routeDebugElement = harness.routeDebugElement;
    expect(routeDebugElement).toBeTruthy();
    const component = routeDebugElement?.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should call logout method when logout button is clicked', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/posts'); // Navigate to activate the route
    const routeDebugElement = harness.routeDebugElement;
    expect(routeDebugElement).toBeTruthy();
    const logoutButton = routeDebugElement?.query(By.css('#logout-button'));
    expect(logoutButton).toBeTruthy();
    logoutButton!.nativeElement.click(); // Simulate button click
    expect(authServiceMock.logout).toHaveBeenCalled();
  });
});
