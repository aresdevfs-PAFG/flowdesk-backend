package com.areswayne.flowdesk.infrastructure.pdf;

import com.areswayne.flowdesk.domain.invoice.Invoice;
import com.areswayne.flowdesk.domain.invoice.InvoiceItem;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;

@Service
public class PdfService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(99, 102, 241); // indigo
    private static final DeviceRgb ROW_ALT_COLOR = new DeviceRgb(241, 245, 249);

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out));
             Document doc = new Document(pdf)) {

            // ── Encabezado ──────────────────────────────────
            Paragraph title = new Paragraph("FLOWDESK")
                    .setBold()
                    .setFontSize(24)
                    .setFontColor(HEADER_COLOR)
                    .setMarginBottom(0);
            doc.add(title);

            doc.add(new Paragraph("Plataforma de gestión de proyectos")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            // ── Datos de la factura ──────────────────────────
            doc.add(new Paragraph("FACTURA")
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(5));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            infoTable.addCell(infoCell("Número:", invoice.getNumber()));
            infoTable.addCell(infoCell("Proyecto:", invoice.getProject().getName()));
            infoTable.addCell(infoCell("Fecha de emisión:",
                    invoice.getIssuedAt() != null ? invoice.getIssuedAt().toString() : "—"));
            infoTable.addCell(infoCell("Fecha de vencimiento:",
                    invoice.getDueAt() != null ? invoice.getDueAt().toString() : "—"));
            infoTable.addCell(infoCell("Estado:", invoice.getStatus().name()));
            infoTable.addCell(new Cell().setBorder(null));

            doc.add(infoTable);

            // ── Tabla de items ───────────────────────────────
            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 1.5f, 1.5f, 1.5f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            // Headers
            for (String header : new String[]{"Descripción", "Horas", "Tarifa/h", "Subtotal"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(HEADER_COLOR)
                        .setPadding(8));
            }

            // Rows
            boolean alt = false;
            for (InvoiceItem item : invoice.getItems()) {
                DeviceRgb bg = alt ? ROW_ALT_COLOR : new DeviceRgb(255, 255, 255);
                String subtotal = item.getQuantity()
                        .multiply(item.getUnitPrice())
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();

                table.addCell(rowCell(item.getDescription(), bg, TextAlignment.LEFT));
                table.addCell(rowCell(item.getQuantity().toPlainString() + " h", bg, TextAlignment.CENTER));
                table.addCell(rowCell("$" + item.getUnitPrice().toPlainString(), bg, TextAlignment.RIGHT));
                table.addCell(rowCell("$" + subtotal, bg, TextAlignment.RIGHT));
                alt = !alt;
            }

            doc.add(table);

            // ── Total ────────────────────────────────────────
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100));

            totalTable.addCell(new Cell()
                    .add(new Paragraph("TOTAL")
                            .setBold()
                            .setFontSize(14)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null));

            totalTable.addCell(new Cell()
                    .add(new Paragraph("$" + invoice.getTotal().toPlainString())
                            .setBold()
                            .setFontSize(14)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null));

            doc.add(totalTable);

            // ── Footer ───────────────────────────────────────
            doc.add(new Paragraph("\nGenerado por FlowDesk — flowdesk.app")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private Cell infoCell(String label, String value) {
        return new Cell()
                .add(new Paragraph(label + " " + value).setFontSize(10))
                .setBorder(null)
                .setPaddingBottom(4);
    }

    private Cell rowCell(String text, DeviceRgb bg, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9))
                .setBackgroundColor(bg)
                .setTextAlignment(align)
                .setPadding(6);
    }
}