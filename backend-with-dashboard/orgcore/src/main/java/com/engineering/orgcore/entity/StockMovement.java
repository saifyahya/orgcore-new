
package com.engineering.orgcore.entity;

import com.engineering.orgcore.enums.ReferenceType;
import com.engineering.orgcore.enums.StockMovementReason;
import com.engineering.orgcore.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockMovement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long branchId;

    private Long productId;

    @Enumerated(EnumType.STRING)
    private StockMovementType type;

    @Enumerated(EnumType.STRING)
    private StockMovementReason reason;

    private Integer quantity;

    // optional, useful for margin/profit later
    private Double unitCost;

    @Enumerated(EnumType.STRING)
    private ReferenceType refType;

    // sale_id / transfer_id / etc
    private String refId;

    private String note;

}