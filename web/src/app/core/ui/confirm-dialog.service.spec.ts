import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ConfirmDialogService } from './confirm-dialog.service';

describe('ConfirmDialogService', () => {
  it('expose true/false selon afterClosed', () => {
    const open = vi.fn().mockReturnValue({ afterClosed: () => of(true) });

    TestBed.configureTestingModule({
      providers: [{ provide: MatDialog, useValue: { open } }],
    });

    const service = TestBed.inject(ConfirmDialogService);
    let confirmed = false;
    service.confirm({ title: 'Supprimer ?', message: 'Irréversible' }).subscribe((v) => {
      confirmed = v;
    });

    expect(open).toHaveBeenCalled();
    expect(confirmed).toBe(true);
  });
});
