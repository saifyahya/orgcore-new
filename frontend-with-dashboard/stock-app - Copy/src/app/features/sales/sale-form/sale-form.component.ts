import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { forkJoin } from 'rxjs';
import { SaleService } from '../../../core/services/sale.service';
import { BranchService } from '../../../core/services/branch.service';
import { ProductService } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslationService } from '../../../core/services/translation.service';
import { Branch, Product, PaymentMethod, SaleChannel, SaleRequest } from '../../../core/models';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-sale-form', standalone: true, templateUrl: './sale-form.component.html', styleUrls: ['./sale-form.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatInputModule, MatFormFieldModule, MatSelectModule, MatProgressSpinnerModule, MatDividerModule, TranslatePipe]
})
export class SaleFormComponent implements OnInit {
  form!: FormGroup; saving = false; loading = true; isEdit = false; saleId: number | null = null;
  branches: Branch[] = []; products: Product[] = [];
  paymentMethods = Object.values(PaymentMethod); channels = Object.values(SaleChannel);

  get items(): FormArray { return this.form.get('items') as FormArray; }
  get subtotal(): number { return this.items.controls.reduce((sum, c) => sum + (c.get('lineTotal')?.value || 0), 0); }
  get grandTotal(): number { return this.subtotal - (this.form.get('discountAmount')?.value || 0) + (this.form.get('taxAmount')?.value || 0); }

  constructor(private fb: FormBuilder, private saleService: SaleService, private branchService: BranchService, private productService: ProductService, private notification: NotificationService, private ts: TranslationService, private route: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
    this.saleId = this.route.snapshot.params['id'] ? Number(this.route.snapshot.params['id']) : null;
    this.isEdit = !!this.saleId && this.router.url.includes('/edit');
    this.form = this.fb.group({ branchId: [null, Validators.required], paymentMethod: [PaymentMethod.CASH], channel: [SaleChannel.MANUAL], externalRef: [''], discountAmount: [0], taxAmount: [0], items: this.fb.array([]) });
    forkJoin({ branches: this.branchService.getAll(), products: this.productService.getAll() }).subscribe({
      next: ({ branches, products }) => { this.branches = branches.content; this.products = products.content; if (this.isEdit && this.saleId) { this.loadSale(); } else { this.addItem(); this.loading = false; } },
      error: () => { this.loading = false; }
    });
  }

  loadSale(): void {
    this.saleService.getById(this.saleId!).subscribe({
      next: (sale) => {
        this.form.patchValue({ branchId: sale.branch?.id, paymentMethod: sale.paymentMethod, channel: sale.channel, externalRef: sale.externalRef || '', discountAmount: sale.discountAmount || 0, taxAmount: sale.taxAmount || 0 });
        sale.items?.forEach(item => this.items.push(this.buildItem(item.product?.id, item.quantity, item.unitPrice, item.lineTotal)));
        this.loading = false;
      }, error: () => { this.loading = false; }
    });
  }

  buildItem(productId?: number, quantity = 1, unitPrice = 0, lineTotal = 0) {
    return this.fb.group({ productId: [productId || null, Validators.required], quantity: [quantity, [Validators.required, Validators.min(1)]], unitPrice: [unitPrice], lineTotal: [lineTotal] });
  }

  addItem(): void { this.items.push(this.buildItem()); }
  removeItem(index: number): void { this.items.removeAt(index); }

  onProductChange(index: number): void {
    const control = this.items.at(index);
    const product = this.products.find(p => p.id === control.get('productId')?.value);
    if (product?.price) { control.get('unitPrice')?.setValue(product.price); this.calculateLineTotal(index); }
  }

  calculateLineTotal(index: number): void {
    const control = this.items.at(index);
    control.get('lineTotal')?.setValue((control.get('quantity')?.value || 0) * (control.get('unitPrice')?.value || 0));
  }

  save(): void {
    if (this.form.invalid || this.items.length === 0) return; this.saving = true;
    const fv = this.form.value;
    const payload: SaleRequest = { branchId: fv.branchId, paymentMethod: fv.paymentMethod, channel: fv.channel, externalRef: fv.externalRef || undefined, discountAmount: fv.discountAmount || 0, taxAmount: fv.taxAmount || 0, totalAmount: this.grandTotal, items: this.items.value };
    const req = this.isEdit ? this.saleService.update(this.saleId!, payload) : this.saleService.create(payload);
    req.subscribe({ next: (sale) => { this.notification.success(this.ts.t(this.isEdit ? 'SALES.UPDATED' : 'SALES.CREATED')); this.router.navigate(['/sales', sale.id]); }, error: () => { this.saving = false; } });
  }
}
