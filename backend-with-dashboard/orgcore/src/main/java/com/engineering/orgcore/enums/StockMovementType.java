package com.engineering.orgcore.enums;

public enum StockMovementType {
    IN,            // زيادة مخزون (شراء / إرجاع)
    OUT,           // نقصان مخزون (بيع)
    ADJUST,        // تعديل يدوي (+ أو - حسب reason/notes)
    TRANSFER_IN,   // دخل من تحويل من فرع آخر
    TRANSFER_OUT   // خرج لتحويل لفرع آخر
}