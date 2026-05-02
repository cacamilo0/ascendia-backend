package com.ascendia.ascendia.question;

public enum Category {
    LITERAL("You struggle to extract explicit information from texts."),
    INFERENCE("You struggle to understand implicit meaning."),
    VOCABULARY("You have difficulty understanding words in context.");

    private final String weaknessMessage;

    Category(String weaknessMessage) {
        this.weaknessMessage = weaknessMessage;
    }

    public String getWeaknessMessage() {
        return weaknessMessage;
    }
}