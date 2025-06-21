package com.example.expertise.dto.checklist;

import com.example.expertise.model.checklist.ChecklistInstance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ChecklistInitResult {
    private ChecklistInstance instance;
    private Map<String, Object> oldDataMap;
}
