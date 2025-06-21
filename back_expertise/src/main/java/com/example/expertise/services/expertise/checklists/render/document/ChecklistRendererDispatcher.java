package com.example.expertise.services.expertise.checklists.render.document;

import com.example.expertise.services.expertise.checklists.render.annotation.RendererFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс ChecklistRendererDispatcher используется для диспетчеризации рендереров чек-листов.
 */
@Component
public class ChecklistRendererDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ChecklistRendererDispatcher.class);
    private final Map<String, DocumentChecklistRenderer> rendererMap = new HashMap<>();
    private final DocumentChecklistRenderer defaultRenderer;

    /**
     * Конструктор класса ChecklistRendererDispatcher.
     *
     * @param renderers список рендереров чек-листов
     */
    public ChecklistRendererDispatcher(List<DocumentChecklistRenderer> renderers) {
        DocumentChecklistRenderer defaultTemp = null;

        for (DocumentChecklistRenderer renderer : renderers) {
            RendererFor annotation = AnnotationUtils.findAnnotation(renderer.getClass(), RendererFor.class);

            if (annotation != null) {
                rendererMap.put(annotation.value(), renderer);
                log.info("Добавлен рендерер для шаблона: {}", annotation.value());
            }

            if (renderer.getClass().getSimpleName().equalsIgnoreCase("DefaultChecklistRenderer")) {
                defaultTemp = renderer;
            }
        }

        if (defaultTemp == null) {
            throw new IllegalStateException("DefaultChecklistRenderer не найден");
        }

        this.defaultRenderer = defaultTemp;
    }

    /**
     * Метод getRenderer возвращает рендерер чек-листа для заданного шаблона.
     *
     * @param templateName имя шаблона
     * @return рендерер чек-листа
     */
    public DocumentChecklistRenderer getRenderer(String templateName) {
        return rendererMap.getOrDefault(templateName, defaultRenderer);
    }
}
