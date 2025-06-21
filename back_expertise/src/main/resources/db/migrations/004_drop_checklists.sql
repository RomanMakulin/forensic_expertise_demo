-- Ввиду новых требований заказчика, необходимо пересмотреть архитектуру чек-листов.
-- Текущая версия не поддерживает новую структуру чек-листов, поэтому необходимо её удалить.

-- Удаляем таблицы, зависящие от checklist_instances
DROP TABLE IF EXISTS checklist_instance_fields CASCADE;
DROP TABLE IF EXISTS spec_values CASCADE;

-- Удаляем checklist_instances
DROP TABLE IF EXISTS checklist_instances CASCADE;

-- Удаляем таблицы, зависящие от checklists
DROP TABLE IF EXISTS checklist_parameters CASCADE;
DROP TABLE IF EXISTS checklist_gosts CASCADE;
DROP TABLE IF EXISTS checklist_fields CASCADE;

-- Удаляем таблицу checklists
DROP TABLE IF EXISTS checklists CASCADE;

-- Удаляем вложенные таблицы параметров
DROP TABLE IF EXISTS parameter_type_specs CASCADE;
DROP TABLE IF EXISTS subtypes CASCADE;
DROP TABLE IF EXISTS parameter_types CASCADE;
DROP TABLE IF EXISTS parameter_specs CASCADE;
DROP TABLE IF EXISTS parameters CASCADE;
