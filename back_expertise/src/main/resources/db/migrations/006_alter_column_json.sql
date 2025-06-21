-- Изменение типа structure в checklist_template с jsonb на json
ALTER TABLE checklist_template
ALTER COLUMN structure TYPE json
    USING structure::json;

-- Изменение типа data в checklist_instance с jsonb на json
ALTER TABLE checklist_instance
ALTER COLUMN data TYPE json
    USING data::json;
