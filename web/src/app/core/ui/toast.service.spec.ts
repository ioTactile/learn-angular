import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let toast: ToastService;
  let open: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    open = vi.fn();
    TestBed.configureTestingModule({
      providers: [{ provide: MatSnackBar, useValue: { open } }],
    });
    toast = TestBed.inject(ToastService);
  });

  it('success ouvre un snackbar court sans action', () => {
    toast.success('OK');

    expect(open).toHaveBeenCalledWith(
      'OK',
      undefined,
      expect.objectContaining({
        duration: 3000,
        panelClass: ['toast-success'],
      }),
    );
  });

  it('error ouvre un snackbar avec action Fermer', () => {
    toast.error('Échec');

    expect(open).toHaveBeenCalledWith(
      'Échec',
      'Fermer',
      expect.objectContaining({
        duration: 5000,
        panelClass: ['toast-error'],
      }),
    );
  });
});
