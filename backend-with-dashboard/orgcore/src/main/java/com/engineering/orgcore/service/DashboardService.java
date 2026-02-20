package com.engineering.orgcore.service;

import com.engineering.orgcore.dto.dashboard.*;
import com.engineering.orgcore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SaleRepository       saleRepository;
    private final CategoryRepository   categoryRepository;
    private final BranchRepository     branchRepository;
    private final SaleItemRepository saleItemRepository;

    /** ISO day labels: index 0 = Monday … index 6 = Sunday */
    private static final String[] DAY_LABELS = {
            "SUNDAY",
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY"
    };

    private static final String[] MONTH_LABELS =
            {
                    "JANUARY",
                    "FEBRUARY",
                    "MARCH",
                    "APRIL",
                    "MAY",
                    "JUNE",
                    "JULY",
                    "AUGUST",
                    "SEPTEMBER",
                    "OCTOBER",
                    "NOVEMBER",
                    "DECEMBER"
            };

    // ─── KPI Summary ──────────────────────────────────────────────────────────

    public DashboardSummaryDto getSummary(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate) {
        DashboardSummaryDto raw = saleRepository.getSalesSummary(tenantId, branchId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX) );

        long totalSaleItems  = saleItemRepository.getTotalSaleItems(tenantId, branchId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX) );

        long totalCategories = categoryRepository.count();
        long activeBranches = branchRepository.countByTenantIdAndIsActive(tenantId, 1);

        // avg sales per product (totalAmount / distinct products sold)
        double avgPerSale = (totalSaleItems > 0 && raw.finalAmount() != null)
                ? raw.finalAmount() / totalSaleItems : 0.0;

        return new DashboardSummaryDto(
                raw.totalSalesAmount()   != null ? raw.totalSalesAmount()   : 0.0,
                raw.totalDiscountAmount() != null ? raw.totalDiscountAmount() : 0.0,
                raw.totalTaxAmount()     != null ? raw.totalTaxAmount()     : 0.0,
                raw.finalAmount()        != null ? raw.finalAmount()        : 0.0,
                raw.totalOrders()        != null ? raw.totalOrders()        : 0L,
                raw.avgOrderValue()      != null ? raw.avgOrderValue()      : 0.0,
                avgPerSale,
                totalSaleItems,
                totalCategories,
                activeBranches
        );
    }

    // ─── Monthly Sales (amount + orders) — fills all 12 months ───────────────

    public List<MonthlySeriesDto> getMonthlySales(Long tenantId, Long branch, Integer year) {
        List<MonthlySeriesDto> raw = saleRepository.getMonthlySales(tenantId, branch, year);
        Map<Integer, MonthlySeriesDto> byMonth = raw.stream()
                .collect(Collectors.toMap(MonthlySeriesDto::month, m -> m));

        List<MonthlySeriesDto> full = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            MonthlySeriesDto data = byMonth.get(m);
            full.add(new MonthlySeriesDto(
                    year,
                    m,
                    MONTH_LABELS[m - 1],
                    data != null ? data.totalAmount()  : 0.0,
                    data != null ? data.orderCount()   : 0L,
                    data != null ? data.individualCount() : 0L
            ));
        }
        return full;
    }

    // ─── Weekly Sales (per day of week) — fills all 7 days ───────────────────

    public List<WeeklyDaySeriesDto> getWeeklySalesByDay(Long tenantId, Long branchId ,LocalDate startDate) {
        List<WeeklyDaySeriesDto> raw = saleRepository.getWeeklySalesByDay(tenantId, branchId , startDate.atStartOfDay(), startDate.plusDays(7).atTime(LocalTime.MAX));

        // SQL DAYOFWEEK: Sun=1, Mon=2 … Sat=7
        Map<Integer, WeeklyDaySeriesDto> byDay = raw.stream()
                .collect(Collectors.toMap(
                        w -> (Integer) w.dayOfWeek(),
                        w -> w
                ));

        List<WeeklyDaySeriesDto> full = new ArrayList<>(7);
        for (int iso = 1; iso <= 7; iso++) {
            WeeklyDaySeriesDto data = byDay.get(iso);
            full.add(new WeeklyDaySeriesDto(
                    iso,
                    DAY_LABELS[iso - 1],
                    data != null ? data.totalAmount() : 0.0,
                    data != null ? data.orderCount()  : 0,
                    data != null ? data.individualCount() : 0
                    ));
        }
        return full;
    }

    /** MySQL DAYOFWEEK (Sun=1 … Sat=7) → ISO (Mon=1 … Sun=7) */
    private int mysqlDayToIso(int mysqlDay) {
        // Sun(1)->7, Mon(2)->1, Tue(3)->2, ... Sat(7)->6
        return (mysqlDay == 1) ? 7 : mysqlDay - 1;
    }

    // ─── Sales by Payment Method ──────────────────────────────────────────────

    public List<PaymentMethodSalesDto> getSalesByPaymentMethod(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getSalesByPaymentMethod(tenantId, branchId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX) );
    }

    // ─── Sales by Category ────────────────────────────────────────────────────

    public List<CategorySalesDto> getSalesByCategory(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getSalesByCategory(tenantId, branchId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX) );
    }

    // ─── Top Products by Quantity ─────────────────────────────────────────────

    public List<TopProductDto> getTopProductsByQuantity(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getTopProductsByQuantity(
                tenantId, branchId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    // ─── Top Products by Revenue ──────────────────────────────────────────────

    public List<TopProductDto> getTopProductsByRevenue(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getTopProductsByRevenue(
                tenantId, branchId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    // ─── Monthly sales for a single product ──────────────────────────────────

//    public List<MonthlySeriesDto> getMonthlyProductSales(Long tenantId, Long productId, int year) {
//        List<MonthlySeriesDto> raw = saleRepository.getMonthlyProductSales(tenantId, productId, year);
//        Map<Integer, MonthlySeriesDto> byMonth = raw.stream()
//                .collect(Collectors.toMap(MonthlySeriesDto::month, m -> m));
//
//        List<MonthlySeriesDto> full = new ArrayList<>(12);
//        for (int m = 1; m <= 12; m++) {
//            MonthlySeriesDto data = byMonth.get(m);
//            full.add(new MonthlySeriesDto(
//                    year, m, MONTH_LABELS[m - 1],
//                    data != null ? data.totalAmount() : 0.0,
//                    data != null ? data.orderCount()  : 0L
//            ));
//        }
//        return full;
//    }
}
