package com.example.expertise.services.expertise.document;

/**
 * Для хранения замен в тексте
 */
public class Replacement {
    public int index;
    public Object content;

    public Replacement(int index, Object content) {
        this.index = index;
        this.content = content;
    }
}
