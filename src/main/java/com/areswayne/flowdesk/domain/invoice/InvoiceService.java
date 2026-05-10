package com.areswayne.flowdesk.domain.invoice;

import com.areswayne.flowdesk.domain.invoice.dto.*;
import com.areswayne.flowdesk.domain.project.Project;
import com.areswayne.flowdesk.domain.project.ProjectRepository;
import com.areswayne.flowdesk.domain.timeentry.TimeEntry;
import com.areswayne.flowdesk.domain.timeentry.TimeEntryRepository;
import com.areswayne.flowdesk.domain.user.User;
import com.areswayne.flowdesk.domain.user.UserRepository;
import com.areswayne.flowdesk.shared.enums.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InvoiceResponse generate(GenerateInvoiceRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        if (project.getHourlyRate() == null) {
            throw new IllegalArgumentException(
                "El proyecto no tiene tarifa por hora definida. Agrégala antes de generar una factura.");
        }

        // Obtener time entries del proyecto
        List<TimeEntry> entries = timeEntryRepository
                .findByProjectId(request.projectId(), request.fromDate(), request.toDate());

        if (entries.isEmpty()) {
            throw new IllegalArgumentException(
                "No hay horas registradas en este proyecto para el período seleccionado");
        }

        // Generar número de factura — formato: FD-{projectShort}-{secuencia}
        long sequence = invoiceRepository.countByProjectId(request.projectId()) + 1;
        String number = String.format("FD-%s-%03d",
                project.getName().substring(0, Math.min(4, project.getName().length())).toUpperCase(),
                sequence);

        // Calcular total desde minutos registrados
        int totalMinutes = entries.stream().mapToInt(TimeEntry::getMinutes).sum();
        BigDecimal totalHours = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal total = totalHours.multiply(project.getHourlyRate())
                .setScale(2, RoundingMode.HALF_UP);

        // Crear factura
        int dueDays = request.dueDays() != null ? request.dueDays() : 30;
        Invoice invoice = Invoice.builder()
                .project(project)
                .number(number)
                .total(total)
                .status(InvoiceStatus.DRAFT)
                .issuedAt(LocalDate.now())
                .dueAt(LocalDate.now().plusDays(dueDays))
                .build();

        invoiceRepository.save(invoice);

        // Crear items — uno por cada time entry
        List<InvoiceItem> items = new ArrayList<>();
        for (TimeEntry entry : entries) {
            BigDecimal hours = BigDecimal.valueOf(entry.getMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            String description = String.format("%s — %s (%d min)",
                    entry.getTask().getTitle(),
                    entry.getUser().getName(),
                    entry.getMinutes());

            if (entry.getNote() != null && !entry.getNote().isBlank()) {
                description += ": " + entry.getNote();
            }

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(description)
                    .quantity(hours)
                    .unitPrice(project.getHourlyRate())
                    .build();

            items.add(item);
        }

        invoiceItemRepository.saveAll(items);
        invoice.setItems(items);

        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByProject(UUID projectId) {
        return invoiceRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        return toResponse(invoice);
    }

        @Transactional(readOnly = true)
        public Invoice getInvoiceForPdf(UUID invoiceId) {
                return invoiceRepository.findByIdWithProjectAndItems(invoiceId)
                                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        }

    @Transactional
    public InvoiceResponse updateStatus(UUID invoiceId, UpdateInvoiceStatusRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        // No se puede reabrir una factura cancelada
        if (invoice.getStatus() == InvoiceStatus.CANCELLED
                && request.status() != InvoiceStatus.CANCELLED) {
            throw new IllegalArgumentException("Una factura cancelada no puede cambiar de estado");
        }

        invoice.setStatus(request.status());
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void delete(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalArgumentException("Solo se pueden eliminar facturas en estado DRAFT");
        }

        invoiceRepository.deleteById(invoiceId);
    }

    // ── Helpers ──────────────────────────────────────────────

    private InvoiceResponse toResponse(Invoice inv) {
        List<InvoiceItemResponse> items = inv.getItems().stream()
                .map(item -> new InvoiceItemResponse(
                        item.getId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getQuantity().multiply(item.getUnitPrice())
                                .setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();

        return new InvoiceResponse(
                inv.getId(),
                inv.getProject().getId(),
                inv.getProject().getName(),
                inv.getNumber(),
                inv.getTotal(),
                inv.getStatus(),
                inv.getIssuedAt(),
                inv.getDueAt(),
                items,
                inv.getCreatedAt()
        );
    }
}