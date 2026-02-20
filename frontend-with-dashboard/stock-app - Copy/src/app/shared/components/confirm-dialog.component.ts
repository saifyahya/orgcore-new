import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '../pipes/translate.pipe';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  color?: 'warn' | 'primary' | 'accent';
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, TranslatePipe],
  template: `
    <div class="confirm-dialog">
      <h2 mat-dialog-title class="dialog-title">
        <mat-icon class="warn-icon">warning</mat-icon>
        {{ data.title }}
      </h2>
      <mat-dialog-content>
        <p>{{ data.message }}</p>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-stroked-button [mat-dialog-close]="false">
          {{ data.cancelText || ('CONFIRM.CANCEL_BTN' | translate) }}
        </button>
        <button mat-flat-button [color]="data.color || 'warn'" [mat-dialog-close]="true">
          {{ data.confirmText || ('CONFIRM.CONFIRM_BTN' | translate) }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`.confirm-dialog { min-width: 320px; } .dialog-title { display: flex; align-items: center; gap: 8px; } .warn-icon { color: #ef4444; } mat-dialog-content p { color: var(--text-secondary); margin: 0; } mat-dialog-actions { gap: 8px; }`]
})
export class ConfirmDialogComponent {
  constructor(public dialogRef: MatDialogRef<ConfirmDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}
}
