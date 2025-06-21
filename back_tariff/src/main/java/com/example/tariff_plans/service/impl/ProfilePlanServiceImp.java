package com.example.tariff_plans.service.impl;

import com.example.tariff_plans.integration.profile.ProfileIntegration;
import com.example.tariff_plans.mapper.PlanMapper;
import com.example.tariff_plans.model.Plan;
import com.example.tariff_plans.model.PlanDto;
import com.example.tariff_plans.repository.PlanRepository;
import com.example.tariff_plans.service.ProfilePlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ProfilePlanServiceImp implements ProfilePlanService {

    private static final Logger log = LoggerFactory.getLogger(ProfilePlanServiceImp.class);

    private final PlanRepository planRepository;

    private final ProfileIntegration profileIntegration;

    public ProfilePlanServiceImp(PlanRepository planRepository, ProfileIntegration profileIntegration) {
        this.planRepository = planRepository;
        this.profileIntegration = profileIntegration;
    }


    @Override
    public List<PlanDto> getAllPlans() {
        List<Plan> plans = planRepository.findAll();
        return plans.stream()
                .map(PlanMapper.INSTANCE::planToPlanDto)
                .collect(Collectors.toList());
    }

    @Override
    public void selectPlan(UUID userId, UUID planId) {
        // используем сервис интеграции для отправки запроса в модуль профиля
        profileIntegration.chargeTariffRequest(userId, planId);
    }


   /* @Override
    public PlanDto getCurrentPlan(UUID userId) {
        return null;
    }

    @Override
    public void renewPlan(UUID userId) {

    }*/

    @Override
    public void cancelPlan(UUID profileId) {
        try {
            // Вызываем метод интеграции для удаления тарифного плана у профиля
            profileIntegration.deletePlanRequest(profileId);
            log.info("Tariff plan was successfully canceled for profile: {}", profileId);
        } catch (RuntimeException e) {
            log.error("Error while canceling tariff plan for profile: {}", profileId, e);
            throw new RuntimeException("Error while canceling tariff plan", e);
        }
    }


    @Override
    public void notifyUserAboutPlan(UUID userId) {

    }


}