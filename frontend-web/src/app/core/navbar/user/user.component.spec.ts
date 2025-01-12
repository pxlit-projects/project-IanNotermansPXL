import { TestBed } from '@angular/core/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { UserComponent } from './user.component';
import { AuthService } from '../../../shared/services/auth.service';

describe('UserComponent', () => {
  let component: UserComponent;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['logout']);

    await TestBed.configureTestingModule({
      imports: [
        UserComponent,
        MatToolbarModule,
        MatButtonModule,
        RouterTestingModule.withRoutes([]), // Use RouterTestingModule for minimal routing setup
      ],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    const fixture = TestBed.createComponent(UserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call logout on the AuthService when logout is triggered', () => {
    component.logout();
    expect(authServiceMock.logout).toHaveBeenCalled();
  });
});
