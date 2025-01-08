import { TestBed } from '@angular/core/testing';
import { CanDeactivateFn } from '@angular/router';

import { routeGuard } from './route.guard';

describe('routeGuard', () => {
  const executeGuard: CanDeactivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => routeGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
