import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatStepperModule } from '@angular/material/stepper';
import { InventoryService } from '../../../core/services/inventory.service';
import { ExcelService } from '../../../core/services/excel.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-inventory-import-dialog',
  standalone: true,
  templateUrl: './inventory-import-dialog.component.html',
  styleUrls: ['./inventory-import-dialog.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatTableModule,
    MatStepperModule,
    TranslatePipe
  ]
})
export class InventoryImportDialogComponent {
  configForm: FormGroup;
  selectedFile: File | null = null;
  previewRows: Record<string, unknown>[] = [];
  previewColumns: string[] = [];
  parsing = false;
  importing = false;

  constructor(
    private fb: FormBuilder,
    private inventoryService: InventoryService,
    private excelService: ExcelService,
    private notification: NotificationService,
    private ts: TranslationService,
    public dialogRef: MatDialogRef<InventoryImportDialogComponent>
  ) {
    this.configForm = this.fb.group({});
  }

  onFileSelected(event: Event): void {
    this.selectedFile = (event.target as HTMLInputElement).files?.[0] || null;
  }

  async parseFile(stepper: any): Promise<void> {
    if (!this.selectedFile) return;
    stepper.next();
    this.parsing = true;
    try {
      this.previewRows = await this.excelService.parseFile(this.selectedFile);
      this.previewColumns = this.previewRows.length > 0 ? Object.keys(this.previewRows[0]) : [];
    } catch (e) {
      this.notification.error(this.ts.t('INVENTORY.IMPORT.PARSE_ERROR'));
    } finally {
      this.parsing = false;
    }
  }

  importFile(): void {
    if (!this.selectedFile) return;
    this.importing = true;
    this.inventoryService.importFromExcel(this.selectedFile).subscribe({
      next: (result: any) => {
        const count = result.message ? result.message.match(/\d+/)?.[0] || this.previewRows.length : this.previewRows.length;
        this.notification.success(this.ts.t('INVENTORY.IMPORT.IMPORT_SUCCESS', { count }));
        this.dialogRef.close(true);
      },
      error: () => {
        this.importing = false;
      }
    });
  }

  async downloadTemplate(): Promise<void> {
    await this.excelService.downloadInventoryTemplate();
  }
}
