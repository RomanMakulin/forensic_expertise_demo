package com.example.tariff_plans.integration.profile;

import java.util.UUID;

/**
 * Интерфейс интеграции с модулем профиля
 */
public interface ProfileIntegration {

    /**
     * Запрос на назначение тарифа конкретному профилю
     *
     * @param profileId ID профиля
     * @param tariffId  ID тарифа
     */
    void chargeTariffRequest(UUID profileId, UUID tariffId);

    /**
     * Запрос на назначение тарифа конкретному профилю
     *
     * @param profileId ID профиля
     */
    void deletePlanRequest(UUID profileId);


}
