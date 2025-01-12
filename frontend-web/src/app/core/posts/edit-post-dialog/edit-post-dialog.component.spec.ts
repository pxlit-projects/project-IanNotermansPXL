import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { EditPostDialogComponent } from './edit-post-dialog.component';
import { By } from '@angular/platform-browser';
import {NoopAnimationsModule} from "@angular/platform-browser/animations";

describe('EditPostDialogComponent', () => {
  let component: EditPostDialogComponent;
  let fixture: ComponentFixture<EditPostDialogComponent>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<EditPostDialogComponent>>;

  beforeEach(async () => {
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [
        EditPostDialogComponent, // Add the standalone component here
        ReactiveFormsModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: { title: 'Sample Post', content: 'Sample Content' } }, // Mock data
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditPostDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should populate the form with data passed through MAT_DIALOG_DATA', () => {
    expect(component.editPostForm.value).toEqual({
      title: 'Sample Post',
      content: 'Sample Content',
    });
  });
});
