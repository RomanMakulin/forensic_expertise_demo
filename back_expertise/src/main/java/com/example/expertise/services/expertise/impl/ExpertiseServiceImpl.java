package com.example.expertise.services.expertise.impl;

import com.example.expertise.dto.expertise.CreateExpertiseDto;
import com.example.expertise.dto.expertise.ExpertiseResponseDto;
import com.example.expertise.enums.FileExtension;
import com.example.expertise.enums.FileType;
import com.example.expertise.enums.MinioBuckets;
import com.example.expertise.exceptions.ExpertiseNotFoundException;
import com.example.expertise.integration.minio.MinioIntegration;
import com.example.expertise.integration.profile.ProfileIntegration;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.model.expertise.Expertise;
import com.example.expertise.repository.expertise.ExpertiseRepository;
import com.example.expertise.services.cache.ChecklistTemplateCache;
import com.example.expertise.services.expertise.checklists.ChecklistsService;
import com.example.expertise.services.expertise.ExpertiseService;
import com.example.expertise.services.expertise.document.BookmarkInserter;
import com.example.expertise.services.expertise.document.DocumentProcessor;
import com.example.expertise.services.expertise.document.impl.BookmarkInserterImpl;
import com.example.expertise.util.mappers.ChecklistInstanceMapper;
import com.example.expertise.util.mappers.ExpertiseMapper;
import com.example.expertise.services.expertise.document.impl.DocumentProcessorImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Сервис для работы с сущностью Expertise.
 */
@Service
@Validated
@AllArgsConstructor
public class ExpertiseServiceImpl implements ExpertiseService {

    private static final Logger log = LoggerFactory.getLogger(ExpertiseServiceImpl.class);

    private final ExpertiseRepository expertiseRepository;
    private final ExpertiseMapper expertiseMapper;
    private final MinioIntegration minioIntegration;
    private final BookmarkInserter bookmarkInserter;
    private final ProfileIntegration profileIntegration;
    private final ChecklistInstanceMapper checklistInstanceMapper;
    private final DocumentProcessor documentProcessorImpl;
    private final ChecklistTemplateCache checklistTemplateCache;
    private final ObjectMapper objectMapper;
    private final ChecklistsService checklistsService;

    /**
     * Получить все текущие экспертизы.
     *
     * @return список всех экспертиз
     */
    @Override
    public List<Expertise> getAllExpertise() {
        return expertiseRepository.findAll();
    }

    /**
     * Получить все экспертизы, которые были созданы более чем 2 дня назад.
     *
     * @return список всех экспертиз, которые были созданы более чем 2 дня назад
     */
    @Override
    public List<Expertise> getAllExpertiseOlderThanTwoDays() {
        return expertiseRepository.findAllOlderThanTwoDays();
    }

    /**
     * Создает новую экспертизу для заданного уникального идентификатора эксперта.
     *
     * @param createExpertiseDto данные для создания экспертизы
     */
    @Transactional
    @Override
    public ExpertiseResponseDto createNewExpertise(CreateExpertiseDto createExpertiseDto) {
        Expertise newExpertise = new Expertise();
        expertiseMapper.updateExpertiseFromDto(createExpertiseDto, newExpertise);
        expertiseRepository.save(newExpertise);

        return createExpertiseResponseDto(newExpertise);
    }

    /**
     * Находит экспертизу по уникальному идентификатору эксперта.
     *
     * @param expertiseId уникальный идентификатор экспертизы
     * @return найденная экспертиза
     */
    @Override
    public Expertise getExpertiseById(@NotNull(message = "expertiseId cannot be null") UUID expertiseId) {
        return expertiseRepository.findById(expertiseId)
                .orElseThrow(ExpertiseNotFoundException::new);
    }

    @Override
    public ExpertiseResponseDto getExpertiseResponseDtoById(@NotNull(message = "expertiseId cannot be null") UUID expertiseId) {
        Expertise expertise = getExpertiseById(expertiseId);
        return createExpertiseResponseDto(expertise);
    }

    /**
     * Получает список экспертиз по id профиля
     *
     * @param profileId id профиля
     * @return список экспертиз
     */
    @Override
    public List<Expertise> getExpertiseByProfileId(@NotNull(message = "profileId cannot be null") UUID profileId) {
        return expertiseRepository.findAllByProfileId(profileId);
    }

    /**
     * Получает список экспертиз по id профиля в формате ExpertiseResponseDto
     *
     * @param profileId id профиля
     * @return список экспертиз в формате ExpertiseResponseDto
     */
    @Override
    public List<ExpertiseResponseDto> getExpertiseResponseDtoByProfileId(@NotNull(message = "profileId cannot be null") UUID profileId) {
        List<Expertise> expertiseList = getExpertiseByProfileId(profileId);
        List<ExpertiseResponseDto> expertiseResponseDtoList = new ArrayList<>();

        expertiseList.forEach(expertise -> expertiseResponseDtoList.add(createExpertiseResponseDto(expertise)));
        return expertiseResponseDtoList;
    }

    /**
     * Удаляет экспертизу по уникальному идентификатору эксперта.
     *
     * @param expertiseId уникальный идентификатор эксперта
     */
    @Transactional
    @Override
    public void deleteExpertise(@NotNull(message = "expertiseId cannot be null") UUID expertiseId) {
        // Удалить все связанные чек-листы и файлы
        deleteAllLinkedChecklists(expertiseId);

        // Удаляем фотографии из MinIO
        log.info("Deleting photos from MinIO for expertiseId: {}", expertiseId);
        minioIntegration.deleteAllAnswerPhotos(expertiseId);

        // Удаляем Expertise
        log.info("Deleting expertise from database: {}", expertiseId);
        expertiseRepository.deleteByIdDirect(expertiseId);

        log.info("Expertise deleted successfully: {}", expertiseId);
    }

    /**
     * Удалить все связанные с экспертизой чек-листы и файлы
     *
     * @param expertiseId ID экспертизы
     */
    private void deleteAllLinkedChecklists(UUID expertiseId) {
        Expertise expertise = getExpertiseById(expertiseId);
        Optional.ofNullable(expertise.getQuestions())
                .orElse(Collections.emptyList()).stream()
                .flatMap(question -> Optional.ofNullable(question.getChecklistInstances())
                        .orElse(Collections.emptyList())
                        .stream())
                .map(ChecklistInstance::getId)
                .forEach(checklistsService::deleteChecklistInstanceById);
    }

    /**
     * Генерирует файл экспертной экспертизы по заданному ID экспертизы
     *
     * @param expertiseId ID экспертизы
     * @return файл экспертной экспертизы
     */
    @Override
    public byte[] generateExpertiseFile(UUID expertiseId, MultipartFile mapScreen) {
        Expertise expertise = getExpertiseById(expertiseId);

        Map<DataFieldName, String> mergeData = bookmarkInserter.generateVariables(expertise);
        Map<String, byte[]> photoMap = preparePhotoMap(expertise);
        List<byte[]> photoDocs = preparePhotoDocs(expertise);

        try (InputStream templateStream = minioIntegration.getExpertiseFile(expertise.getTemplateName()).getInputStream()) {
            WordprocessingMLPackage template = WordprocessingMLPackage.load(templateStream);
            return documentProcessorImpl.processDocument(template, mergeData, photoMap, photoDocs, expertise.getQuestions(), mapScreen);
        } catch (IOException | Docx4JException e) {
            log.error("Ошибка при обработке файла DOCX для экспертизы с ID: {}", expertiseId, e);
            throw new RuntimeException("Не удалось сгенерировать файл экспертизы", e);
        } catch (Exception e) {
            log.error("Неизвестная ошибка при обработке файла DOCX для экспертизы с ID: {}", expertiseId, e);
            throw new RuntimeException("Не удалось сгенерировать файл экспертизы", e);
        }
    }

    /**
     * Создает объект dto экспертизы на основании сущности Expertise.
     *
     * @param expertise экспертиза
     * @return объект dto экспертизы
     */
    private ExpertiseResponseDto createExpertiseResponseDto(Expertise expertise) {
        ExpertiseResponseDto expertiseResponseDto = new ExpertiseResponseDto(expertise, checklistInstanceMapper, objectMapper);

        // Для каждого вопроса устанавливаем fieldNameCache для чек-листов
        if (expertiseResponseDto.getQuestions() != null) {
            expertiseResponseDto.getQuestions().forEach(question -> {
                if (question.getChecklistInstances() != null) {
                    question.getChecklistInstances().forEach(checklist -> {
                        // Получаем fieldNameCache по ID шаблона
                        UUID templateId = checklist.getTemplateId();
                        Map<String, String> fieldNameCache = checklistTemplateCache.getFieldNameMap(templateId);
                        checklist.setFieldNameCache(fieldNameCache);
                    });
                }
            });
        }
        return expertiseResponseDto;
    }

    /**
     * Подготавливает список документов фотографий для обработки файла DOCX
     * ВНИМАНИЕ: Текущий формат возвращаемых документов фотографий - PDF
     *
     * @param expertise экспертиза
     * @return список документов фотографий для обработки файла DOCX. ФОРМАТ PDF
     */
    private List<byte[]> preparePhotoDocs(Expertise expertise) {
        String profileId = expertise.getProfileId().toString();

        // Получаем список ID документов
        List<String> additionalDiplomasIds = profileIntegration.getFileIdsList(profileId, FileType.ADDITIONAL_DIPLOMA.type());
        List<String> certificatesIds = profileIntegration.getFileIdsList(profileId, FileType.CERTIFICATE.type());
        List<String> qualificationsIds = profileIntegration.getFileIdsList(profileId, FileType.QUALIFICATION_CERTIFICATE.type());

        // Преобразуем список ID в имена файлов
        List<String> additionalDiplomasNames = additionalDiplomasIds.stream().map(diplomaId -> profileId + "_" + diplomaId).toList();
        List<String> certificatesNames = certificatesIds.stream().map(certificateId -> profileId + "_" + certificateId).toList();
        List<String> qualificationsNames = qualificationsIds.stream().map(qualificationId -> profileId + "_" + qualificationId).toList();

        // Общий список документов (потокобезопасный)
        List<byte[]> allDocs = Collections.synchronizedList(new ArrayList<>());

        // Общий пул потоков и контекст запроса
        Executor executor = Executors.newFixedThreadPool(8); // Максимум 8 потоков
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // Параллельно загружаем файлы из всех категорий
        CompletableFuture<Void> diplomaFuture = CompletableFuture.runAsync(() ->
                putBytesData(profileId, allDocs, MinioBuckets.USER_DIPLOMS.bucket(), requestAttributes), executor);

        CompletableFuture<Void> additionalDiplomasFuture = CompletableFuture.runAsync(() ->
                putBytesDataParallel(additionalDiplomasNames, allDocs, MinioBuckets.USER_ADDITIONAL_DIPLOMS.bucket(), executor, requestAttributes), executor);

        CompletableFuture<Void> certificatesFuture = CompletableFuture.runAsync(() ->
                putBytesDataParallel(certificatesNames, allDocs, MinioBuckets.USER_CERTS.bucket(), executor, requestAttributes), executor);

        CompletableFuture<Void> qualificationsFuture = CompletableFuture.runAsync(() ->
                putBytesDataParallel(qualificationsNames, allDocs, MinioBuckets.USER_QUALIFICATION.bucket(), executor, requestAttributes), executor);

        // Ждем завершения всех задач
        CompletableFuture.allOf(diplomaFuture, additionalDiplomasFuture, certificatesFuture, qualificationsFuture).join();

        return allDocs.isEmpty() ? Collections.emptyList() : allDocs;
    }

    /**
     * Загружает файлы из MinIO в параллельных потоках и добавляет их в общий список
     *
     * @param fileNames         список имен файлов
     * @param allDocs           общий список всех документов
     * @param bucket            название бакета
     * @param executor          общий пул потоков
     * @param requestAttributes контекст запроса
     */
    private void putBytesDataParallel(List<String> fileNames, List<byte[]> allDocs, String bucket, Executor executor, RequestAttributes requestAttributes) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        List<CompletableFuture<byte[]>> futures = fileNames.stream()
                .map(fileName -> CompletableFuture.supplyAsync(() -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    try {
                        byte[] byteFile = minioIntegration.getFileByParams(fileName, FileExtension.PDF.extension(), bucket);
                        if (byteFile != null) {
                            allDocs.add(byteFile);
                        }
                        return byteFile;
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                }, executor))
                .toList();

        futures.forEach(CompletableFuture::join);
    }

    /**
     * Загружает один файл из MinIO и добавляет его в общий список
     *
     * @param fileName          имя файла
     * @param allDocs           общий список всех документов
     * @param bucket            название бакета
     * @param requestAttributes контекст запроса
     */
    private void putBytesData(String fileName, List<byte[]> allDocs, String bucket, RequestAttributes requestAttributes) {
        RequestContextHolder.setRequestAttributes(requestAttributes);
        try {
            byte[] fileBytes = minioIntegration.getFileByParams(fileName, FileExtension.PDF.extension(), bucket);
            if (fileBytes != null) {
                allDocs.add(fileBytes);
            }
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     * Подготавливает карту фотографий для обработки файла DOCX
     *
     * @param expertise экспертиза
     * @return карта фотографий для обработки файла DOCX
     */
    private Map<String, byte[]> preparePhotoMap(Expertise expertise) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes(); // Сохраняем текущий RequestContext

        int parallelism = 10; // Ограничиваем до 10 параллельных потоков
        ForkJoinPool customThreadPool = new ForkJoinPool(parallelism);
        Map<String, byte[]> photoMap = new ConcurrentHashMap<>();

        try {
            customThreadPool.submit(() -> {
                expertise.getQuestions().stream()
                        .flatMap(question -> question.getPhotos().stream())
                        .parallel()
                        .forEach(photo -> {
                            RequestContextHolder.setRequestAttributes(requestAttributes); // Устанавливаем RequestContext для текущего потока
                            try {
                                byte[] imageBytes = minioIntegration.getExpertisePhotoAsBytes(photo.getFilePath());
                                if (imageBytes != null && imageBytes.length > 0) {
                                    photoMap.put("[PHOTO_" + photo.getId() + "]", imageBytes);
                                }
                            } catch (Exception e) {
                                log.error("Ошибка при получении фото из MinIO для пути {}: {}", photo.getFilePath(), e.getMessage());
                            } finally {
                                RequestContextHolder.resetRequestAttributes(); // Очищаем RequestContext после выполнения задачи
                            }
                        });
            }).get(); // Ожидаем завершения всех задач
        } catch (InterruptedException | ExecutionException e) {
            log.error("Ошибка при параллельной обработке фотографий: {}", e.getMessage());
            Thread.currentThread().interrupt(); // Восстанавливаем прерванный статус
        } finally {
            customThreadPool.shutdown();
        }

        return photoMap;
    }
}