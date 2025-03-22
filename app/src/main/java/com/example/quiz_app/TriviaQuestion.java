package com.example.quiz_app;

import java.util.List;

public class TriviaQuestion {
    private String question;
    private List<String> options;
    private String correctAnswer;

    // Getters
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
