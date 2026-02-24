package com.engineering.orgcore.repository;

import com.engineering.orgcore.dto.dashboard.*;
import com.engineering.orgcore.entity.Sale;
import com.engineering.orgcore.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

@Query("""
        SELECT s FROM Sale s
        WHERE s.tenantId = :tenantId
          AND (s.createdAt >= :startDate AND s.createdAt < :endDate)
          AND (s.branch.id = :branchId OR :branchId IS NULL)
        """)
    Page<Sale> findAllByTenantId(Long tenantId, @Param("branchId") Long branchId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    Optional<Sale> findByTenantIdAndExternalRef(Long tenantId, String externalRef);

    // ─── KPI summary ──────────────────────────────────────────────────────────
    @Query("""
                SELECT new com.engineering.orgcore.dto.dashboard.DashboardSummaryDto(
                    COALESCE(SUM(s.totalAmount), 0.0),
                    COALESCE(SUM(s.discountRate * s.totalAmount), 0.0),
                    COALESCE(SUM(s.taxRate * s.totalAmount), 0.0),
                    COALESCE(SUM(s.finalAmount), 0.0),
                    COUNT(s.id),
                    CASE WHEN COUNT(s.id) > 0 THEN COALESCE(SUM(s.finalAmount), 0.0) / COUNT(s.id) ELSE 0.0 END,
                    0.0, 0L, 0L, 0L
                )
                FROM Sale s
                WHERE s.tenantId = :tenantId
                  AND (s.createdAt >= :startDate AND s.createdAt < :endDate)
                  AND (s.branch.id = :branchId OR :branchId IS NULL)
            """)
    DashboardSummaryDto getSalesSummary(@Param("tenantId") Long tenantId, @Param("branchId") Long branchId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ─── Monthly sales (amount + order count, SQL MONTH: JANUARY=1,..) ─────────────────────────────────
    @Query("""
                SELECT new com.engineering.orgcore.dto.dashboard.MonthlySeriesDto(
                    YEAR(s.createdAt),
                    MONTH(s.createdAt),
                    '',
                    COALESCE(SUM(s.finalAmount), 0.0),
                    COALESCE(COUNT(s.id), 0),
                    COALESCE(SUM(i.quantity), 0)
                )
                FROM Sale s
                LEFT JOIN s.items i
                WHERE s.tenantId = :tenantId
                  AND (:branchId IS NULL OR s.branch.id = :branchId)
                  AND YEAR(s.createdAt) = :year
                GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)
                ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)
            """)
    List<MonthlySeriesDto> getMonthlySales(Long tenantId, Long branchId, int year);

    // ─── Weekly sales by day-of-week (MySQL DAYOFWEEK: Sun=1 … Sat=7) ─────────
    @Query(value = """
            WITH per_sale AS (
                                                                    SELECT
                                                                      s.id,
                                                                      s.created_at,
                                                                      s.final_amount,
                                                                      ISNULL(SUM(si.quantity), 0) AS total_qty
                                                                    FROM sale s
                                                                    LEFT JOIN sale_item si ON si.sale_id = s.id
                                                                    WHERE s.tenant_id = :tenantId
                                                                      AND (s.created_at >= :startDate AND s.created_at < :endDate)
                                                                      AND (:branchId IS NULL OR s.branch_id = :branchId)
                                                                    GROUP BY s.id, s.created_at, s.final_amount
                                                                  )
                                                                  SELECT
                                                                    CAST(MIN(created_at) AS date) AS saleDate,
                                                                    DATEPART(WEEKDAY, created_at) AS dayOfWeek,
                                                                    'N/A' AS dayLabel,
                                                                    COALESCE(SUM(final_amount), 0) AS totalAmount,
                                                                    COALESCE(SUM(total_qty), 0) AS orderCount,
                                                                    COUNT(id) AS individualCount
                                                                  FROM per_sale
                                                                  GROUP BY DATEPART(WEEKDAY, created_at)
                                                                  ORDER BY DATEPART(WEEKDAY, created_at);
            
            """, nativeQuery = true)
    List<WeeklyDaySeriesDto> getWeeklySalesByDay(Long tenantId, Long branchId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ─── Sales by payment method ───────────────────────────────────────────────
    @Query("""
                SELECT new com.engineering.orgcore.dto.dashboard.PaymentMethodSalesDto(
                    CAST(s.paymentMethod AS string), COUNT(s.id), COALESCE(SUM(s.finalAmount), 0.0)
                )
                FROM Sale s
                WHERE s.tenantId = :tenantId
                AND (s.createdAt >= :startDate AND s.createdAt < :endDate)
                AND (s.branch.id = :branchId OR :branchId IS NULL)
                AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod)
                GROUP BY s.paymentMethod
                ORDER BY SUM(s.finalAmount) DESC
            """)
    List<PaymentMethodSalesDto> getSalesByPaymentMethod(@Param("tenantId") Long tenantId,
                                                        @Param("branchId") Long branchId,
                                                        @Param("paymentMethod") PaymentMethod paymentMethod,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    // ─── Sales by category ────────────────────────────────────────────────────
    @Query("""
                SELECT new com.engineering.orgcore.dto.dashboard.CategorySalesDto(
                    p.category.id, p.category.name, p.category.image,
                    SUM(si.quantity), COALESCE(SUM(si.lineTotal), 0.0), COUNT(DISTINCT s.id)
                )
                FROM Sale s JOIN s.items si JOIN si.product p
                WHERE s.tenantId = :tenantId
                AND (s.createdAt >= :startDate AND s.createdAt < :endDate)
                AND (s.branch.id = :branchId OR :branchId IS NULL)
                AND (p.category.id = :categoryId OR :categoryId IS NULL)
                GROUP BY p.category.id, p.category.name, p.category.image
                ORDER BY SUM(si.lineTotal) DESC
            """)
    List<CategorySalesDto> getSalesByCategory(@Param("tenantId") Long tenantId,
                                              @Param("branchId") Long branchId,
                                              @Param("categoryId") Long categoryId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // ─── Top products by quantity sold ────────────────────────────────────────
    @Query("""
                SELECT new com.engineering.orgcore.dto.dashboard.TopProductDto(
                    p.id, p.name, p.image, SUM(si.quantity), COALESCE(SUM(si.lineTotal), 0.0)
                )
                FROM Sale s JOIN s.items si JOIN si.product p
                WHERE s.tenantId = :tenantId
                AND (s.createdAt >= :startDate AND s.createdAt < :endDate)
                AND (:branchId IS NULL OR s.branch.id = :branchId )
                AND (:productId IS NULL OR p.id = :productId )
                GROUP BY p.id, p.name, p.image
                ORDER BY SUM(si.quantity) DESC
            """)
    List<TopProductDto> getTopProductsByQuantity(@Param("tenantId") Long tenantId,
                                                 @Param("branchId") Long branchId,
                                                 @Param("productId") Long productId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // ─── Top products by revenue ──────────────────────────────────────────────
    @Query("""
                SELECT new com.engineering.orgcore.dto.dashboard.TopProductDto(
                    p.id, p.name, p.image, SUM(si.quantity), COALESCE(SUM(si.lineTotal), 0.0)
                )
                FROM Sale s JOIN s.items si JOIN si.product p
                WHERE s.tenantId = :tenantId
                AND (s.createdAt >= :startDate AND s.createdAt < :endDate)
                AND (s.branch.id = :branchId OR :branchId IS NULL)
                AND (:productId IS NULL OR p.id = :productId )
                GROUP BY p.id, p.name, p.image
                ORDER BY SUM(si.lineTotal) DESC
            """)
    List<TopProductDto> getTopProductsByRevenue(@Param("tenantId") Long tenantId,
                                                @Param("branchId") Long branchId,
                                                @Param("productId") Long productId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // ─── Monthly sales for a specific product ────────────────────────────────
//    @Query("""
//        SELECT new com.engineering.orgcore.dto.dashboard.MonthlySeriesDto(
//            YEAR(s.createdAt), MONTH(s.createdAt), '',
//            COALESCE(SUM(si.lineTotal), 0.0), CAST(SUM(si.quantity) AS long)
//        )
//        FROM Sale s JOIN s.items si
//        WHERE s.tenantId = :tenantId AND si.product.id = :productId
//          AND YEAR(s.createdAt) = :year
//        GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)
//        ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)
//    """)
//    List<MonthlySeriesDto> getMonthlyProductSales(
//            @Param("tenantId") Long tenantId,
//            @Param("productId") Long productId,
//            @Param("year") int year
//    );


}
