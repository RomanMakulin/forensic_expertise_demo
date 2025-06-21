package com.example.adminservice.mapper;

import com.example.adminservice.dto.profile.*;
import com.example.adminservice.dto.profile.original.OriginalAdditionalDiplomaDto;
import com.example.adminservice.dto.profile.original.OriginalCertificateDto;
import com.example.adminservice.dto.profile.original.OriginalProfileDto;
import com.example.adminservice.dto.profile.original.OriginalQualificationDto;
import com.example.adminservice.enums.FileExtension;
import com.example.adminservice.enums.MinioBuckets;
import com.example.adminservice.service.ProfileFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Маппер профиля в DTO для отправки клиенту
 */
@Component
public class ProfileMapper {

    private static final Logger log = LoggerFactory.getLogger(ProfileMapper.class);
    private final ProfileFileService profileFileService;
    private final ExecutorService executorService = new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(3));

    public ProfileMapper(ProfileFileService profileFileService) {
        this.profileFileService = profileFileService;
    }

    /**
     * Маппер профиля в список DTO
     */
    public List<ProfileDto> toProfileDtoList(List<OriginalProfileDto> originalProfileDtoList) {
        return originalProfileDtoList.stream()
                .map(this::toProfileDto)
                .collect(Collectors.toList());
    }

    /**
     * Маппер профиля в DTO
     */
    public ProfileDto toProfileDto(OriginalProfileDto originalProfile) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setId(originalProfile.getId());
        profileDto.setUser(toUserDto(originalProfile));
        profileDto.setPhone(originalProfile.getPhone());
        profileDto.setLocation(toLocationDto(originalProfile));
        profileDto.setProfileStatus(toStatusDto(originalProfile));
        profileDto.setDirections(toDirectionDtoSet(originalProfile));
        profileDto.setInstruments(originalProfile.getInstruments());
        profileDto.setBirthday(originalProfile.getBirthday());

        String profileIdStr = String.valueOf(originalProfile.getId());

        // Асинхронный маппинг основных файлов
        Map<String, CompletableFuture<String>> fileFutures = fetchMainFiles(profileIdStr);
        CompletableFuture.allOf(fileFutures.values().toArray(new CompletableFuture[0])).join();

        try {
            profileDto.setPhoto(fileFutures.get("photo").get());
            profileDto.setPassport(fileFutures.get("passport").get());
            profileDto.setDiplom(fileFutures.get("diplom").get());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Ошибка получения основного файла: ", e);
        }

        // Асинхронный маппинг дополнительных файлов
        CompletableFuture<List<CertificateDto>> certificatesFuture = mapCertificatesAsync(originalProfile.getCertificates(), profileIdStr);
        CompletableFuture<List<QualificationDto>> qualificationsFuture = mapQualificationsAsync(originalProfile.getQualifications(), profileIdStr);
        CompletableFuture<List<AdditionalDiplomaDto>> additionalDiplomasFuture = mapAdditionalDiplomasAsync(originalProfile.getAdditionalDiplomas(), profileIdStr);

        CompletableFuture.allOf(certificatesFuture, qualificationsFuture, additionalDiplomasFuture).join();

        try {
            profileDto.setCertificates(certificatesFuture.get());
            profileDto.setQualifications(qualificationsFuture.get());
            profileDto.setAdditionalDiplomas(additionalDiplomasFuture.get());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Ошибка получения дополнительных файлов: ", e);
        }

        return profileDto;
    }

    /**
     * Асинхронный маппинг списка сертификатов
     */
    private CompletableFuture<List<CertificateDto>> mapCertificatesAsync(List<OriginalCertificateDto> originals, String profileId) {
        if (originals == null) return CompletableFuture.completedFuture(Collections.emptyList());

        List<CompletableFuture<CertificateDto>> futures = originals.stream()
                .map(original -> CompletableFuture.supplyAsync(() -> {
                    CertificateDto dto = new CertificateDto();
                    dto.setId(original.getId());
                    dto.setName(original.getName());
                    dto.setIssueDate(original.getIssueDate());
                    dto.setOrganization(original.getOrganization());
                    dto.setNumber(original.getNumber());
                    dto.setLink(profileFileService.getFileLink(
                            profileId + "_" + original.getId(),
                            FileExtension.PDF.extension(),
                            MinioBuckets.USER_CERTS.bucket()));
                    return dto;
                }, executorService))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                log.error("Ошибка маппинга сертификата: ", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    /**
     * Асинхронный маппинг списка квалификаций
     */
    private CompletableFuture<List<QualificationDto>> mapQualificationsAsync(List<OriginalQualificationDto> originals, String profileId) {
        if (originals == null) return CompletableFuture.completedFuture(Collections.emptyList());

        List<CompletableFuture<QualificationDto>> futures = originals.stream()
                .map(original -> CompletableFuture.supplyAsync(() -> {
                    QualificationDto dto = new QualificationDto();
                    dto.setId(original.getId());
                    dto.setCourseName(original.getCourseName());
                    dto.setIssueDate(original.getIssueDate());
                    dto.setInstitution(original.getInstitution());
                    dto.setNumber(original.getNumber());
                    dto.setLink(profileFileService.getFileLink(
                            profileId + "_" + original.getId(),
                            FileExtension.PDF.extension(),
                            MinioBuckets.USER_QUALIFICATION.bucket()));
                    return dto;
                }, executorService))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                log.error("Ошибка маппинга квалификации: ", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    /**
     * Асинхронный маппинг списка дополнительных дипломов
     */
    private CompletableFuture<List<AdditionalDiplomaDto>> mapAdditionalDiplomasAsync(List<OriginalAdditionalDiplomaDto> originals, String profileId) {
        if (originals == null) return CompletableFuture.completedFuture(Collections.emptyList());

        List<CompletableFuture<AdditionalDiplomaDto>> futures = originals.stream()
                .map(original -> CompletableFuture.supplyAsync(() -> {
                    AdditionalDiplomaDto dto = new AdditionalDiplomaDto();
                    dto.setId(original.getId());
                    dto.setNumber(original.getNumber());
                    dto.setIssueDate(original.getIssueDate());
                    dto.setInstitution(original.getInstitution());
                    dto.setSpecialty(original.getSpecialty());
                    dto.setDegree(original.getDegree());
                    dto.setLink(profileFileService.getFileLink(
                            profileId + "_" + original.getId(),
                            FileExtension.PDF.extension(),
                            MinioBuckets.USER_ADDITIONAL_DIPLOMS.bucket()));
                    return dto;
                }, executorService))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                log.error("Ошибка маппинга дополнительного диплома: ", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    /**
     * Асинхронное получение основных файлов
     */
    private Map<String, CompletableFuture<String>> fetchMainFiles(String profileId) {
        Map<String, CompletableFuture<String>> futures = new HashMap<>();
        futures.put("photo", fetchFileAsync(() -> profileFileService.getFileLink(
                profileId, FileExtension.JPG.extension(), MinioBuckets.USER_AVATARS.bucket())));
        futures.put("passport", fetchFileAsync(() -> profileFileService.getFileLink(
                profileId, FileExtension.PDF.extension(), MinioBuckets.USER_PASSPORTS.bucket())));
        futures.put("diplom", fetchFileAsync(() -> profileFileService.getFileLink(
                profileId, FileExtension.PDF.extension(), MinioBuckets.USER_DIPLOMS.bucket())));
        return futures;
    }

    /**
     * Общая реализация асинхронного получения файла
     */
    private CompletableFuture<String> fetchFileAsync(Supplier<String> supplier) {
        return CompletableFuture.supplyAsync(supplier, executorService)
                .exceptionally(ex -> {
                    log.error("Ошибка получения файла: {}", ex.getMessage());
                    return null;
                });
    }

    /**
     * Маппер пользователя в DTO
     *
     * @param originalProfile профиль пользователя
     * @return дто пользователя
     */
    private UserDto toUserDto(OriginalProfileDto originalProfile) {
        if (originalProfile.getAppUser() == null) return null;
        UserDto userDto = new UserDto();
        userDto.setEmail(originalProfile.getAppUser().getEmail());
        userDto.setFullName(originalProfile.getAppUser().getFullName());
        userDto.setRegistrationDate(originalProfile.getAppUser().getRegistrationDate());
        userDto.setRole(originalProfile.getAppUser().getRole() != null ? originalProfile.getAppUser().getRole().getName() : null);
        return userDto;
    }

    /**
     * Маппер местоположения в DTO
     *
     * @param originalProfile профиль пользователя
     * @return дто местоположения
     */
    private LocationDto toLocationDto(OriginalProfileDto originalProfile) {
        if (originalProfile.getLocation() == null) return null;
        LocationDto locationDto = new LocationDto();
        locationDto.setAddress(originalProfile.getLocation().getAddress());
        locationDto.setCity(originalProfile.getLocation().getCity());
        locationDto.setCountry(originalProfile.getLocation().getCountry());
        locationDto.setRegion(originalProfile.getLocation().getRegion());
        return locationDto;
    }

    /**
     * Маппер статуса профиля в DTO
     *
     * @param originalProfile профиль пользователя
     * @return дто статуса профиля
     */
    private StatusDto toStatusDto(OriginalProfileDto originalProfile) {
        if (originalProfile.getStatus() == null) return null;
        StatusDto statusDto = new StatusDto();
        statusDto.setActivityStatus(originalProfile.getStatus().getActivityStatus());
        statusDto.setVerificationResult(originalProfile.getStatus().getVerificationResult());
        return statusDto;
    }

    /**
     * Маппер направлений в DTO
     *
     * @param originalProfile профиль пользователя
     * @return дто направлений
     */
    private Set<DirectionDto> toDirectionDtoSet(OriginalProfileDto originalProfile) {
        if (originalProfile.getDirections() == null) return new HashSet<>();
        return originalProfile.getDirections().stream()
                .map(direction -> new DirectionDto(direction.getId(), direction.getName()))
                .collect(Collectors.toSet());
    }
}