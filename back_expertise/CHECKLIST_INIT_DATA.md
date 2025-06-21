[Назад](README.md)
# Инициализация базовых - чек-листов

---

### Оптимизированная версия:
```sql
WITH
-- 1. Добавление чек-листа (checklists)
checklist AS (
INSERT INTO checklists (name, description)
VALUES ('Функциональные характеристики объекта строительства',
    'Данные об адресе объекта или номер кадастра')
    RETURNING id
    ),

-- 2. Добавление полей чек-листа (checklist_fields)
    checklist_fields AS (
INSERT INTO checklist_fields (checklist_id, field_name)
SELECT
    checklist.id,
    field_name
FROM checklist
    CROSS JOIN LATERAL (
    VALUES
    ('Кадастровый номер'),
    ('Адрес объекта')
    ) AS fields (field_name)
    RETURNING id, checklist_id, field_name
    ),

-- 3. Добавление параметров (parameters)
    params AS (
INSERT INTO parameters (name)
VALUES ('Полы')
    RETURNING id, name
    ),

-- 4. Связь чек-листа с параметрами (checklist_parameters)
    checklist_params AS (
INSERT INTO checklist_parameters (checklist_id, parameter_id)
SELECT
    checklist.id,
    params.id
FROM checklist, params
    RETURNING checklist_id, parameter_id
    ),

-- 5. Добавление типов параметров (parameter_types)
    param_types AS (
INSERT INTO parameter_types (parameter_id, name)
SELECT
    params.id,
    type_name
FROM params
    CROSS JOIN LATERAL (
    VALUES
    ('Ленточный фундамент'),
    ('Плиты'),
    ('Сваи')
    ) AS types (type_name)
    RETURNING id, parameter_id, name
    ),

-- 6. Добавление подтипов (subtypes)
    subtypes AS (
INSERT INTO subtypes (parameter_type_id, name)
SELECT
    param_types.id,
    subtype_name
FROM param_types
    CROSS JOIN LATERAL (
    SELECT unnest(ARRAY[
    CASE WHEN param_types.name = 'Ленточный фундамент' THEN 'Бетон' END,
    CASE WHEN param_types.name = 'Ленточный фундамент' THEN 'Кирпич' END,
    CASE WHEN param_types.name = 'Ленточный фундамент' THEN 'Камень' END,
    CASE WHEN param_types.name = 'Ленточный фундамент' THEN 'Блоки' END,
    CASE WHEN param_types.name = 'Ленточный фундамент' THEN 'Металлические' END,
    CASE WHEN param_types.name = 'Сваи' THEN 'Забивные' END,
    CASE WHEN param_types.name = 'Сваи' THEN 'Винтовые' END,
    CASE WHEN param_types.name = 'Сваи' THEN 'Буроинъекционные' END,
    CASE WHEN param_types.name = 'Сваи' THEN 'Висячая' END
    ]) AS subtype_name
    ) AS subtypes
WHERE subtype_name IS NOT NULL
    RETURNING parameter_type_id, name
    ),

-- 7. Добавление спецификаций параметров (parameter_specs)
    param_specs AS (
INSERT INTO parameter_specs (name, unit, is_required)
VALUES
    ('Глубина заложения', 'мм', TRUE),
    ('Ширина', 'мм', TRUE),
    ('Высота', 'мм', TRUE),
    ('Длина', 'мм', TRUE),
    ('Диаметр', 'мм', TRUE)
ON CONFLICT (name) DO NOTHING -- Чтобы избежать дублирования спецификаций
    RETURNING id, name
    ),

-- 8. Связь типов параметров со спецификациями (parameter_type_specs)
    param_type_specs AS (
INSERT INTO parameter_type_specs (parameter_type_id, spec_id)
SELECT
    param_types.id,
    param_specs.id
FROM param_types
    CROSS JOIN param_specs
WHERE (param_types.name, param_specs.name) IN (
    ('Ленточный фундамент', 'Глубина заложения'),
    ('Ленточный фундамент', 'Ширина'),
    ('Ленточный фундамент', 'Высота'),
    ('Плиты', 'Длина'),
    ('Плиты', 'Ширина'),
    ('Плиты', 'Высота'),
    ('Сваи', 'Глубина заложения'),
    ('Сваи', 'Ширина'),
    ('Сваи', 'Высота'),
    ('Сваи', 'Диаметр')
    )
    RETURNING parameter_type_id, spec_id
    )

-- Итоговый SELECT для завершения WITH
SELECT
    c.id AS checklist_id,
    cf.field_name AS field_name,
    p.name AS parameter_name,
    pt.name AS parameter_type_name,
    s.name AS subtype_name,
    ps.name AS spec_name
FROM checklist c
         LEFT JOIN checklist_fields cf ON cf.checklist_id = c.id
         LEFT JOIN checklist_parameters cp ON cp.checklist_id = c.id
         LEFT JOIN parameters p ON p.id = cp.parameter_id
         LEFT JOIN parameter_types pt ON pt.parameter_id = p.id
         LEFT JOIN subtypes s ON s.parameter_type_id = pt.id
         LEFT JOIN parameter_type_specs pts ON pts.parameter_type_id = pt.id
         LEFT JOIN parameter_specs ps ON ps.id = pts.spec_id
ORDER BY c.id, cf.field_name, p.name, pt.name, s.name, ps.name;
```

### Старая версия без оптимизации: 
```sql
-- Добавление чек-листа (checklists)
INSERT INTO checklists (id, name, description)
VALUES ('550e8400-e29b-41d4-a716-446655440100', 'Функциональные характеристики объекта строительства',
        'Данные об адресе объекта или номер кадастра');

--  Добавление параметров (parameters)
INSERT INTO parameters (id, name)
VALUES ('550e8400-e29b-41d4-a716-446655440200', 'Полы');

-- Связь чек-листа с параметрами (checklist_parameters)
INSERT INTO checklist_parameters (id, checklist_id, parameter_id)
VALUES ('550e8400-e29b-41d4-a716-446655440300', '550e8400-e29b-41d4-a716-446655440100',
        '550e8400-e29b-41d4-a716-446655440200');

-- Добавление типов параметров (parameter_types)
INSERT INTO parameter_types (id, parameter_id, name)
VALUES ('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200', 'Ленточный фундамент'),
       ('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440200', 'Плиты'),
       ('550e8400-e29b-41d4-a716-446655440402', '550e8400-e29b-41d4-a716-446655440200', 'Сваи');

-- Добавление подтипов (subtypes)
-- Подтипы для "Ленточный фундамент"
INSERT INTO subtypes (id, parameter_type_id, name)
VALUES ('550e8400-e29b-41d4-a716-446655440500', '550e8400-e29b-41d4-a716-446655440400', 'Бетон'),
       ('550e8400-e29b-41d4-a716-446655440501', '550e8400-e29b-41d4-a716-446655440400', 'Кирпич'),
       ('550e8400-e29b-41d4-a716-446655440502', '550e8400-e29b-41d4-a716-446655440400', 'Камень'),
       ('550e8400-e29b-41d4-a716-446655440503', '550e8400-e29b-41d4-a716-446655440400', 'Блоки'),
       ('550e8400-e29b-41d4-a716-446655440504', '550e8400-e29b-41d4-a716-446655440400', 'Металлические');

-- Подтипы для "Сваи"
INSERT INTO subtypes (id, parameter_type_id, name)
VALUES ('550e8400-e29b-41d4-a716-446655440505', '550e8400-e29b-41d4-a716-446655440402', 'Забивные'),
       ('550e8400-e29b-41d4-a716-446655440506', '550e8400-e29b-41d4-a716-446655440402', 'Винтовые'),
       ('550e8400-e29b-41d4-a716-446655440507', '550e8400-e29b-41d4-a716-446655440402', 'Буроинъекционные'),
       ('550e8400-e29b-41d4-a716-446655440508', '550e8400-e29b-41d4-a716-446655440402', 'Висячая');

-- Добавление спецификаций параметров (parameter_specs)
INSERT INTO parameter_specs (id, name, unit, is_required)
VALUES ('550e8400-e29b-41d4-a716-446655440600', 'Глубина заложения', 'мм', TRUE),
       ('550e8400-e29b-41d4-a716-446655440601', 'Ширина', 'мм', TRUE),
       ('550e8400-e29b-41d4-a716-446655440602', 'Высота', 'мм', TRUE),
       ('550e8400-e29b-41d4-a716-446655440603', 'Длина', 'мм', TRUE),
       ('550e8400-e29b-41d4-a716-446655440604', 'Диаметр', 'мм', TRUE);

--  Связь типов параметров со спецификациями (parameter_type_specs)
-- Спецификации для "Ленточный фундамент"
INSERT INTO parameter_type_specs (id, parameter_type_id, spec_id, default_value)
VALUES ('550e8400-e29b-41d4-a716-446655440700', '550e8400-e29b-41d4-a716-446655440400',
        '550e8400-e29b-41d4-a716-446655440600', NULL), -- Глубина заложения
       ('550e8400-e29b-41d4-a716-446655440701', '550e8400-e29b-41d4-a716-446655440400',
        '550e8400-e29b-41d4-a716-446655440601', NULL), -- Ширина
       ('550e8400-e29b-41d4-a716-446655440702', '550e8400-e29b-41d4-a716-446655440400',
        '550e8400-e29b-41d4-a716-446655440602', NULL);
-- Высота

-- Спецификации для "Плиты"
INSERT INTO parameter_type_specs (id, parameter_type_id, spec_id, default_value)
VALUES ('550e8400-e29b-41d4-a716-446655440703', '550e8400-e29b-41d4-a716-446655440401',
        '550e8400-e29b-41d4-a716-446655440603', NULL), -- Длина
       ('550e8400-e29b-41d4-a716-446655440704', '550e8400-e29b-41d4-a716-446655440401',
        '550e8400-e29b-41d4-a716-446655440601', NULL), -- Ширина
       ('550e8400-e29b-41d4-a716-446655440705', '550e8400-e29b-41d4-a716-446655440401',
        '550e8400-e29b-41d4-a716-446655440602', NULL);
-- Высота

-- Спецификации для "Сваи"
INSERT INTO parameter_type_specs (id, parameter_type_id, spec_id, default_value)
VALUES ('550e8400-e29b-41d4-a716-446655440706', '550e8400-e29b-41d4-a716-446655440402',
        '550e8400-e29b-41d4-a716-446655440600', NULL), -- Глубина заложения
       ('550e8400-e29b-41d4-a716-446655440707', '550e8400-e29b-41d4-a716-446655440402',
        '550e8400-e29b-41d4-a716-446655440601', NULL), -- Ширина
       ('550e8400-e29b-41d4-a716-446655440708', '550e8400-e29b-41d4-a716-446655440402',
        '550e8400-e29b-41d4-a716-446655440602', NULL), -- Высота
       ('550e8400-e29b-41d4-a716-446655440709', '550e8400-e29b-41d4-a716-446655440402',
        '550e8400-e29b-41d4-a716-446655440604', NULL); -- Диаметр


```