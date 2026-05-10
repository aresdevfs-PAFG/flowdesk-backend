package com.areswayne.flowdesk.domain.invoice;

import com.areswayne.flowdesk.shared.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Invoice> findByProjectIdAndStatus(UUID projectId, InvoiceStatus status);

    Optional<Invoice> findByNumber(String number);

        @Query("select distinct i from Invoice i " +
            "join fetch i.project p " +
            "left join fetch i.items items " +
            "where i.id = :invoiceId")
        Optional<Invoice> findByIdWithProjectAndItems(@Param("invoiceId") UUID invoiceId);

    boolean existsByNumber(String number);

    // Para generar número único autoincremental
    long countByProjectId(UUID projectId);
}