import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule } from '@angular/material/table';
import { SaleService } from '../../../core/services/sale.service';
import { Sale } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { LocalizedCurrencyPipe } from '../../../shared/pipes/localized-currency.pipe';

@Component({ selector: 'app-sale-detail', standalone: true, templateUrl: './sale-detail.component.html', styleUrls: ['./sale-detail.component.scss'],
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatDividerModule, MatTableModule, TranslatePipe, LocalizedCurrencyPipe]
})
export class SaleDetailComponent implements OnInit {
  sale: Sale | null = null; loading = true; exporting = false;
  itemColumns = ['code', 'product', 'quantity', 'unitPrice', 'lineTotal'];
  get subtotal(): number { return (this.sale?.items || []).reduce((sum, i) => sum + ((i.quantity || 0) * (i.unitPrice || 0)), 0); }

  constructor(private saleService: SaleService, private route: ActivatedRoute) {}
  ngOnInit(): void { this.saleService.getById(Number(this.route.snapshot.params['id'])).subscribe({ next: (sale) => { this.sale = sale; this.loading = false; }, error: () => { this.loading = false; } }); }
  
  exportToPdf(): void {
    if (!this.sale?.id || this.exporting) return;
    this.exporting = true;
    this.saleService.exportToPdf(this.sale.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `sale-${this.sale?.id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.exporting = false;
      },
      error: () => {
        this.exporting = false;
      }
    });
  }
}
