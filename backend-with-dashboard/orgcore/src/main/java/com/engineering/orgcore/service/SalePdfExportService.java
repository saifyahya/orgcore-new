package com.engineering.orgcore.service;

import com.engineering.orgcore.entity.Sale;
import com.engineering.orgcore.entity.SaleItem;
import com.engineering.orgcore.util.ArabicTextUtil;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class SalePdfExportService {

    // ── Brand colours ─────────────────────────────────────────────────────────
    private static final DeviceRgb HEADER_BG    = new DeviceRgb(0x1E, 0x3A, 0x5F);
    private static final DeviceRgb ACCENT_BLUE  = new DeviceRgb(0x26, 0x7A, 0xFF);
    private static final DeviceRgb TABLE_HEADER = new DeviceRgb(0xF0, 0xF4, 0xFF);
    private static final DeviceRgb ROW_ALT      = new DeviceRgb(0xF8, 0xF9, 0xFF);
    private static final DeviceRgb DIVIDER      = new DeviceRgb(0xE2, 0xE8, 0xF0);
    private static final DeviceRgb TEXT_DARK    = new DeviceRgb(0x1A, 0x20, 0x2C);
    private static final DeviceRgb TEXT_MUTED   = new DeviceRgb(0x71, 0x80, 0x96);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm:ss a");
    private static final NumberFormat      CURRENCY;

    static {
        CURRENCY = NumberFormat.getNumberInstance(Locale.US);
        CURRENCY.setMinimumFractionDigits(2);
        CURRENCY.setMaximumFractionDigits(2);
    }

    // ── Load Arial TTF bytes once from classpath ──────────────────────────────
    private static final byte[] ARIAL_BYTES;
    private static final byte[] ARIAL_BOLD_BYTES;

    static {
        try {
            ARIAL_BYTES      = new ClassPathResource("fonts/arial.ttf").getInputStream().readAllBytes();
            ARIAL_BOLD_BYTES = new ClassPathResource("fonts/arialbd.ttf").getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Cannot load Arial fonts from classpath: " + e.getMessage());
        }
    }

    /** Creates a fresh embedded PdfFont for the regular Arial TTF. */
    private static PdfFont regularFont() throws IOException {
        return PdfFontFactory.createFont(ARIAL_BYTES, "Identity-H", EmbeddingStrategy.FORCE_EMBEDDED);
    }

    /** Creates a fresh embedded PdfFont for the bold Arial TTF. */
    private static PdfFont boldFont() throws IOException {
        return PdfFontFactory.createFont(ARIAL_BOLD_BYTES, "Identity-H", EmbeddingStrategy.FORCE_EMBEDDED);
    }

    // ── Shorthand: reshape Arabic then wrap in Paragraph ─────────────────────
    // After ICU4J visual reordering, the string is already in left-to-right
    // display order – so we must align LEFT (not RIGHT) for it to read correctly.
    private static Paragraph ar(String text, PdfFont font, float size, Color color) {
        return new Paragraph(ArabicTextUtil.reshape(text))
                .setFont(font).setFontSize(size).setFontColor(color)
                .setTextAlignment(TextAlignment.LEFT)
                .setWidth(UnitValue.createPercentValue(100));
    }

    // ─────────────────────────────────────────────────────────────────────────
    public byte[] exportSaleToPdf(Sale sale) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter   writer   = new PdfWriter(baos);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        Document    document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(0, 0, 36, 0);

        PdfFont regular = regularFont();
        PdfFont bold    = boldFont();

        // ── 1. Hero header ───────────────────────────────────────────────────
        document.add(buildHeroHeader(sale, bold, regular));

        // ── 2. Info-card row ─────────────────────────────────────────────────
        Div body = new Div().setMarginLeft(36).setMarginRight(36).setMarginTop(24);
        body.add(buildInfoCards(sale, bold, regular));

        // ── 3. Items table ───────────────────────────────────────────────────
        body.add(ar("عناصر البيع (" + sale.getItems().size() + ")", bold, 14, TEXT_DARK)
                .setMarginTop(28).setMarginBottom(12));

        body.add(buildItemsTable(sale.getItems(), bold, regular));

        // ── 4. Totals block ──────────────────────────────────────────────────
        body.add(buildTotalsBlock(sale, bold, regular));

        document.add(body);
        document.close();

        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private Div buildHeroHeader(Sale sale, PdfFont bold, PdfFont regular) {
        Div hero = new Div()
                .setBackgroundColor(HEADER_BG)
                .setPaddingLeft(36).setPaddingRight(36)
                .setPaddingTop(28).setPaddingBottom(24)
                .setWidth(UnitValue.createPercentValue(100));

        hero.add(ar("عملية البيع رقم " + sale.getId(), bold, 22, ColorConstants.WHITE)
                .setMarginBottom(4));

        hero.add(ar("تفاصيل البيع والعناصر", regular, 11, new DeviceRgb(0xB0, 0xC4, 0xDE))
                .setMarginBottom(0));

        return hero;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private Table buildInfoCards(Sale sale, PdfFont bold, PdfFont regular) {

        String branchName = sale.getBranch()        != null ? sale.getBranch().getBranchName()     : "N/A";
        String dateStr    = sale.getCreatedAt()     != null ? sale.getCreatedAt().format(DATE_FMT) : "N/A";
        String channel    = sale.getChannel()       != null ? sale.getChannel().name()             : "N/A";
        String payment    = sale.getPaymentMethod() != null ? sale.getPaymentMethod().name()       : "N/A";
        String discount   = sale.getDiscountRate()  != null ? sale.getDiscountRate() + "%"         : "0%";
        String tax        = sale.getTaxRate()       != null ? sale.getTaxRate()      + "%"         : "0%";
        String createdBy  = sale.getCreatedBy()     != null ? sale.getCreatedBy()                  : "N/A";

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0);

        table.addCell(infoCard("الفرع",            branchName, bold, regular));
        table.addCell(infoCard("التاريخ",          dateStr,    bold, regular));
        table.addCell(infoCard("أنشأ بواسطة",      createdBy,  bold, regular));
        table.addCell(infoCard("القناة",           channel,    bold, regular));
        table.addCell(infoCard("طريقة الدفع",      payment,    bold, regular));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(6));
        table.addCell(infoCard("نسبة الخصم (%)",   discount,   bold, regular));
        table.addCell(infoCard("نسبة الضريبة (%)", tax,        bold, regular));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(6));

        return table;
    }

    private Cell infoCard(String label, String value, PdfFont bold, PdfFont regular) {
        Div card = new Div()
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(DIVIDER, 1))
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(8))
                .setPadding(14);

        // label is always Arabic – always reshape
        card.add(ar(label, regular, 9, TEXT_MUTED).setMarginBottom(4));

        // value may be Latin (dates, branch names) – reshape only if Arabic
        boolean isArabic = ArabicTextUtil.containsArabic(value);
        card.add(new Paragraph(isArabic ? ArabicTextUtil.reshape(value) : value)
                .setFont(bold).setFontSize(12).setFontColor(TEXT_DARK)
                .setTextAlignment(isArabic ? TextAlignment.LEFT : TextAlignment.RIGHT)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0));

        return new Cell().add(card).setBorder(Border.NO_BORDER).setPadding(6);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private Table buildItemsTable(List<SaleItem> items, PdfFont bold, PdfFont regular) {

        float[] cols = {30, 25, 10, 17, 18};
        Table table = new Table(UnitValue.createPercentArray(cols))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0);

        String[] headers = {"الرمز", "المنتج", "الكمية", "سعر الوحدة", "إجمالي السطر"};
        for (String h : headers) {
            table.addHeaderCell(
                    new Cell().add(ar(h, bold, 10, TEXT_DARK))
                            .setBackgroundColor(TABLE_HEADER)
                            .setBorderBottom(new SolidBorder(ACCENT_BLUE, 2))
                            .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                            .setPadding(10)
            );
        }

        boolean alt = false;
        for (SaleItem item : items) {
            Color rowBg = alt ? ROW_ALT : ColorConstants.WHITE;
            alt = !alt;

            String code  = item.getProduct() != null && item.getProduct().getCode() != null ? item.getProduct().getCode() : "N/A";
            String name  = item.getProduct() != null && item.getProduct().getName() != null ? item.getProduct().getName() : "N/A";
            int    qty   = item.getQuantity()  != null ? item.getQuantity()  : 0;
            double price = item.getUnitPrice() != null ? item.getUnitPrice() : 0.0;
            double line  = item.getLineTotal() != null ? item.getLineTotal() : 0.0;

            table.addCell(dataCell(ArabicTextUtil.reshapeIfArabic(code),              rowBg, regular, TextAlignment.RIGHT));
            table.addCell(dataCell(ArabicTextUtil.reshapeIfArabic(name),              rowBg, bold,    TextAlignment.RIGHT));
            table.addCell(dataCell(String.valueOf(qty),                               rowBg, regular, TextAlignment.CENTER));
            table.addCell(dataCell("JOD " + CURRENCY.format(price),                  rowBg, regular, TextAlignment.RIGHT));
            table.addCell(dataCell("JOD " + CURRENCY.format(line),                   rowBg, bold,    TextAlignment.RIGHT));
        }

        return table;
    }

    private Cell dataCell(String text, Color bg, PdfFont font, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(TEXT_DARK))
                .setBackgroundColor(bg)
                .setBorderBottom(new SolidBorder(DIVIDER, 0.5f))
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setPaddingTop(10).setPaddingBottom(10).setPaddingLeft(10).setPaddingRight(10)
                .setTextAlignment(align);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private Div buildTotalsBlock(Sale sale, PdfFont bold, PdfFont regular) {

        double subtotal = sale.getTotalAmount()  != null ? sale.getTotalAmount()  : 0.0;
        double discRate = sale.getDiscountRate() != null ? sale.getDiscountRate() : 0.0;
        double taxRate  = sale.getTaxRate()      != null ? sale.getTaxRate()      : 0.0;
        double discAmt  = subtotal * discRate / 100.0;
        double taxAmt   = subtotal * taxRate  / 100.0;
        double finalAmt = sale.getFinalAmount()  != null ? sale.getFinalAmount()  : subtotal - discAmt + taxAmt;

        Div outer = new Div().setMarginTop(4);

        outer.add(new Paragraph(" ")
                .setBorderTop(new SolidBorder(DIVIDER, 1))
                .setMarginTop(12).setMarginBottom(0));

        Div totals = new Div()
                .setWidth(UnitValue.createPercentValue(42))
                .setHorizontalAlignment(HorizontalAlignment.LEFT);

        totals.add(totalsRow("المجموع الفرعي", "JOD " + CURRENCY.format(subtotal),   regular, regular, TEXT_MUTED,  TEXT_DARK,  false));
        totals.add(totalsRow("الخصم",          "- JOD " + CURRENCY.format(discAmt),  regular, regular, TEXT_MUTED,  new DeviceRgb(0xE5, 0x3E, 0x3E), false));
        totals.add(totalsRow("الضريبة",        "+ JOD " + CURRENCY.format(taxAmt),   regular, regular, TEXT_MUTED,  TEXT_DARK,  false));

        totals.add(new Paragraph(" ")
                .setBorderTop(new SolidBorder(DIVIDER, 1))
                .setMarginTop(6).setMarginBottom(6));

        totals.add(totalsRow("الإجمالي الكلي", "JOD " + CURRENCY.format(finalAmt),   bold,    bold,    TEXT_DARK,   ACCENT_BLUE, true));

        outer.add(totals);
        return outer;
    }

    private Div totalsRow(String label, String value,
                          PdfFont labelFont, PdfFont valueFont,
                          DeviceRgb labelColor, DeviceRgb valueColor, boolean large) {
        float size = large ? 13f : 11f;

        Table row = new Table(new float[]{50, 50})
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(4);

        row.addCell(new Cell()
                .add(new Paragraph(value).setFont(valueFont).setFontSize(size).setFontColor(valueColor))
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).setPadding(2));

        row.addCell(new Cell()
                .add(ar(label, labelFont, size, labelColor))
                .setBorder(Border.NO_BORDER).setPadding(2));

        Div wrap = new Div();
        wrap.add(row);
        return wrap;
    }
}
