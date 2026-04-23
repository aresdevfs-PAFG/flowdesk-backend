CREATE TABLE projects (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    status       VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE','ON_HOLD','COMPLETED','ARCHIVED')),
    hourly_rate  NUMERIC(10,2),
    start_date   DATE,
    end_date     DATE,
    created_at   TIMESTAMP DEFAULT now(),
    updated_at   TIMESTAMP DEFAULT now()
);

CREATE TABLE project_members (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    user_id    UUID NOT NULL REFERENCES users(id),
    role       VARCHAR(20) NOT NULL CHECK (role IN ('MANAGER','COLLABORATOR','VIEWER')),
    UNIQUE (project_id, user_id)
);