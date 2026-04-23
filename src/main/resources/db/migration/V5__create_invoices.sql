CREATE TABLE invoices (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    number     VARCHAR(50) NOT NULL UNIQUE,
    total      NUMERIC(12,2) NOT NULL,
    status     VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT','SENT','PAID','OVERDUE','CANCELLED')),
    issued_at  DATE,
    due_at     DATE,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE invoice_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id  UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(300) NOT NULL,
    quantity    NUMERIC(10,2) NOT NULL,
    unit_price  NUMERIC(10,2) NOT NULL
);