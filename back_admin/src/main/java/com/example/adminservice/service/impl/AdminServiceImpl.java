package com.example.adminservice.service.impl;

import com.example.adminservice.dto.profile.ProfileDto;
import com.example.adminservice.dto.profile.original.OriginalProfileDto;
import com.example.adminservice.dto.profileCancel.ProfileCancelForProfile;
import com.example.adminservice.dto.profileCancel.ProfileCancelFromFront;
import com.example.adminservice.enums.FileExtension;
import com.example.adminservice.enums.MinioBuckets;
import com.example.adminservice.integration.mail.MailIntegration;
import com.example.adminservice.integration.mail.dto.MailRequest;
import com.example.adminservice.integration.profile.ProfileIntegration;
import com.example.adminservice.mapper.ProfileMapper;
import com.example.adminservice.service.AdminService;
import com.example.adminservice.service.ProfileFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Реализация сервиса работы с админкой
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final ProfileIntegration profileIntegration;
    private final ProfileMapper profileMapper;
    private final ProfileFileService profileFileService;
    private final MailIntegration mailIntegration;

    public AdminServiceImpl(ProfileIntegration profileIntegration,
                            ProfileMapper profileMapper,
                            ProfileFileService profileFileService,
                            MailIntegration mailIntegration) {
        this.profileIntegration = profileIntegration;
        this.profileMapper = profileMapper;
        this.profileFileService = profileFileService;
        this.mailIntegration = mailIntegration;
    }

    /**
     * Получение всех неподтвержденных профилей
     *
     * @return список объектов ProfileDto
     */
    @Override
    public List<ProfileDto> getUnverifiedProfiles() {
        List<OriginalProfileDto> originalProfileDtoList = profileIntegration.requestForUnverifiedProfiles();
        return profileMapper.toProfileDtoList(originalProfileDtoList);
    }

    /**
     * Получение всех профилей
     *
     * @return список объектов ProfileDto
     */
    @Override
    public List<ProfileDto> getAllProfiles() {
        List<OriginalProfileDto> originalProfileDtoList = profileIntegration.requestForAllProfiles();
        return profileMapper.toProfileDtoList(originalProfileDtoList);
    }

    /**
     * Подтверждение валидации профиля
     *
     * @param profileId идентификатор профиля
     */
    @Override
    public void verifyProfile(String profileId) {
        profileIntegration.requestForVerifyProfile(profileId);
    }

    /**
     * Отмена валидации профиля
     *
     * @param profileDto объект с некорректными данными профиля
     */
    @Override
    public void cancelValidationProfile(ProfileCancelFromFront profileDto) {
        String profileId = profileDto.getProfileId();

        // Заполнение данных для отмены верификации в сервисе профиля
        ProfileCancelForProfile profileCancelForProfile = new ProfileCancelForProfile(
                profileId,
                profileDto.getDirections(),
                profileDto.getInstruments(),
                profileDto.getAdditionalDiplomas(),
                profileDto.getCertificates(),
                profileDto.getQualifications(),
                profileDto.getNeedDiplomaDelete());

        // Запрос на отмену валидации профиля
        profileIntegration.requestForCancelVerifyProfile(profileCancelForProfile);

        // Асинхронное удаление отмененных файлов из minIO
        asyncDeleteCancelledFiles(profileDto);

        // Отправка письма пользователю с информацией об отмене верификации
        sendCancelMessage(profileDto);
    }

    /**
     * Отправка письма пользователю с информацией об отмене верификации профиля
     *
     * @param profileDto объект с некорректными данными профиля
     */
    private void sendCancelMessage(ProfileCancelFromFront profileDto) {
        MailRequest mailRequest = new MailRequest(profileDto.getUserMail(), "Ваш профиль не прошел валидацию администратором", cancelMessage(profileDto));
        mailIntegration.sendMail(mailRequest);
    }

    /**
     * Формируем сообщение отмены верификации профиля для отправки на почту
     *
     * @param profileDto объект с некорректными данными профиля
     * @return сообщение для отправки на почту
     */
    private String cancelMessage(ProfileCancelFromFront profileDto) {
        StringBuilder cancelMessage = new StringBuilder("Вы не прошли валидацию администратором.\nОбновите следующие значения:\n");

        Stream.of(
                        new AbstractMap.SimpleEntry<>(Boolean.TRUE.equals(profileDto.getNeedPassportDelete()), "- Паспорт\n"),
                        new AbstractMap.SimpleEntry<>(Boolean.TRUE.equals(profileDto.getNeedDiplomaDelete()), "- Диплом\n"),
                        new AbstractMap.SimpleEntry<>(profileDto.getDirections() != null && !profileDto.getDirections().isEmpty(), "- Направления работы\n"),
                        new AbstractMap.SimpleEntry<>(profileDto.getInstruments() != null && !profileDto.getInstruments().isEmpty(), "- Инструменты\n"),
                        new AbstractMap.SimpleEntry<>(profileDto.getAdditionalDiplomas() != null && !profileDto.getAdditionalDiplomas().isEmpty(), "- Дополнительные дипломы\n"),
                        new AbstractMap.SimpleEntry<>(profileDto.getCertificates() != null && !profileDto.getCertificates().isEmpty(), "- Сертификаты\n"),
                        new AbstractMap.SimpleEntry<>(profileDto.getQualifications() != null && !profileDto.getQualifications().isEmpty(), "- Документы переквалификации\n")
                )
                .filter(Map.Entry::getKey)
                .map(Map.Entry::getValue)
                .forEach(cancelMessage::append);

        return cancelMessage.toString();
    }

    /**
     * Асинхронное удаление отмененных файлов из minIO
     *
     * @param profileDto объект с данными профиля
     */
    private void asyncDeleteCancelledFiles(ProfileCancelFromFront profileDto) {
        String profileId = profileDto.getProfileId();

        // Сохраняем SecurityContext текущего потока
        SecurityContext securityContext = SecurityContextHolder.getContext();

        // Удаление диплома, если указано
        deleteMainFiles(profileDto.getNeedDiplomaDelete(), profileId, securityContext, FileExtension.PDF.extension(), MinioBuckets.USER_DIPLOMS.bucket());

        // Удаление паспорта, если указано
        deleteMainFiles(profileDto.getNeedPassportDelete(), profileId, securityContext, FileExtension.PDF.extension(), MinioBuckets.USER_PASSPORTS.bucket());

        // Удаление дополнительных дипломов, если указано
        deleteAdditionalFiles(profileDto.getAdditionalDiplomas(), securityContext, profileId, FileExtension.PDF.extension(), MinioBuckets.USER_ADDITIONAL_DIPLOMS.bucket());

        // Удаление сертификатов, если указано
        deleteAdditionalFiles(profileDto.getCertificates(), securityContext, profileId, FileExtension.PDF.extension(), MinioBuckets.USER_CERTS.bucket());

        // Удаление сертификатов, если указано
        deleteAdditionalFiles(profileDto.getQualifications(), securityContext, profileId, FileExtension.PDF.extension(), MinioBuckets.USER_QUALIFICATION.bucket());
    }

    /**
     * Общий метод удаления основных файлов (диплома, паспорта) из minIO
     *
     * @param needDelete      флаг необходимости удаления файла
     * @param profileId       идентификатор профиля
     * @param securityContext объект контекста безопасности
     * @param extension       расширение файла
     * @param bucket          название бакета
     */
    private void deleteMainFiles(boolean needDelete, String profileId, SecurityContext securityContext, String extension, String bucket) {
        Optional.ofNullable(needDelete)
                .filter(Boolean::booleanValue)
                .ifPresent(need -> runAsyncWithSecurityContext(securityContext,
                        () -> profileFileService.deleteFile(profileId, extension, bucket)));
    }

    /**
     * Общий метод удаления доп. файлов (списки) из minIO
     *
     * @param filesIds        список идентификаторов файлов
     * @param securityContext объект контекста безопасности
     * @param profileId       идентификатор профиля
     * @param extension       расширение файла
     * @param bucket          название бакета
     */
    private void deleteAdditionalFiles(List<String> filesIds, SecurityContext securityContext, String profileId, String extension, String bucket) {
        Optional.ofNullable(filesIds)
                .filter(files -> !files.isEmpty())
                .ifPresent(qualifications -> qualifications.forEach(fileId -> {
                    runAsyncWithSecurityContext(securityContext,
                            () -> profileFileService.deleteFile(profileId + "_" + fileId, extension, bucket));
                }));
    }

    /**
     * Запускает задачу асинхронно с передачей SecurityContext.
     */
    private void runAsyncWithSecurityContext(SecurityContext securityContext, Runnable task) {
        CompletableFuture.runAsync(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                task.run();
            } finally {
                SecurityContextHolder.clearContext(); // Очищаем контекст после выполнения задачи
            }
        });
    }

}
