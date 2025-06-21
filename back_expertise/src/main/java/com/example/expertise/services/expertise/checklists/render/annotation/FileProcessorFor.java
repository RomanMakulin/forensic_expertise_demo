package com.example.expertise.services.expertise.checklists.render.annotation;

import java.lang.annotation.*;

@Repeatable(FileProcessorFor.List.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileProcessorFor {
    String value(); // название шаблона чек-листа

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FileProcessorFor[] value();
    }
}

