package com.example.expertise.services.expertise.checklists.render.files;

import com.example.expertise.services.expertise.checklists.render.annotation.FileProcessorFor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Диспетчер для выбора нужного процессора
 */
@Component
@RequiredArgsConstructor
public class ChecklistDataDispatcher {

    private final Map<String, ChecklistDataRender> processorMap = new HashMap<>();

    @Autowired
    public ChecklistDataDispatcher(List<ChecklistDataRender> processors) {
        for (ChecklistDataRender processor : processors) {
            FileProcessorFor[] annotations = processor.getClass().getAnnotationsByType(FileProcessorFor.class);
            for (FileProcessorFor a : annotations) {
                processorMap.put(a.value(), processor);
            }
        }

    }

    public ChecklistDataRender getProcessor(String templateName) {
        return processorMap.getOrDefault(templateName, null);
    }
}

