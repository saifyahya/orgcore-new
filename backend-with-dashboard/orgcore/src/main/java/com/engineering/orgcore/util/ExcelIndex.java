package com.engineering.orgcore.util;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface ExcelIndex {
    int value();                 // 0-based column index
    boolean required() default true;
}