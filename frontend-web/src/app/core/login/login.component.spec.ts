import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../shared/services/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { By } from '@angular/platform-browser';
import {NoopAnimationsModule} from "@angular/platform-browser/animations";
import { of } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Mock services
    authServiceMock = jasmine.createSpyObj('AuthService', ['login']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        FormsModule,
        MatSelectModule,
        MatInputModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call AuthService.login and navigate to home on form submit', () => {
    component.username = 'testUser';
    component.role = 'editor';

    const form = fixture.debugElement.query(By.css('form'));
    form.triggerEventHandler('ngSubmit', null);

    expect(authServiceMock.login).toHaveBeenCalledWith('testUser', 'editor');
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });


  it('should enable the login button when the form is valid', () => {
    // Arrange: Find the login button and form fields
    const loginButton = fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement;

    const usernameInput = fixture.debugElement.query(By.css('input[name="username"]')).nativeElement;
    const roleSelect = fixture.debugElement.query(By.css('mat-select[name="role"]')).nativeElement;

    // Act: Simulate user input
    usernameInput.value = 'testUser';
    usernameInput.dispatchEvent(new Event('input'));

    roleSelect.value = 'user';
    roleSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges(); // Trigger Angular change detection

    // Assert: Check if the button is enabled
    expect(loginButton.disabled).toBeFalse();
  });


  it('should render the correct form fields and button', () => {
    const usernameInput = fixture.debugElement.query(By.css('input[name="username"]')).nativeElement;
    const roleSelect = fixture.debugElement.query(By.css('mat-select')).nativeElement;
    const button = fixture.debugElement.query(By.css('button')).nativeElement;

    expect(usernameInput).toBeTruthy();
    expect(roleSelect).toBeTruthy();
    expect(button).toBeTruthy();
  });
});
