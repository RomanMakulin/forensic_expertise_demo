package com.example.expertise.services.expertise.checklists.render.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RendererFor {
    String value();
}
