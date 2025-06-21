package com.example.expertise.enums;

/**
 * Список закладок экспертизы
 */
public enum Bookmarks {

    // MERGEFIELD
    END_DATE("Дата_окончания_экспертизы"),
    START_DATE("Дата_начала_экспертизы"),
    EXPERTISE_NAME("Наименование_экспертизы_какой"),
    RULING_DATE("Определение_от_число"),
    COURT_NAME("Суд_какого"),
    CASE_NUMBER("Номер_дела"),
    SIGN_DATE("Дата_подписки_минус_2_дня_от_даты_начал"),
    PRESIDING_JUDGE("Председательствующего_судьи"),
    PLAINTIFF("По_иску"),
    LOCATION("Месторасположения_объекта_экспертизы"),
    VOLUME_COUNT("Сколько_томов"),
    PARTICIPANTS("Присутствующие_при_осмотре"),
    INSPECTION_DATE_TIME("Дата_и_время_осмотра"),
    ALL_JUDGES("Судей"),
    ALL_QUESTIONS("Список_вопросы"),
    QUESTION_ANSWER_SUMMARY("Список_вопрос_ответ_резюме"),
    QUESTION_CONCLUSION("Список_вопрос_вывод"),

    // MERGEFIELD DOCS
    MAIN_DIPLOMA("Основной_диплом"),
    ADDITIONAL_DIPLOMA_LIST("Список_доп_дипломов"),
    CERTS_LIST("Список_сертификатов"),
    QUALIFICATION_LIST("Список_квалификаций"),

    // PLACEHOLDERS
    PHOTO_DOCS("Фото_документы"),
    CADASTER_NUMBER("cadasterNumber"),
    OBJECT_ADDRESS("objectAddress"),
    EXPERTISE_MAP("expertiseMapScreenshot");

    private final String bookmarkName;

    Bookmarks(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public String getName() {
        return bookmarkName;
    }
}