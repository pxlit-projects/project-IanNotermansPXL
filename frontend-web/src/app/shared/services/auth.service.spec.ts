import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { User } from '../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: Router, useValue: mockRouter }, // Mock Router
      ],
    });

    service = TestBed.inject(AuthService); // Use TestBed to create the service
  });

  afterEach(() => {
    sessionStorage.clear(); // Clear session storage after each test
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should set the current user and update sessionStorage', () => {
      const username = 'testUser';
      const role = 'admin';

      service.login(username, role);

      const storedUser = JSON.parse(sessionStorage.getItem('currentUser') || '{}');
      expect(storedUser).toEqual({ username, role });
      expect(service.currentUser).toEqual({ username, role });
    });

    it('should update the currentUserSubject observable', (done) => {
      const username = 'testUser';
      const role = 'user';

      service.login(username, role);

      service.currentUser$.subscribe((user) => {
        expect(user).toEqual({ username, role });
        done();
      });
    });
  });

  describe('logout', () => {
    it('should clear the current user and sessionStorage, then navigate to /login', () => {
      service.login('testUser', 'admin'); // Log in first
      service.logout();

      expect(sessionStorage.getItem('currentUser')).toBeNull();
      expect(service.currentUser).toBeNull();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should emit null through the currentUser$ observable', (done) => {
      service.logout();

      service.currentUser$.subscribe((user) => {
        expect(user).toBeNull();
        done();
      });
    });
  });

  describe('get currentUser', () => {
    it('should return null if no user is logged in', () => {
      sessionStorage.removeItem('currentUser'); // Ensure sessionStorage is empty

      // Reinitialize the service to ensure it reads sessionStorage
      service = TestBed.inject(AuthService);

      expect(service.currentUser).toBeNull(); // Validate currentUser
    });
  });

});
