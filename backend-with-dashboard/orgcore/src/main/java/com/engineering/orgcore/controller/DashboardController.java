package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.dashboard.*;
import com.engineering.orgcore.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All dashboard/analytics endpoints.
 * Base path: /dashboard
 * All endpoints are tenant-scoped (extracted from the JWT principal).
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;
    private final Utils            utils;



    // ─── KPI Summary cards ────────────────────────────────────────────────────

    /**
     * GET /dashboard/summary?year=2024
     * Returns aggregate KPIs: totalSales, totalDiscount, totalOrders, avgOrderValue,
     * avgSalesPerProduct, totalProducts, totalCategories, activeBranches.
     */
    @GetMapping("/summary")
    public DashboardSummaryDto getSummary(
            @RequestParam(required = false) Long branchId,  @RequestParam LocalDate startDate, @RequestParam LocalDate endDate
    ) {
        return dashboardService.getSummary(utils.getCurrentTenant(), branchId, startDate, endDate);
    }

    // ─── Monthly Sales (amount + order count) ─────────────────────────────────

    /**
     * GET /dashboard/monthly-sales?year=2024
     * Returns 12 rows (Jan-Dec) with totalAmount and orderCount per month.
     * Used for the line chart (Monthly Sales Trend) and bar chart (Orders by Month).
     */
    @GetMapping("/monthly-sales")
    public List<MonthlySeriesDto> getMonthlySales(
            @RequestParam Integer year, @RequestParam(required = false) Long branchId
    ) {
        return dashboardService.getMonthlySales(utils.getCurrentTenant(), branchId, year);
    }

    // ─── Weekly Sales by Day-of-Week ──────────────────────────────────────────

    /**
     * GET /dashboard/weekly-sales?year=2024
     * Returns 7 rows (Mon-Sun) with orderCount and totalAmount.
     * Used for the weekly bar chart.
     */
    @GetMapping("/weekly-sales")
    public List<WeeklyDaySeriesDto> getWeeklySalesByDay(
            @RequestParam(required = false) Long branchId, @RequestParam LocalDate startDate
    ) {
        return dashboardService.getWeeklySalesByDay(utils.getCurrentTenant(), branchId, startDate);
    }

    // ─── Sales by Payment Method (doughnut chart) ─────────────────────────────

    /**
     * GET /dashboard/sales-by-payment?year=2024
     * Returns one row per payment method with orderCount and totalAmount.
     */
    @GetMapping("/sales-by-payment")
    public List<PaymentMethodSalesDto> getSalesByPaymentMethod(
            @RequestParam(required = false) Long branchId,  @RequestParam LocalDate startDate, @RequestParam LocalDate endDate
    ) {
        return dashboardService.getSalesByPaymentMethod(utils.getCurrentTenant(), branchId, startDate, endDate);
    }

    // ─── Sales by Category (pie chart) ────────────────────────────────────────

    /**
     * GET /dashboard/sales-by-category?year=2024
     * Returns one row per category with totalRevenue, totalQuantity and orderCount.
     */
    @GetMapping("/sales-by-category")
    public List<CategorySalesDto> getSalesByCategory(
            @RequestParam(required = false) Long branchId,  @RequestParam LocalDate startDate, @RequestParam LocalDate endDate
    ) {
        return dashboardService.getSalesByCategory(utils.getCurrentTenant(), branchId, startDate, endDate);
    }

    // ─── Top Products by Quantity (bar chart) ─────────────────────────────────

    /**
     * GET /dashboard/top-products-qty?year=2024&limit=10
     * Returns the top N products sorted by total units sold.
     */
    @GetMapping("/top-products-qty")
    public List<TopProductDto> getTopProductsByQuantity(
            @RequestParam(required = false) Long branchId,  @RequestParam LocalDate startDate, @RequestParam LocalDate endDate
    ) {
        return dashboardService.getTopProductsByQuantity(utils.getCurrentTenant(), branchId, startDate, endDate);
    }

    // ─── Top Products by Revenue (bar chart) ──────────────────────────────────

    /**
     * GET /dashboard/top-products-revenue?year=2024&limit=10
     * Returns the top N products sorted by total revenue.
     */
    @GetMapping("/top-products-revenue")
    public List<TopProductDto> getTopProductsByRevenue(
            @RequestParam(required = false) Long branchId,  @RequestParam LocalDate startDate, @RequestParam LocalDate endDate
    ) {
        return dashboardService.getTopProductsByRevenue(utils.getCurrentTenant(), branchId, startDate, endDate);
    }

    // ─── Monthly sales for one specific product ───────────────────────────────

    /**
     * GET /dashboard/product-monthly-sales/{productId}?year=2024
     * Returns 12 rows for a single product (revenue + quantity per month).
     */
//    @GetMapping("/product-monthly-sales/{productId}")
//    public List<MonthlySeriesDto> getMonthlyProductSales(
//            @PathVariable Long    productId,
//            @RequestParam Integer year
//    ) {
//        return dashboardService.getMonthlyProductSales(
//                utils.getCurrentTenant(), productId, year);
//    }
}
