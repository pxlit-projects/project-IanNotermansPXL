import { TestBed } from '@angular/core/testing';
import { AuthGuard } from './auth.guard';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    // Create spy objects for AuthService and Router
    mockAuthService = jasmine.createSpyObj('AuthService', [], { currentUser: null });
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });

    guard = TestBed.inject(AuthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access if user is authenticated and has the correct role', () => {
    Object.defineProperty(mockAuthService, 'currentUser', {
      get: () => ({ username: 'adminUser', role: 'admin' }),
    });

    const route = { data: { role: 'admin' } } as any; // Route requiring 'admin' role
    const state = {} as any; // Mock RouterStateSnapshot

    const result = guard.canActivate(route, state);
    expect(result).toBeTrue(); // The guard should allow access
  });

  it('should deny access and navigate to login if user is not authenticated', () => {
    Object.defineProperty(mockAuthService, 'currentUser', {
      get: () => null, // User is not authenticated
    });

    const route = {} as any; // No role data in this case
    const state = {} as any;

    const result = guard.canActivate(route, state);
    expect(result).toBeFalse(); // The guard should deny access
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']); // Should navigate to login
  });

  it('should deny access and navigate to home if user does not have the required role', () => {
    Object.defineProperty(mockAuthService, 'currentUser', {
      get: () => ({ username: 'user123', role: 'user' }), // User has an incorrect role
    });

    const route = { data: { role: 'admin' } } as any; // Route requiring 'admin' role
    const state = {} as any;

    const result = guard.canActivate(route, state);
    expect(result).toBeFalse(); // The guard should deny access
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']); // Should navigate to home
  });

  it('should allow access if no role is specified in route data', () => {
    Object.defineProperty(mockAuthService, 'currentUser', {
      get: () => ({ username: 'user123', role: 'user' }), // User is authenticated
    });

    const route = { data: {} } as any; // Ensure data is defined
    const state = {} as any;

    const result = guard.canActivate(route, state);
    expect(result).toBeTrue(); // The guard should allow access
  });
});
