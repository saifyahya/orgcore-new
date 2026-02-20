import { DashboardSummary, MonthlySeries, WeeklyDaySeries, CategorySales, PaymentMethodSales, TopProduct } from '../../core/models';

export const FAKE_SUMMARY: DashboardSummary = {
    totalSalesAmount: 2847650.75,
    totalOrders: 1847,
    avgOrderValue: 1542.33,
    totalDiscountAmount: 142380.50,
    totalProducts: 342,
    totalCategories: 18,
    activeBranches: 7,
    avgSalesPerProduct: 8327.34
};

export const FAKE_MONTHLY: MonthlySeries[] = [
    { year: 2026, month: 1, monthLabel: 'يناير', totalAmount: 185420.50, orderCount: 142 },
    { year: 2026, month: 2, monthLabel: 'فبراير', totalAmount: 198750.25, orderCount: 156 },
    { year: 2026, month: 3, monthLabel: 'مارس', totalAmount: 245680.75, orderCount: 189 },
    { year: 2026, month: 4, monthLabel: 'أبريل', totalAmount: 267890.00, orderCount: 201 },
    { year: 2026, month: 5, monthLabel: 'مايو', totalAmount: 289340.50, orderCount: 218 },
    { year: 2026, month: 6, monthLabel: 'يونيو', totalAmount: 312560.25, orderCount: 234 },
    { year: 2026, month: 7, monthLabel: 'يوليو', totalAmount: 298450.75, orderCount: 225 },
    { year: 2026, month: 8, monthLabel: 'أغسطس', totalAmount: 276890.00, orderCount: 207 },
    { year: 2026, month: 9, monthLabel: 'سبتمبر', totalAmount: 254320.50, orderCount: 195 },
    { year: 2026, month: 10, monthLabel: 'أكتوبر', totalAmount: 223450.25, orderCount: 172 },
    { year: 2026, month: 11, monthLabel: 'نوفمبر', totalAmount: 201890.75, orderCount: 158 },
    { year: 2026, month: 12, monthLabel: 'ديسمبر', totalAmount: 193007.25, orderCount: 150 }
];

export const FAKE_WEEKLY: WeeklyDaySeries[] = [
    { dayOfWeek: 1, dayLabel: 'الإثنين', totalAmount: 42350.50, orderCount: 67 },
    { dayOfWeek: 2, dayLabel: 'الثلاثاء', totalAmount: 38920.75, orderCount: 61 },
    { dayOfWeek: 3, dayLabel: 'الأربعاء', totalAmount: 45680.25, orderCount: 72 },
    { dayOfWeek: 4, dayLabel: 'الخميس', totalAmount: 51240.00, orderCount: 81 },
    { dayOfWeek: 5, dayLabel: 'الجمعة', totalAmount: 58760.50, orderCount: 93 },
    { dayOfWeek: 6, dayLabel: 'السبت', totalAmount: 67890.75, orderCount: 107 },
    { dayOfWeek: 7, dayLabel: 'الأحد', totalAmount: 48320.25, orderCount: 76 }
];

export const FAKE_CATEGORIES: CategorySales[] = [
    { categoryId: 1, categoryName: 'المشويات', totalQuantity: 1245, totalRevenue: 487650.50, orderCount: 342 },
    { categoryId: 2, categoryName: 'المقبلات', totalQuantity: 2134, totalRevenue: 398420.75, orderCount: 521 },
    { categoryId: 3, categoryName: 'السلطات', totalQuantity: 987, totalRevenue: 156780.25, orderCount: 289 },
    { categoryId: 4, categoryName: 'المشروبات', totalQuantity: 3456, totalRevenue: 298340.00, orderCount: 867 },
    { categoryId: 5, categoryName: 'الحلويات', totalQuantity: 1567, totalRevenue: 287650.50, orderCount: 412 },
    { categoryId: 6, categoryName: 'المعجنات', totalQuantity: 1123, totalRevenue: 245890.75, orderCount: 356 },
    { categoryId: 7, categoryName: 'الوجبات السريعة', totalQuantity: 1456, totalRevenue: 312450.25, orderCount: 398 },
    { categoryId: 8, categoryName: 'الأطباق الرئيسية', totalQuantity: 943, totalRevenue: 398760.00, orderCount: 245 },
    { categoryId: 9, categoryName: 'العصائر الطبيعية', totalQuantity: 2345, totalRevenue: 167890.50, orderCount: 487 },
    { categoryId: 10, categoryName: 'القهوة والشاي', totalQuantity: 1876, totalRevenue: 156340.25, orderCount: 534 }
];

export const FAKE_PAYMENTS: PaymentMethodSales[] = [
    { paymentMethod: 'بطاقة ائتمان', totalAmount: 1245680.75, orderCount: 892 },
    { paymentMethod: 'نقدي', totalAmount: 687420.50, orderCount: 534 },
    { paymentMethod: 'بطاقة مدى', totalAmount: 534890.25, orderCount: 312 },
    { paymentMethod: 'محفظة إلكترونية', totalAmount: 298760.00, orderCount: 87 },
    { paymentMethod: 'تحويل بنكي', totalAmount: 80899.25, orderCount: 22 }
];

export const FAKE_TOP_QTY: TopProduct[] = [
    { productId: 1, productName: 'شاورما دجاج', totalQuantity: 487, totalRevenue: 24350.00 },
    { productId: 2, productName: 'برجر لحم', totalQuantity: 423, totalRevenue: 21150.00 },
    { productId: 3, productName: 'بيتزا مارجريتا', totalQuantity: 398, totalRevenue: 27860.00 },
    { productId: 4, productName: 'سلطة سيزر', totalQuantity: 367, totalRevenue: 14680.00 },
    { productId: 5, productName: 'عصير برتقال طازج', totalQuantity: 342, totalRevenue: 10260.00 },
    { productId: 6, productName: 'فطيرة جبن', totalQuantity: 318, totalRevenue: 9540.00 },
    { productId: 7, productName: 'كنافة نابلسية', totalQuantity: 289, totalRevenue: 17340.00 },
    { productId: 8, productName: 'قهوة عربية', totalQuantity: 267, totalRevenue: 6675.00 },
    { productId: 9, productName: 'حمص بالطحينة', totalQuantity: 245, totalRevenue: 7350.00 },
    { productId: 10, productName: 'فلافل', totalQuantity: 223, totalRevenue: 5575.00 }
];

export const FAKE_TOP_REV: TopProduct[] = [
    { productId: 11, productName: 'مشاوي مشكلة للعائلة', totalQuantity: 67, totalRevenue: 134000.00 },
    { productId: 12, productName: 'سمك مشوي كامل', totalQuantity: 89, totalRevenue: 89000.00 },
    { productId: 13, productName: 'ستيك لحم أنجس', totalQuantity: 112, totalRevenue: 78400.00 },
    { productId: 14, productName: 'صينية كباب', totalQuantity: 134, totalRevenue: 67000.00 },
    { productId: 15, productName: 'دجاج مشوي كامل', totalQuantity: 98, totalRevenue: 58800.00 },
    { productId: 16, productName: 'مقلوبة لحم', totalQuantity: 156, totalRevenue: 54600.00 },
    { productId: 17, productName: 'مندي دجاج', totalQuantity: 178, totalRevenue: 53400.00 },
    { productId: 18, productName: 'كبسة لحم', totalQuantity: 145, totalRevenue: 50750.00 },
    { productId: 19, productName: 'برياني روبيان', totalQuantity: 87, totalRevenue: 47850.00 },
    { productId: 20, productName: 'مظبي دجاج', totalQuantity: 123, totalRevenue: 44280.00 }
];

export const FAKE_RECENT_SALES: any[] = [
    { id: 1, createdAt: '2026-02-17T14:30:00', branch: { branchName: 'الفرع الرئيسي' }, totalAmount: 1245.50, paymentMethod: 'بطاقة ائتمان', channel: 'POS' },
    { id: 2, createdAt: '2026-02-17T13:15:00', branch: { branchName: 'فرع الشمال' }, totalAmount: 892.75, paymentMethod: 'نقدي', channel: 'POS' },
    { id: 3, createdAt: '2026-02-16T19:45:00', branch: { branchName: 'الفرع الرئيسي' }, totalAmount: 2340.00, paymentMethod: 'بطاقة مدى', channel: 'POS' },
    { id: 4, createdAt: '2026-02-16T18:20:00', branch: { branchName: 'فرع الجنوب' }, totalAmount: 567.25, paymentMethod: 'نقدي', channel: 'MANUAL' },
    { id: 5, createdAt: '2026-02-16T16:10:00', branch: { branchName: 'فرع الشرق' }, totalAmount: 1890.50, paymentMethod: 'بطاقة ائتمان', channel: 'POS' },
    { id: 6, createdAt: '2026-02-15T20:30:00', branch: { branchName: 'الفرع الرئيسي' }, totalAmount: 3456.75, paymentMethod: 'محفظة إلكترونية', channel: 'POS' },
    { id: 7, createdAt: '2026-02-15T15:45:00', branch: { branchName: 'فرع الغرب' }, totalAmount: 678.00, paymentMethod: 'نقدي', channel: 'POS' },
    { id: 8, createdAt: '2026-02-15T14:20:00', branch: { branchName: 'فرع الشمال' }, totalAmount: 1234.50, paymentMethod: 'بطاقة ائتمان', channel: 'POS' },
    { id: 9, createdAt: '2026-02-14T21:15:00', branch: { branchName: 'الفرع الرئيسي' }, totalAmount: 2890.25, paymentMethod: 'بطاقة مدى', channel: 'POS' },
    { id: 10, createdAt: '2026-02-14T17:30:00', branch: { branchName: 'فرع الجنوب' }, totalAmount: 456.75, paymentMethod: 'نقدي', channel: 'MANUAL' }
];
