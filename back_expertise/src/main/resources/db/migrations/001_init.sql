-- Создание таблицы expertise
CREATE TABLE expertise
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id           UUID NOT NULL,
    case_number          VARCHAR(255),
    name                 VARCHAR(255),
    ruling_date          DATE,
    court_name           VARCHAR(255),
    start_date           DATE,
    end_date             DATE,
    sign_date            DATE,
    presiding_judge      VARCHAR(255),
    plaintiff            TEXT,
    location             VARCHAR(255),
    volume_count         VARCHAR(255),
    participants         TEXT,
    inspection_date_time TIMESTAMP,
    speciality           VARCHAR(200),
    template_name        VARCHAR(250)
);

-- Создание таблицы expertise_judge
CREATE TABLE expertise_judge
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name    VARCHAR(255),
    expertise_id UUID NOT NULL,
    FOREIGN KEY (expertise_id) REFERENCES expertise (id) ON DELETE CASCADE
);

-- Создание таблицы expertise_question
CREATE TABLE expertise_question
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_text     TEXT NOT NULL,
    answer            TEXT,
    answer_conclusion TEXT,
    expertise_id      UUID,
    FOREIGN KEY (expertise_id) REFERENCES expertise (id) ON DELETE CASCADE
);

-- Создание таблицы checklists
CREATE TABLE checklists
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

-- Создание таблицы parameters
CREATE TABLE parameters
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
);

-- Создание таблицы checklist_parameters
CREATE TABLE checklist_parameters
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_id UUID NOT NULL,
    parameter_id UUID NOT NULL,
    FOREIGN KEY (checklist_id) REFERENCES checklists (id) ON DELETE CASCADE,
    FOREIGN KEY (parameter_id) REFERENCES parameters (id) ON DELETE CASCADE
);

-- Создание таблицы parameter_types
CREATE TABLE parameter_types
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parameter_id UUID         NOT NULL,
    name         VARCHAR(255) NOT NULL,
    FOREIGN KEY (parameter_id) REFERENCES parameters (id) ON DELETE CASCADE
);

-- Создание таблицы subtypes
CREATE TABLE subtypes
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parameter_type_id UUID         NOT NULL,
    name              VARCHAR(255) NOT NULL,
    FOREIGN KEY (parameter_type_id) REFERENCES parameter_types (id) ON DELETE CASCADE
);

-- Создание таблицы parameter_specs
CREATE TABLE parameter_specs
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    unit        VARCHAR(50),
    is_required BOOLEAN DEFAULT TRUE
);

-- Создание таблицы parameter_type_specs
CREATE TABLE parameter_type_specs
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parameter_type_id UUID NOT NULL,
    spec_id           UUID NOT NULL,
    default_value     VARCHAR(255),
    FOREIGN KEY (parameter_type_id) REFERENCES parameter_types (id) ON DELETE CASCADE,
    FOREIGN KEY (spec_id) REFERENCES parameter_specs (id) ON DELETE CASCADE
);

-- Создание таблицы checklist_instances
CREATE TABLE checklist_instances
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_id          UUID NOT NULL,
    parameter_id          UUID NOT NULL,
    parameter_type_id     UUID NOT NULL,
    subtype_id            UUID,
    expertise_question_id UUID,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (checklist_id) REFERENCES checklists (id) ON DELETE CASCADE,
    FOREIGN KEY (parameter_id) REFERENCES parameters (id) ON DELETE CASCADE,
    FOREIGN KEY (parameter_type_id) REFERENCES parameter_types (id) ON DELETE CASCADE,
    FOREIGN KEY (subtype_id) REFERENCES subtypes (id) ON DELETE SET NULL,
    FOREIGN KEY (expertise_question_id) REFERENCES expertise_question (id) ON DELETE CASCADE
);

-- Создание таблицы spec_values
CREATE TABLE spec_values
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_instance_id UUID NOT NULL,
    spec_id               UUID NOT NULL,
    value                 VARCHAR(255),
    FOREIGN KEY (checklist_instance_id) REFERENCES checklist_instances (id) ON DELETE CASCADE,
    FOREIGN KEY (spec_id) REFERENCES parameter_specs (id) ON DELETE CASCADE
);

-- Создание таблицы expertise_photo
CREATE TABLE expertise_photo
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_path   VARCHAR(255) NOT NULL,
    question_id UUID         NOT NULL,
    FOREIGN KEY (question_id) REFERENCES expertise_question (id) ON DELETE CASCADE
);

-- Создание таблицы checklist_gosts
CREATE TABLE checklist_gosts
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_id UUID NOT NULL,
    name         VARCHAR(255),
    FOREIGN KEY (checklist_id) REFERENCES checklists (id) ON DELETE CASCADE
);

-- Создание таблицы checklist_fields
CREATE TABLE checklist_fields
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_id UUID         NOT NULL,
    field_name   VARCHAR(255) NOT NULL,
    FOREIGN KEY (checklist_id) REFERENCES checklists (id) ON DELETE CASCADE,
    UNIQUE (checklist_id, field_name)
);

-- Создание таблицы checklist_instance_fields
CREATE TABLE checklist_instance_fields
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_instance_id UUID NOT NULL,
    checklist_field_id    UUID NOT NULL,
    value                 VARCHAR(255),
    FOREIGN KEY (checklist_instance_id) REFERENCES checklist_instances (id) ON DELETE CASCADE,
    FOREIGN KEY (checklist_field_id) REFERENCES checklist_fields (id) ON DELETE CASCADE,
    UNIQUE (checklist_instance_id, checklist_field_id)
);

-- Создание индексов
CREATE INDEX idx_expertise_profile_id ON expertise (profile_id);
CREATE INDEX idx_judge_expertise_id ON expertise_judge (expertise_id);
CREATE INDEX idx_expertise_question_expertise_id ON expertise_question (expertise_id);
CREATE INDEX idx_expertise_photo_question_id ON expertise_photo (question_id);
CREATE INDEX idx_expertise_photo_file_path ON expertise_photo (file_path);
CREATE INDEX idx_checklist_parameters_checklist_id ON checklist_parameters (checklist_id);
CREATE INDEX idx_checklist_parameters_parameter_id ON checklist_parameters (parameter_id);
CREATE INDEX idx_parameter_types_parameter_id ON parameter_types (parameter_id);
CREATE INDEX idx_subtypes_parameter_type_id ON subtypes (parameter_type_id);
CREATE INDEX idx_parameter_type_specs_parameter_type_id ON parameter_type_specs (parameter_type_id);
CREATE INDEX idx_parameter_type_specs_spec_id ON parameter_type_specs (spec_id);
CREATE INDEX idx_checklist_instances_checklist_id ON checklist_instances (checklist_id);
CREATE INDEX idx_checklist_instances_parameter_id ON checklist_instances (parameter_id);
CREATE INDEX idx_checklist_instances_parameter_type_id ON checklist_instances (parameter_type_id);
CREATE INDEX idx_checklist_instances_expertise_question_id ON checklist_instances (expertise_question_id);
CREATE INDEX idx_spec_values_checklist_instance_id ON spec_values (checklist_instance_id);
CREATE INDEX idx_spec_values_spec_id ON spec_values (spec_id);
CREATE INDEX idx_checklist_gosts_checklist_id ON checklist_gosts (checklist_id);