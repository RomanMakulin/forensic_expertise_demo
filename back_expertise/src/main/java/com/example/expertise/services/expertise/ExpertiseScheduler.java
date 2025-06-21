package com.example.expertise.services.expertise;

import com.example.expertise.model.expertise.Expertise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Сервис для управления задачами экспертизы.
 */
@Component
public class ExpertiseScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpertiseScheduler.class);
    private final ExpertiseService expertiseService;

    public ExpertiseScheduler(ExpertiseService expertiseService) {
        this.expertiseService = expertiseService;
    }

    /**
     * Задача для удаления неактивных экспертиз.
     * Запускается каждые 4 часа.
     */
    @Scheduled(cron = "0 0 0/4 * * ?") // Каждые 4 часа
    public void deleteInactiveExpertise() {
        List<Expertise> oldExpertise = expertiseService.getAllExpertiseOlderThanTwoDays();
        log.info("Найдено {} неактивных экспертиз", oldExpertise.size());
        for (Expertise expertise : oldExpertise) {
            try {
                expertiseService.deleteExpertise(expertise.getId());
                log.info("Удалена экспертиза с id: {}", expertise.getId());
            } catch (Exception e) {
                log.error("Ошибка при удалении экспертизы с id: {}", expertise.getId(), e);
            }
        }
    }

}
