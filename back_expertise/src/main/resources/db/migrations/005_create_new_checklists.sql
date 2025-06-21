-- Создание таблицы шаблонов чек-листов
CREATE TABLE checklist_template
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT  NOT NULL, -- Название шаблона (например, "Характеристики объекта строительства")
    structure  JSONB NOT NULL, -- Структура шаблона в формате JSON
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы экземпляров чек-листов
CREATE TABLE checklist_instance
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id           UUID  NOT NULL, -- Ссылка на вопрос (например, из expertise_question)
    checklist_template_id UUID  NOT NULL, -- Ссылка на шаблон
    data                  JSONB NOT NULL, -- Заполненные пользователем данные по шаблону
    created_at            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (question_id) REFERENCES expertise_question (id) ON DELETE CASCADE,
    FOREIGN KEY (checklist_template_id) REFERENCES checklist_template (id) ON DELETE CASCADE
);

-- Индексы для ускорения запросов
CREATE INDEX idx_checklist_instance_question_id ON checklist_instance (question_id);
CREATE INDEX idx_checklist_instance_template_id ON checklist_instance (checklist_template_id);
