ALTER TABLE checklist_template
ALTER COLUMN structure TYPE text
    USING structure::text;

ALTER TABLE checklist_instance
ALTER COLUMN data TYPE text
    USING data::text;
