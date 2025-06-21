package com.example.expertise.enums.checklists;

public enum ChecklistJsonKey {

    // Общие ключи
    CADASTRAL_NUMBER("cadastral_number"),
    ADDRESS("address"),
    AREA("area"),
    COORDINATES("coordinates"),
    VALUE("value"),
    NAME("name"),
    PARAM_LABEL("param_label"),

    // Ключи для помещений и параметров
    PREMISES("premises"),
    PREMISE_NAME("premise_name"),
    PREMISE_PARAMETERS("premise_parameters"),
    PREMISE_PHOTOS("premise-photos"),

    // Дефекты
    DEFECT_PHOTOS("defect-photos"),
    DEFECT_SCHEMA_PDF("defect-schema-pdf"),
    DEFECT_DESCRIPTION("defect_description"),
    DEFECT_VOLUME("defect_volume"),
    DEFECT_NOTE("defect_note"),

    // Типовой текст
    TYPE_TEXT_PARAMS("type_text_params"),

    // Земельные ключи
    INTERSECTION("intersection_percentage"),
    CATEGORY("land_category"),
    PERMITTED_USE("permitted_use"),
    VRI_CODES("vri_codes"),
    DISTRICT("district"),
    DISTINCT_MAP_TEXT("distinct-map-text"),

    // Изображения
    CADASTRE_IMAGES("cadastre-images"),
    OPEN_SOURCE_IMAGES("open-source-images"),
    DISTRICT_MAP_IMAGES("district-map-images"),
    PZZ_SCREENSHOTS("pzz-screenshots");

    private final String key;

    ChecklistJsonKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
