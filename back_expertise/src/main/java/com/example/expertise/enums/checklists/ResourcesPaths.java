package com.example.expertise.enums.checklists;

/**
 * Пути к .txt-файлам с типовыми текстами чек-листов
 */
public enum ResourcesPaths {
    INTRO("checklists-data/construction/construction-intro.txt"),
    BLOCK_1("checklists-data/construction/block-1.txt"),
    BLOCK_2("checklists-data/construction/block-2.txt"),
    BLOCK_3("checklists-data/construction/block-3.txt"),
    BLOCK_4("checklists-data/construction/block-4.txt"),
    BLOCK_5("checklists-data/construction/block-5.txt");

    private final String path;

    ResourcesPaths(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
