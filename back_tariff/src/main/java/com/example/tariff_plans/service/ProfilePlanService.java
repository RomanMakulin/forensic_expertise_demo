package com.example.tariff_plans.service;

import com.example.tariff_plans.model.PlanDto;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления тарифными планами пользователей.
 */
public interface ProfilePlanService {
    /**
     * Получить список всех доступных тарифов
     */
    List<PlanDto> getAllPlans();

    /**
     * Выбрать тариф для пользователя
     */
    void selectPlan(UUID userId, UUID planId);

    /*
      Оплатить выбранный тариф
     */
    //void payForPlan(UUID userId, PaymentDetails paymentDetails);

    /**
     * Получить информацию о текущем тарифе пользователя
     */
    // PlanDto getCurrentPlan(UUID userId);

    /**
     * Продлить подписку на тариф
     */
    // void renewPlan(UUID userId);

    /**
     * Отменить подписку на тариф
     */
    void cancelPlan(UUID profileId);

    /**
     * Уведомить пользователя о статусе его тарифного плана
     */
    void notifyUserAboutPlan(UUID userId);
}
