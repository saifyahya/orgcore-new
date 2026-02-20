import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatStepperModule } from '@angular/material/stepper';
import { SaleService } from '../../../core/services/sale.service';
import { ExcelService } from '../../../core/services/excel.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Branch } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

interface DialogData { branches: Branch[]; }

@Component({ selector: 'app-sale-import-dialog', standalone: true, templateUrl: './sale-import-dialog.component.html', styleUrls: ['./sale-import-dialog.component.scss'],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule, MatIconModule, MatTableModule, MatStepperModule, TranslatePipe]
})
export class SaleImportDialogComponent {
  configForm: FormGroup; selectedFile: File | null = null;
  previewRows: Record<string, unknown>[] = []; previewColumns: string[] = [];
  parsing = false; importing = false;

  constructor(private fb: FormBuilder, private saleService: SaleService, private excelService: ExcelService, private notification: NotificationService, private ts: TranslationService, public dialogRef: MatDialogRef<SaleImportDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DialogData) {
    this.configForm = this.fb.group({ branchId: [null, Validators.required] });
  }

  onFileSelected(event: Event): void { this.selectedFile = (event.target as HTMLInputElement).files?.[0] || null; }

  async parseFile(stepper: any): Promise<void> {
    if (!this.selectedFile) return; stepper.next(); this.parsing = true;
    try { this.previewRows = await this.excelService.parseFile(this.selectedFile); this.previewColumns = this.previewRows.length > 0 ? Object.keys(this.previewRows[0]) : []; }
    catch (e) { this.notification.error(this.ts.t('SALES.IMPORT.PARSE_ERROR')); }
    finally { this.parsing = false; }
  }

  importFile(): void {
    if (!this.selectedFile || !this.configForm.value.branchId) return; this.importing = true;
    this.saleService.importFromExcel(this.selectedFile, this.configForm.value.branchId).subscribe({
      next: (result) => { this.notification.success(this.ts.t('SALES.IMPORT.IMPORT_SUCCESS', { count: result.length })); this.dialogRef.close(true); },
      error: () => { this.importing = false; }
    });
  }

  async downloadTemplate(): Promise<void> { await this.excelService.downloadSaleTemplate(); }
}
