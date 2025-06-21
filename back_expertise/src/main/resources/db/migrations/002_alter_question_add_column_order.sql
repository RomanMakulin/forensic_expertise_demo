ALTER TABLE expertise_question ADD COLUMN question_order INTEGER;
UPDATE expertise_question SET question_order = 0 WHERE question_order IS NULL;