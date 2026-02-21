// ---- Enums ----
export enum PaymentMethod {
  CASH = 'CASH',
  CARD = 'CARD',
  ONLINE = 'ONLINE',
  OTHER = 'OTHER'
}

export enum SaleChannel {
  MANUAL = 'MANUAL',
  IMPORT = 'IMPORT',
  POS = 'POS',
  API = 'API'
}

export enum StockMovementType {
  IN = 'IN',
  OUT = 'OUT',
  ADJUSTMENT = 'ADJUSTMENT',
  TRANSFER = 'TRANSFER'
}

export enum StockMovementReason {
  PURCHASE = 'PURCHASE',
  SALE = 'SALE',
  RETURN = 'RETURN',
  DAMAGE = 'DAMAGE',
  LOSS = 'LOSS',
  CORRECTION = 'CORRECTION',
  TRANSFER_IN = 'TRANSFER_IN',
  TRANSFER_OUT = 'TRANSFER_OUT'
}

export enum ReferenceType {
  SALE = 'SALE',
  PURCHASE = 'PURCHASE',
  TRANSFER = 'TRANSFER',
  MANUAL = 'MANUAL'
}

// ---- Base ----
export interface BaseEntity {
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

// ---- Category ----
export interface Category extends BaseEntity {
  id?: number;
  name: string;
  description?: string;
  isActive?: number;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  isActive?: number;
}

// ---- Branch ----
export interface Branch extends BaseEntity {
  id?: number;
  branchName: string;
  address?: string;
  isActive?: number;
}

export interface BranchRequest {
  branchName: string;
  address?: string;
  isActive?: number;
}

// ---- Product ----
export interface Product extends BaseEntity {
  id?: number;
  name: string;
  description?: string;
  categoryDto?: Category;
  categoryId?: number;
  image?: string;
  price?: number;
  discount?: number;
  isActive?: number;
  rate?: number;
}

export interface ProductRequest {
  name: string;
  description?: string;
  categoryId?: number;
  image?: string;
  price?: number;
  discount?: number;
  isActive?: number;
  rate?: number;
}

// ---- Inventory ----
export interface Inventory extends BaseEntity {
  id?: number;
  branch?: Branch;
  branchId?: number;
  product?: Product;
  productId?: number;
  quantity?: number;
}

export interface InventoryRequest {
  branchId: number;
  productId: number;
  quantity: number;
}

// ---- Sale Item ----
export interface SaleItem extends BaseEntity {
  id?: number;
  product?: Product;
  productId?: number;
  quantity: number;
  unitPrice?: number;
  lineTotal?: number;
}

export interface SaleItemRequest {
  productId: number;
  quantity: number;
  unitPrice?: number;
  lineTotal?: number;
}

// ---- Sale ----
export interface Sale extends BaseEntity {
  id?: number;
  branch?: Branch;
  branchId?: number;
  totalAmount?: number;
  discountAmount?: number;
  taxAmount?: number;
  paymentMethod?: PaymentMethod;
  channel?: SaleChannel;
  externalRef?: string;
  items?: SaleItem[];
}

export interface SaleRequest {
  branchId: number;
  totalAmount?: number;
  discountAmount?: number;
  taxAmount?: number;
  paymentMethod?: PaymentMethod;
  channel?: SaleChannel;
  externalRef?: string;
  items: SaleItemRequest[];
}

// ---- Stock Movement ----
export interface StockMovement extends BaseEntity {
  id?: number;
  branch: Branch;
  product: Product;
  type?: StockMovementType;
  reason?: StockMovementReason;
  quantity?: number;
  unitCost?: number;
  refType?: ReferenceType;
  refId?: string;
  note?: string;
}

export interface StockMovementRequest {
  branchId: number;
  productId: number;
  type: StockMovementType;
  reason: StockMovementReason;
  quantity: number;
  unitCost?: number;
  refType?: ReferenceType;
  refId?: string;
  note?: string;
}

// ---- API Response Wrapper ----
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status?: number;
}


export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export type Page<T> = PageResponse<T>;

// ─── Dashboard / Analytics Models ────────────────────────────────────────────

export interface DashboardSummary {
  totalSalesAmount: number;
  totalDiscountAmount: number;
  totalTaxAmount: number;
  finalAmount: number;
  totalOrders: number;           // عدد الفواتير - number of invoices
  avgOrderValue: number;
  avgSalesPerProduct: number;
  totalProducts: number;          // عدد المبيعات - number of sales
  totalCategories: number;
  activeBranches: number;
}

export interface MonthlySeries {
  year: number;
  month: number;
  monthLabel: string;             // "JANUARY", "FEBRUARY", etc. (from backend)
  totalAmount: number;
  orderCount: number;             // عدد الفواتير - number of invoices
  individualCount: number;        // عدد المبيعات - number of sales
}

export interface WeeklyDaySeries {
  saleDate: string;               // LocalDate from backend
  dayOfWeek: number;              // 1=Monday ... 7=Sunday (ISO)
  dayLabel: string;               // "MONDAY", "TUESDAY", etc. (from backend)
  totalAmount: number;
  orderCount: number;             // عدد الفواتير - number of invoices
  individualCount: number;        // عدد المبيعات - number of sales
}

export interface CategorySales {
  categoryId: number;
  categoryName: string;
  categoryImage?: string;
  totalQuantity: number;
  totalRevenue: number;
  orderCount: number;
}

export interface PaymentMethodSales {
  paymentMethod: string;
  orderCount: number;
  totalAmount: number;
}

export interface TopProduct {
  productId: number;
  productName: string;
  productImage?: string;
  totalQuantity: number;
  totalRevenue: number;
}
