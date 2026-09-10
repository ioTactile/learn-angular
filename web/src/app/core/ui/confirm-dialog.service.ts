import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { ConfirmDialogData } from './confirm-dialog.models';

/**
 * Dialog de confirmation générique.
 * Le service ne connaît pas l’API : l’appelant décide quoi faire si `true`.
 */
@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private readonly dialog = inject(MatDialog);

  confirm(data: ConfirmDialogData): Observable<boolean> {
    return this.dialog
      .open(ConfirmDialogComponent, {
        data,
        width: '24rem',
        autoFocus: 'dialog',
      })
      .afterClosed()
      .pipe(map((result) => result === true));
  }
}
