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
   * Format: Flattened structure where each row is a sale item,
   * and sale header fields are repeated. Backend should group by externalRef.
   */
  async downloadSaleTemplate(): Promise<void> {
    const sampleData = [
      {
        branchId: 1,
        totalAmount: 327.50,
        discountAmount: 0,
        taxAmount: 27.50,
        paymentMethod: 'CASH',
        channel: 'POS',
        externalRef: 'POS-001',
        productId: 1,
        quantity: 10,
        unitPrice: 25.00,
        lineTotal: 250.00
      },
      {
        branchId: 1,
        totalAmount: 327.50,
        discountAmount: 0,
        taxAmount: 27.50,
        paymentMethod: 'CASH',
        channel: 'POS',
        externalRef: 'POS-001',
        productId: 2,
        quantity: 5,
        unitPrice: 15.50,
        lineTotal: 77.50
      },
      {
        branchId: 1,
        totalAmount: 450.00,
        discountAmount: 50.00,
        taxAmount: 0,
        paymentMethod: 'CARD',
        channel: 'ONLINE',
        externalRef: 'WEB-002',
        productId: 3,
        quantity: 20,
        unitPrice: 22.50,
        lineTotal: 450.00
      }
    ];
    await this.exportToExcel(sampleData as Record<string, unknown>[], 'sales_import_template');
  }

  /**
   * Download a sample template Excel file for inventory import
   */
  async downloadInventoryTemplate(): Promise<void> {
    const sampleData = [
      {
        branchId: 1,
        productCode: 1,
        quantity: 100,
        note: 'Initial stock',
        referenceType: 'IMPORT'
      },
      {
        branchId: 1,
        productCode: 2,
        quantity: 50,
        note: 'Inventory adjustment',
        referenceType: 'IMPORT'
      }
    ];
    await this.exportToExcel(sampleData as Record<string, unknown>[], 'inventory_import_template');
  }

  /**
   * Download a sample template Excel file for product import
   */
  async downloadProductTemplate(): Promise<void> {
    const sampleData = [
      {
        name: 'Sample Product 1',
        description: 'Product description',
        categoryId: 1,
        price: 25.00,
        isActive: 1,
        code: 'PROD001'
      },
      {
        name: 'Sample Product 2',
        description: 'Another product',
        categoryId: 2,
        price: 50.00,
        isActive: 1,
        code: 'PROD002'
      }
    ];
    await this.exportToExcel(sampleData as Record<string, unknown>[], 'product_import_template');
  }
}
