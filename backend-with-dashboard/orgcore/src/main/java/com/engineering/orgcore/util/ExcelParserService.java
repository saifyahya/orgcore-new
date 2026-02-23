package com.engineering.orgcore.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.*;

@Service
public class ExcelParserService {
    public <T> List<T> read(InputStream inputStream, int sheetIndex, int startRowIndex, Class<T> recordType) {
        if (recordType == null) throw new IllegalArgumentException("recordType is required");
        if (!recordType.isRecord()) {
            throw new IllegalArgumentException("recordType must be a record. Got: " + recordType.getName());
        }

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            if (sheet == null) return List.of();

            RecordComponent[] components = recordType.getRecordComponents();
            Object[] args = new Object[components.length];
            Class<?>[] ctorTypes = Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new);

            Constructor<T> ctor = recordType.getDeclaredConstructor(ctorTypes);
            ctor.setAccessible(true);

            List<T> result = new ArrayList<>();

            int lastRow = sheet.getLastRowNum();
            for (int r = startRowIndex; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (isBlankRow(row)) continue;

                for (int i = 0; i < components.length; i++) {
                    RecordComponent rc = components[i];

                    ExcelIndex idx = rc.getAnnotation(ExcelIndex.class);
                    if (idx == null) {
                        throw new IllegalArgumentException("Missing @ExcelIndex on record component: " + rc.getName());
                    }

                    int col = idx.value();
                    boolean required = idx.required();

                    Cell cell = (row == null) ? null : row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    Object value = convertCell(cell, rc.getType());

                    if (required && value == null) {
                        throw new IllegalArgumentException("Missing required value at row " + (r + 1)
                                + ", col " + (col + 1) + " for field '" + rc.getName() + "'");
                    }

                    args[i] = value;
                }

                result.add(ctor.newInstance(args));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel into " + recordType.getSimpleName(), e);
        }
    }

    // ---------------- Helpers ----------------

    private boolean isBlankRow(Row row) {
        if (row == null) return true;
        short lastCell = row.getLastCellNum();
        if (lastCell <= 0) return true;

        for (int i = 0; i < lastCell; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) continue;

            if (cell.getCellType() == CellType.BLANK) continue;

            if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) continue;

            return false;
        }
        return true;
    }

    private Object convertCell(Cell cell, Class<?> targetType) {
        if (cell == null) return null;

        // String
        if (targetType == String.class) {
            String s = cellToString(cell);
            return (s == null || s.isBlank()) ? null : s;
        }

        // Long
        if (targetType == Long.class || targetType == long.class) {
            Double n = cellToNumber(cell);
            return (n == null) ? null : n.longValue();
        }

        // Integer
        if (targetType == Integer.class || targetType == int.class) {
            Double n = cellToNumber(cell);
            return (n == null) ? null : n.intValue();
        }

        // Double
        if (targetType == Double.class || targetType == double.class) {
            return cellToNumber(cell);
        }

        // Boolean
        if (targetType == Boolean.class || targetType == boolean.class) {
            return cellToBoolean(cell);
        }

        // Enum (optional support)
        if (targetType.isEnum()) {
            String s = cellToString(cell);
            if (s == null || s.isBlank()) return null;
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            return Enum.valueOf(enumType, s.trim()); // expects exact enum name
        }

        throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
    }

    private String cellToString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                long lv = (long) v;
                yield (v == lv) ? String.valueOf(lv) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula(); // if you want evaluated value, we can add FormulaEvaluator
            default -> null;
        };
    }

    private Double cellToNumber(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                String s = cell.getStringCellValue();
                if (s == null || s.isBlank()) yield null;
                yield Double.parseDouble(s.trim());
            }
            default -> null;
        };
    }

    private Boolean cellToBoolean(Cell cell) {
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> {
                String s = cell.getStringCellValue();
                if (s == null || s.isBlank()) yield null;
                yield Boolean.parseBoolean(s.trim());
            }
            default -> null;
        };
    }
}
