import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ExcelService {
  /**
   * Parse an Excel/CSV file using SheetJS (xlsx).
   * Returns an array of row objects keyed by header names.
   */
  async parseFile(file: File): Promise<Record<string, unknown>[]> {
    const XLSX = await import('xlsx');
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const data = new Uint8Array(e.target!.result as ArrayBuffer);
          const workbook = XLSX.read(data, { type: 'array' });
          const sheetName = workbook.SheetNames[0];
          const sheet = workbook.Sheets[sheetName];
          const rows = XLSX.utils.sheet_to_json<Record<string, unknown>>(sheet);
          resolve(rows);
        } catch (err) {
          reject(err);
        }
      };
      reader.onerror = () => reject(reader.error);
      reader.readAsArrayBuffer(file);
    });
  }

  /**
   * Export data to Excel file
   */
  async exportToExcel(data: Record<string, unknown>[], fileName: string): Promise<void> {
    const XLSX = await import('xlsx');
    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
    XLSX.writeFile(workbook, `${fileName}.xlsx`);
  }

  /**
   * Download a sample template Excel file for sales import
   */
  async downloadSaleTemplate(): Promise<void> {
    const sampleData = [
      {
        productId: 1,
        quantity: 10,
        unitPrice: 25.00,
        lineTotal: 250.00
      },
      {
        productId: 2,
        quantity: 5,
        unitPrice: 15.50,
        lineTotal: 77.50
      }
    ];
    await this.exportToExcel(sampleData as Record<string, unknown>[], 'sales_import_template');
  }
}
