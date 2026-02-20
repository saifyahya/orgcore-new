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

@Component({ selector: 'app-sale-detail', standalone: true, templateUrl: './sale-detail.component.html', styleUrls: ['./sale-detail.component.scss'],
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatDividerModule, MatTableModule, TranslatePipe]
})
export class SaleDetailComponent implements OnInit {
  sale: Sale | null = null; loading = true;
  itemColumns = ['product', 'quantity', 'unitPrice', 'lineTotal'];
  get subtotal(): number { return (this.sale?.items || []).reduce((sum, i) => sum + (i.lineTotal || 0), 0); }

  constructor(private saleService: SaleService, private route: ActivatedRoute) {}
  ngOnInit(): void { this.saleService.getById(Number(this.route.snapshot.params['id'])).subscribe({ next: (sale) => { this.sale = sale; this.loading = false; }, error: () => { this.loading = false; } }); }
}
