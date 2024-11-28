import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConceptPostListComponent } from './concept-post-list.component';

describe('ConceptPostListComponent', () => {
  let component: ConceptPostListComponent;
  let fixture: ComponentFixture<ConceptPostListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConceptPostListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConceptPostListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
