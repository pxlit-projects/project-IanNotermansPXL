import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RejectDialogComponent } from './reject-dialog.component';
import { MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('RejectDialogComponent', () => {
  let component: RejectDialogComponent;
  let fixture: ComponentFixture<RejectDialogComponent>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<RejectDialogComponent>>;

  beforeEach(async () => {
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [
        RejectDialogComponent,
        FormsModule,
        NoopAnimationsModule, // Include NoopAnimationsModule for testing
      ],
      providers: [{ provide: MatDialogRef, useValue: dialogRefSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(RejectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should bind textarea value to the comment property', async () => {
    const textarea = fixture.debugElement.query(By.css('textarea')).nativeElement;

    textarea.value = 'Test comment';
    textarea.dispatchEvent(new Event('input')); // Trigger input event for ngModel binding
    fixture.detectChanges();

    expect(component.comment).toBe('Test comment');
  });

  it('should close the dialog with no data when Cancel is clicked', () => {
    const cancelButton = fixture.debugElement.query(By.css('button[aria-label="Cancel action"]')).nativeElement;
    cancelButton.click();

    expect(dialogRefSpy.close).toHaveBeenCalledWith(); // Expect no data to be sent
  });

  it('should close the dialog with comment when Submit is clicked', () => {
    component.comment = 'This is a rejection reason';
    fixture.detectChanges();

    const submitButton = fixture.debugElement.query(By.css('button[aria-label="Submit action"]')).nativeElement;
    submitButton.click();

    expect(dialogRefSpy.close).toHaveBeenCalledWith('This is a rejection reason'); // Expect the comment to be sent
  });
});
