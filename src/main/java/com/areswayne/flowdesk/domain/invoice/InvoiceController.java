package com.areswayne.flowdesk.domain.invoice;

import com.areswayne.flowdesk.domain.invoice.dto.*;
import com.areswayne.flowdesk.infrastructure.pdf.PdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generate(@Valid @RequestBody GenerateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.generate(request));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<InvoiceResponse>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(invoiceService.getByProject(projectId));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.getById(invoiceId));
    }

    @PatchMapping("/{invoiceId}/status")
    public ResponseEntity<InvoiceResponse> updateStatus(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        return ResponseEntity.ok(invoiceService.updateStatus(invoiceId, request));
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<Void> delete(@PathVariable UUID invoiceId) {
        invoiceService.delete(invoiceId);
        return ResponseEntity.noContent().build();
    }

    // Exportar PDF
    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID invoiceId) {
        Invoice invoice = invoiceService.getInvoiceForPdf(invoiceId);

        byte[] pdf = pdfService.generateInvoicePdf(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"factura-" + invoice.getNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}