// TriviaAppActivity.java (Enhanced Full Version with Intro, Restart, Loading Animation)
package com.example.quiz_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TriviaAppActivity extends AppCompatActivity {

    private LinearLayout introLayout, quizLayout, resultsLayout, optionsContainer;
    private TextView questionTextView, loadingText, resultsTextView;
    private Button startButton, nextButton, restartButton;
    private ProgressBar loadingBar;
    private final List<UserAnswer> userAnswers = new ArrayList<>();
    private int score = 0;
    private int questionCount = 0;
    private final int totalQuestions = 5;

    private ApiService apiService;
    private String currentCorrectAnswer = "";
    private String currentQuestionText = "";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable loadingRunnable;
    private final String[] loadingMessages = {
            "Thinking of a challenging question",
            "Gathering trivia knowledge",
            "Crafting an exciting question",
            "Analyzing historical facts",
            "Digging deep into the trivia archives",
            "Formulating an interesting question",
            "Picking the best question for you"
    };
    private int loadingIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        apiService = ApiService.Factory.create();

        introLayout = findViewById(R.id.introLayout);
        quizLayout = findViewById(R.id.quizLayout);
        resultsLayout = findViewById(R.id.resultsLayout);
        optionsContainer = findViewById(R.id.optionsContainer);

        questionTextView = findViewById(R.id.questionTextView);
        loadingText = findViewById(R.id.loadingText);
        resultsTextView = findViewById(R.id.resultsTextView);

        startButton = findViewById(R.id.startButton);
        nextButton = findViewById(R.id.nextQuestionButton);
        restartButton = findViewById(R.id.restartButton);
        loadingBar = findViewById(R.id.loadingBar);

        startButton.setOnClickListener(v -> {
            introLayout.setVisibility(View.GONE);
            quizLayout.setVisibility(View.VISIBLE);
            startQuiz();
        });

        nextButton.setOnClickListener(v -> {
            nextButton.setVisibility(View.GONE);
            fetchTriviaQuestion();
        });

        restartButton.setOnClickListener(v -> {
            resultsLayout.setVisibility(View.GONE);
            quizLayout.setVisibility(View.VISIBLE);
            score = 0;
            questionCount = 0;
            userAnswers.clear();
            startQuiz();
        });
    }

    private void startQuiz() {
        fetchTriviaQuestion();
    }

    private void fetchTriviaQuestion() {
        showLoading(true);
        Call<TriviaQuestion> call = apiService.getTriviaQuestion();
        call.enqueue(new Callback<TriviaQuestion>() {
            @Override
            public void onResponse(Call<TriviaQuestion> call, Response<TriviaQuestion> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayQuestion(response.body());
                } else {
                    questionTextView.setText("Failed to load question.");
                }
            }

            @Override
            public void onFailure(Call<TriviaQuestion> call, Throwable t) {
                showLoading(false);
                questionTextView.setText("Network error. Please try again.");
            }
        });
    }

    private void displayQuestion(TriviaQuestion trivia) {
        questionCount++;
        currentCorrectAnswer = trivia.getCorrectAnswer();
        currentQuestionText = trivia.getQuestion();

        questionTextView.setText(currentQuestionText);
        optionsContainer.removeAllViews();

        for (String option : trivia.getOptions()) {
            Button optionButton = new Button(this);
            optionButton.setText(option);
            optionButton.setOnClickListener(v -> checkAnswer(optionButton));
            optionButton.setBackgroundResource(R.drawable.option_button);
            optionsContainer.addView(optionButton);
        }
    }

    private void checkAnswer(Button selectedButton) {
        String selectedAnswer = selectedButton.getText().toString();
        boolean isCorrect = selectedAnswer.equals(currentCorrectAnswer);

        if (isCorrect) {
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            score++;
        } else {
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                Button btn = (Button) optionsContainer.getChildAt(i);
                if (btn.getText().toString().equals(currentCorrectAnswer)) {
                    btn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                }
            }
        }

        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).setEnabled(false);
        }

        userAnswers.add(new UserAnswer(currentQuestionText, selectedAnswer, currentCorrectAnswer));

        if (questionCount >= totalQuestions) {
            new Handler(Looper.getMainLooper()).postDelayed(this::showResults, 1000);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private void showResults() {
        quizLayout.setVisibility(View.GONE);
        resultsLayout.setVisibility(View.VISIBLE);

        StringBuilder resultText = new StringBuilder();
        resultText.append("Your Score: ").append(score).append(" / ").append(totalQuestions).append("\n\n");
        for (UserAnswer ua : userAnswers) {
            resultText.append("Q: ").append(ua.getQuestion()).append("\n")
                    .append("Your Answer: ").append(ua.getUserAnswer()).append("\n")
                    .append("Correct Answer: ").append(ua.getCorrectAnswer()).append("\n\n");
        }
        resultsTextView.setText(resultText.toString());
    }

    private void showLoading(boolean isLoading) {
        loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loadingText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            startLoadingAnimation();
        } else {
            stopLoadingAnimation();
        }
    }

    private void startLoadingAnimation() {
        loadingIndex = 0;
        loadingRunnable = new Runnable() {
            @Override
            public void run() {
                loadingText.setText(loadingMessages[loadingIndex]);
                AlphaAnimation fade = new AlphaAnimation(0.3f, 1.0f);
                fade.setDuration(800);
                fade.setRepeatCount(1);
                fade.setRepeatMode(AlphaAnimation.REVERSE);
                loadingText.startAnimation(fade);

                loadingIndex = (loadingIndex + 1) % loadingMessages.length;
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(loadingRunnable);
    }

    private void stopLoadingAnimation() {
        handler.removeCallbacks(loadingRunnable);
    }

    public static class UserAnswer {
        private final String question;
        private final String userAnswer;
        private final String correctAnswer;

        public UserAnswer(String question, String userAnswer, String correctAnswer) {
            this.question = question;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
}