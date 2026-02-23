package com.engineering.orgcore.service;

import com.engineering.orgcore.dto.dashboard.*;
import com.engineering.orgcore.enums.PaymentMethod;
import com.engineering.orgcore.repository.BranchRepository;
import com.engineering.orgcore.repository.CategoryRepository;
import com.engineering.orgcore.repository.SaleItemRepository;
import com.engineering.orgcore.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SaleRepository saleRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final SaleItemRepository saleItemRepository;

    /**
     * ISO day labels: index 0 = Monday … index 6 = Sunday
     */
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
        DashboardSummaryDto raw = saleRepository.getSalesSummary(tenantId, branchId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        Long totalSaleItems = saleItemRepository.getTotalSaleItems(tenantId, branchId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        totalSaleItems = totalSaleItems == null ? 0 : totalSaleItems;

        Long totalCategories = categoryRepository.count();
        Long activeBranches = branchRepository.countByTenantIdAndIsActive(tenantId, 1);

        // avg sales per product (totalAmount / distinct products sold)
        double avgPerSale = (totalSaleItems > 0 && raw.finalAmount() != null)
                ? raw.finalAmount() / totalSaleItems : 0.0;

        return new DashboardSummaryDto(
                raw.totalSalesAmount() != null ? raw.totalSalesAmount() : 0.0,
                raw.totalDiscountAmount() != null ? raw.totalDiscountAmount() : 0.0,
                raw.totalTaxAmount() != null ? raw.totalTaxAmount() : 0.0,
                raw.finalAmount() != null ? raw.finalAmount() : 0.0,
                raw.totalOrders() != null ? raw.totalOrders() : 0L,
                raw.avgOrderValue() != null ? raw.avgOrderValue() : 0.0,
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
                    data != null ? data.totalAmount() : 0.0,
                    data != null ? data.orderCount() : 0L,
                    data != null ? data.individualCount() : 0L
            ));
        }
        return full;
    }

    // ─── Weekly Sales (per day of week) — fills all 7 days ───────────────────

    public List<WeeklyDaySeriesDto> getWeeklySalesByDay(Long tenantId, Long branchId, LocalDate startDate) {
        LocalDate startOfWeek =
                startDate.minusDays(startDate.getDayOfWeek().getValue() % 7);
        LocalDate endOfWeek = startOfWeek.plusDays(7);
        List<WeeklyDaySeriesDto> raw = saleRepository.getWeeklySalesByDay(tenantId, branchId, startOfWeek.atStartOfDay(), endOfWeek.plusDays(1).atStartOfDay());

        // SQL DAYOFWEEK: Sun=1, Mon=2 … Sat=7
        Map<LocalDate, WeeklyDaySeriesDto> byDay = raw.stream()
                .collect(Collectors.toMap(
                        WeeklyDaySeriesDto::saleDate,
                        w -> w
                ));

        List<WeeklyDaySeriesDto> full = new ArrayList<>(7);
        int day = 1;
        while (startOfWeek.isBefore(endOfWeek)) {
            WeeklyDaySeriesDto data = byDay.get(startOfWeek);

            full.add(new WeeklyDaySeriesDto(
                    data != null ? data.saleDate() : startOfWeek,
                    day,
                    DAY_LABELS[day - 1],
                    data != null ? data.totalAmount() : 0.0,
                    data != null ? data.orderCount() : 0,
                    data != null ? data.individualCount() : 0
            ));
            startOfWeek = startOfWeek.plusDays(1);
            day++;
        }
        return full;
    }

    /**
     * MySQL DAYOFWEEK (Sun=1 … Sat=7) → ISO (Mon=1 … Sun=7)
     */
    private int mysqlDayToIso(int mysqlDay) {
        // Sun(1)->7, Mon(2)->1, Tue(3)->2, ... Sat(7)->6
        return (mysqlDay == 1) ? 7 : mysqlDay - 1;
    }

    // ─── Sales by Payment Method ──────────────────────────────────────────────

    public List<PaymentMethodSalesDto> getSalesByPaymentMethod(Long tenantId, Long branchId, String paymentMethod, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getSalesByPaymentMethod(tenantId, branchId,paymentMethod != null ?  PaymentMethod.valueOf(paymentMethod) : null, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    // ─── Sales by Category ────────────────────────────────────────────────────

    public List<CategorySalesDto> getSalesByCategory(Long tenantId, Long branchId,Long categoryId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getSalesByCategory(tenantId, branchId, categoryId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    // ─── Top Products by Quantity ─────────────────────────────────────────────

    public List<TopProductDto> getTopProductsByQuantity(Long tenantId, Long branchId, Long productId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getTopProductsByQuantity(
                tenantId, branchId, productId, startDate.atStartOfDay(), endDate.plusDays(1).atTime(LocalTime.MAX));
    }

    // ─── Top Products by Revenue ──────────────────────────────────────────────

    public List<TopProductDto> getTopProductsByRevenue(Long tenantId, Long branchId, Long productId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.getTopProductsByRevenue(
                tenantId, branchId, productId, startDate.atStartOfDay(), endDate.plusDays(1).atTime(LocalTime.MAX));
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
