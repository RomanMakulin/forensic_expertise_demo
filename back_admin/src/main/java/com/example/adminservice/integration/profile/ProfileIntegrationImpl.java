package com.example.adminservice.integration.profile;

import com.example.adminservice.dto.profile.original.OriginalProfileDto;
import com.example.adminservice.dto.profileCancel.ProfileCancelForProfile;
import com.example.adminservice.config.AppConfig;
import com.example.adminservice.integration.IntegrationHelper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реализация сервиса для взаимодействия с модулем профилей
 */
@Service
public class ProfileIntegrationImpl implements ProfileIntegration {

    private final IntegrationHelper integrationHelper;
    private final AppConfig appConfig;

    public ProfileIntegrationImpl(IntegrationHelper integrationHelper,
                                  AppConfig appConfig) {
        this.integrationHelper = integrationHelper;
        this.appConfig = appConfig;
    }

    /**
     * Отправка запроса на получение всех профилей
     *
     * @return - список профилей
     */
    @Override
    public List<OriginalProfileDto> requestForAllProfiles() {
        return getProfilesProcess("get-all-profiles");
    }

    /**
     * Отправка запроса на получение непроверенных профилей
     *
     * @return - список профилей
     */
    @Override
    public List<OriginalProfileDto> requestForUnverifiedProfiles() {
        return getProfilesProcess("get-unverified-profiles");
    }

    /**
     * Общая реализация отправки запроса на получение профилей
     *
     * @param passKey - ключ урла для запроса из проперти
     * @return - список профилей
     */
    private List<OriginalProfileDto> getProfilesProcess(String passKey) {
        String pathApi = appConfig.getPaths().getProfile().get(passKey);
        return integrationHelper.sendRequest(
                pathApi,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OriginalProfileDto>>() {}
        );
    }

    /**
     * Отправка запроса на верификацию профиля
     *
     * @param profileId - ID профиля
     */
    @Override
    public void requestForVerifyProfile(String profileId) {
        String pathApi = appConfig.getPaths().getProfile().get("verify-profile");
        String urlWithId = String.format("%s/%s", pathApi, profileId);

        integrationHelper.sendRequest(
                urlWithId,
                HttpMethod.GET,
                null,
                Void.class);
    }

    /**
     * Отправка запроса на отмену верификации профиля
     *
     * @param profileDto - некорректные данные профиля
     */
    @Override
    public void requestForCancelVerifyProfile(ProfileCancelForProfile profileDto) {
        String pathApi = appConfig.getPaths().getProfile().get("cancel-validation");

        integrationHelper.sendRequest(
                pathApi,
                HttpMethod.POST,
                profileDto,
                Void.class);
    }

}
