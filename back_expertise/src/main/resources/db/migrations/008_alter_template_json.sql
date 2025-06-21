ALTER TABLE checklist_template
ALTER COLUMN structure TYPE jsonb
    USING structure::jsonb;