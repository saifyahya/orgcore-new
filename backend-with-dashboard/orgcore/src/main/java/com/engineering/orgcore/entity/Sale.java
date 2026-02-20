package com.engineering.orgcore.entity;

import com.engineering.orgcore.enums.PaymentMethod;
import com.engineering.orgcore.enums.SaleChannel;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    private Double totalAmount;

    private Double discountAmount;

    private Double taxAmount;

    private Double finalAmount;

    // Optional: payment info
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    // channel (manual, import, api)
    @Enumerated(EnumType.STRING)
    private SaleChannel channel = SaleChannel.MANUAL;

    // If imported from POS, keep a reference to avoid duplicates
    private String externalRef;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    // Helper to keep both sides consistent
    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
    }
}
