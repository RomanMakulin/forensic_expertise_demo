package com.example.expertise.services.expertise.document.impl;

import com.example.expertise.dto.profile.*;
import com.example.expertise.enums.Bookmarks;
import com.example.expertise.integration.profile.ProfileIntegration;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.model.expertise.*;
import com.example.expertise.services.expertise.document.BookmarkInserter;
import com.example.expertise.util.DateFormatUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.expertise.services.expertise.document.impl.DocumentProcessorImpl.LINE_SEPARATOR;
import static com.example.expertise.services.expertise.document.impl.DocumentProcessorImpl.QUESTION_SEPARATOR;

/**
 * Сервис для заполнения закладок в экспертизе.
 */
@Component
public class BookmarkInserterImpl implements BookmarkInserter {

    private static final Logger log = LoggerFactory.getLogger(BookmarkInserterImpl.class);

    private final ProfileIntegration profileIntegration;

    public BookmarkInserterImpl(ProfileIntegration profileIntegration) {
        this.profileIntegration = profileIntegration;
    }

    /**
     * Генерирует данные для заполнения закладок.
     *
     * @param expertise Экспертная экспертиза
     * @return Карта данных
     */
    @Override
    public Map<DataFieldName, String> generateVariables(Expertise expertise) {
        Map<DataFieldName, String> variables = new HashMap<>();

        // MERGEFIELDS
        variables.put(new DataFieldName(Bookmarks.END_DATE.getName()), DateFormatUtils.formatDate(expertise.getEndDate()));
        variables.put(new DataFieldName(Bookmarks.START_DATE.getName()), DateFormatUtils.formatDate(expertise.getStartDate()));
        variables.put(new DataFieldName(Bookmarks.EXPERTISE_NAME.getName()), expertise.getName());
        variables.put(new DataFieldName(Bookmarks.RULING_DATE.getName()), DateFormatUtils.formatDate(expertise.getRulingDate()));
        variables.put(new DataFieldName(Bookmarks.COURT_NAME.getName()), expertise.getCourtName());
        variables.put(new DataFieldName(Bookmarks.CASE_NUMBER.getName()), expertise.getCaseNumber());
        variables.put(new DataFieldName(Bookmarks.SIGN_DATE.getName()), DateFormatUtils.formatDate(expertise.getSignDate()));
        variables.put(new DataFieldName(Bookmarks.PRESIDING_JUDGE.getName()), expertise.getPresidingJudge());
        variables.put(new DataFieldName(Bookmarks.PLAINTIFF.getName()), expertise.getPlaintiff());
        variables.put(new DataFieldName(Bookmarks.LOCATION.getName()), expertise.getLocation());
        variables.put(new DataFieldName(Bookmarks.VOLUME_COUNT.getName()), expertise.getVolumeCount());
        variables.put(new DataFieldName(Bookmarks.PARTICIPANTS.getName()), expertise.getParticipants());
        variables.put(new DataFieldName(Bookmarks.INSPECTION_DATE_TIME.getName()), DateFormatUtils.formatDateTime(expertise.getInspectionDateTime()));

        // MERGEFIELDS docs (проставляем по закладкам данные документов)
        ProfileResponseDto profileFilesData = profileIntegration.getProfile(expertise.getProfileId());
        variables.put(new DataFieldName(Bookmarks.MAIN_DIPLOMA.getName()), mainDiploma(profileFilesData.getDiploma()));
        variables.put(new DataFieldName(Bookmarks.ADDITIONAL_DIPLOMA_LIST.getName()), additionalDiplomas(profileFilesData.getAdditionalDiplomas()));
        variables.put(new DataFieldName(Bookmarks.CERTS_LIST.getName()), certs(profileFilesData.getCertificates()));
        variables.put(new DataFieldName(Bookmarks.QUALIFICATION_LIST.getName()), qualifications(profileFilesData.getQualifications()));

        // PLACEHOLDERS
        variables.put(new DataFieldName(Bookmarks.ALL_JUDGES.getName()), allJudges(expertise));
        variables.put(new DataFieldName(Bookmarks.ALL_QUESTIONS.getName()), allQuestions(expertise));
        variables.put(new DataFieldName(Bookmarks.QUESTION_ANSWER_SUMMARY.getName()), questionAnswerSummary(expertise));
        variables.put(new DataFieldName(Bookmarks.QUESTION_CONCLUSION.getName()), questionConclusion(expertise));

        return variables;
    }

    /**
     * Формирование строки с содержанием основного диплома
     *
     * @param diploma данные основного диплома
     * @return строка с содержанием основного диплома
     */
    private String mainDiploma(DiplomaDto diploma) {
        if (diploma == null) return "\"Удалить при остуствии\"";
        return String.format("Диплом № %s от %s по специальности «%s», «%s»",
                diploma.getNumber(),
                diploma.getDate().toString(),
                diploma.getSpecialization(),
                diploma.getOrganization());
    }

    /**
     * Формирование строки с содержанием дополнительных дипломов
     *
     * @param additionalDiplomas данные дополнительных дипломов
     * @return строка с содержанием дополнительных дипломов
     */
    private String additionalDiplomas(List<AdditionalDiplomaDto> additionalDiplomas) {
        if (additionalDiplomas == null || additionalDiplomas.isEmpty()) return "\"Удалить при остуствии\"";
        return additionalDiplomas.stream()
                .map(diploma -> String.format("Диплом № %s от %s по специальности «%s», «%s»",
                        diploma.getNumber(),
                        diploma.getIssueDate().toString(),
                        diploma.getSpecialty(),
                        diploma.getInstitution()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Формирование строки с содержанием сертификатов
     *
     * @param certificates данные сертификатов
     * @return строка с содержанием сертификатов
     */
    private String certs(List<CertificateDto> certificates) {
        if (certificates == null || certificates.isEmpty()) return "\"Удалить при остуствии\"";
        return certificates.stream()
                .map(cert -> String.format("Сертификат «%s» %s от %s выдан «%s»",
                        cert.getName(),
                        cert.getNumber(),
                        cert.getIssueDate().toString(),
                        cert.getOrganization()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Формирование строки с содержанием квалификаций
     *
     * @param qualifications данные квалификаций
     * @return строка с содержанием квалификаций
     */
    private String qualifications(List<QualificationDto> qualifications) {
        if (qualifications == null || qualifications.isEmpty()) return "\"Удалить при остуствии\"";
        return qualifications.stream()
                .map(qual -> String.format("Удостоверение о повышении квалификации № %s от %s по «%s», «%s»",
                        qual.getNumber(),
                        qual.getIssueDate().toString(),
                        qual.getCourseName(),
                        qual.getInstitution()))
                .collect(Collectors.joining("\n"));
    }


    /**
     * Получение строчки со всеми судьями
     *
     * @param expertise экспертиза.
     * @return строка, содержащая имена всех судей экспертизы.
     */
    private String allJudges(Expertise expertise) {
        return expertise.getExpertiseJudges().stream()
                .map(ExpertiseJudge::getFullName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Получение строчки со всеми вопросами экспертизы.
     *
     * @param expertise экспертиза
     * @return строка со всеми вопросами экспертизы.
     */
    private String allQuestions(Expertise expertise) {
        List<String> questions = expertise.getQuestions().stream()
                .map(ExpertiseQuestion::getQuestionText)
                .collect(Collectors.toList());

        return IntStream.range(0, questions.size())
                .mapToObj(i -> "Вопрос № " + (i + 1) + ": " + questions.get(i))
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Формирование строки по принципу вопрос-ответ-резюме
     *
     * @param expertise экспертиза
     * @return строка формата вопрос-ответ-резюме
     */
    private String questionAnswerSummary(Expertise expertise) {
        StringBuilder result = new StringBuilder();
        List<ExpertiseQuestion> questions = expertise.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) {
                result.append(QUESTION_SEPARATOR);
            }
            result.append(formatQuestionAnswer(questions.get(i), i + 1));
        }
        log.info("Question answer summary: {}", result.toString());
        return result.toString();
    }

    /**
     * Формирование строки по принципу вопрос-вывод
     *
     * @param expertise экспертиза
     * @return строка формата вопрос-вывод
     */
    private String questionConclusion(Expertise expertise) {
        StringBuilder result = new StringBuilder();
        List<ExpertiseQuestion> questions = expertise.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) {
                result.append(QUESTION_SEPARATOR);
            }
            result.append(formatQuestionConclusion(questions.get(i), i + 1));
        }
        return result.toString();
    }

    /**
     * Форматирование вопроса, ответа и резюме.
     * + Добавление placeholder для изображений.
     *
     * @param question       вопрос экспертизы
     * @param questionNumber номер вопроса
     * @return форматированная строка
     */
    private String formatQuestionAnswer(ExpertiseQuestion question, int questionNumber) {
        StringBuilder result = new StringBuilder();

        // Вопрос
        result.append(String.format("Вопрос № %d: %s%s", questionNumber, question.getQuestionText(), LINE_SEPARATOR));

        // Ответ (с проверкой на null или пустую строку)
        String answer = question.getAnswer() != null && !question.getAnswer().trim().isEmpty()
                ? question.getAnswer()
                : "Нет ответа на вопрос";
        result.append(String.format("Ответ на вопрос № %d: %s%s", questionNumber, LINE_SEPARATOR, answer));

        // Чек-листы
        if (question.getChecklistInstances() != null && !question.getChecklistInstances().isEmpty()) {
            for (ChecklistInstance checklistInstance : question.getChecklistInstances()) {
                result.append(String.format("%s[CHECKLIST_%s]", LINE_SEPARATOR, checklistInstance.getId()));
            }
        }

        // Фотографии
        if (question.getPhotos() != null && !question.getPhotos().isEmpty()) {
            for (ExpertisePhoto photo : question.getPhotos()) {
                result.append(String.format("%s[PHOTO_%s]", LINE_SEPARATOR, photo.getId()));
            }
        }

        // Резюме
        result.append(LINE_SEPARATOR)
                .append(String.format("Резюмируя проведенное исследование по %s вопросу, экспертом установлено:%s%s",
                        numberToOrdinalRussian(questionNumber), LINE_SEPARATOR, question.getAnswerConclusion()));

        log.info("Строка с вопросом: {}", result.toString());
        return result.toString();
    }

    /**
     * Форматирование вопрос-вывод
     *
     * @param question       вопрос экспертизы
     * @param questionNumber номер вопроса
     * @return сформированная строка
     */
    private String formatQuestionConclusion(ExpertiseQuestion question, int questionNumber) {
        return String.format("Вопрос № %d: %s%s", questionNumber, question.getQuestionText(), LINE_SEPARATOR) +
                String.format("Вывод к вопросу № %d: %s%s", questionNumber, LINE_SEPARATOR, question.getAnswerConclusion());
    }

    /**
     * Преобразование числа в порядковое числительное на русском языке (в дательном падеже) до 30.
     */
    private String numberToOrdinalRussian(int number) {
        switch (number) {
            case 1:
                return "первому";
            case 2:
                return "второму";
            case 3:
                return "третьему";
            case 4:
                return "четвертому";
            case 5:
                return "пятому";
            case 6:
                return "шестому";
            case 7:
                return "седьмому";
            case 8:
                return "восьмому";
            case 9:
                return "девятому";
            case 10:
                return "десятому";
            case 11:
                return "одиннадцатому";
            case 12:
                return "двенадцатому";
            case 13:
                return "тринадцатому";
            case 14:
                return "четырнадцатому";
            case 15:
                return "пятнадцатому";
            case 16:
                return "шестнадцатому";
            case 17:
                return "семнадцатому";
            case 18:
                return "восемнадцатому";
            case 19:
                return "девятнадцатому";
            case 20:
                return "двадцатому";
            case 21:
                return "двадцать первому";
            case 22:
                return "двадцать второму";
            case 23:
                return "двадцать третьему";
            case 24:
                return "двадцать четвертому";
            case 25:
                return "двадцать пятому";
            case 26:
                return "двадцать шестому";
            case 27:
                return "двадцать седьмому";
            case 28:
                return "двадцать восьмому";
            case 29:
                return "двадцать девятому";
            case 30:
                return "тридцатому";
            default:
                return number + "-му"; // Для чисел > 30
        }
    }
}