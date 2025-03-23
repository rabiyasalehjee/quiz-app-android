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
    private LinearLayout resultsContainer;
    private TextView scoreTextView;

    private TextView questionTextView, loadingText, resultsTextView;
    private Button startButton, nextButton, restartButton, skipButton;
    private ProgressBar loadingBar;
    private final List<UserAnswer> userAnswers = new ArrayList<>();
    private int score = 0;
    private int questionCount = 0;
    private final int totalQuestions = 2;

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

        startButton = findViewById(R.id.startButton);
        nextButton = findViewById(R.id.nextQuestionButton);
        restartButton = findViewById(R.id.restartButton);
        skipButton = findViewById(R.id.skipButton);
        loadingBar = findViewById(R.id.loadingBar);
        resultsContainer = findViewById(R.id.resultsContainer);
        scoreTextView = findViewById(R.id.scoreTextView);


        startButton.setOnClickListener(v -> {
            introLayout.setVisibility(View.GONE);
            quizLayout.setVisibility(View.VISIBLE);
            startQuiz();
        });

        nextButton.setOnClickListener(v -> {
            nextButton.setVisibility(View.GONE);
            fetchTriviaQuestion();
        });

        skipButton.setOnClickListener(v -> {
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
        questionTextView.setText("");
        optionsContainer.removeAllViews();
        nextButton.setVisibility(View.GONE);
        skipButton.setVisibility(View.GONE);
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
                optionsContainer.removeAllViews();
                skipButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                t.printStackTrace();
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
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setBackgroundResource(R.drawable.option_card_background);
            card.setElevation(6f);
            card.setPadding(12, 12, 12, 12);

            Button optionButton = new Button(this);
            optionButton.setText(option);
            optionButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            optionButton.setTextColor(getResources().getColor(android.R.color.white));
            optionButton.setTextSize(16f);
            optionButton.setAllCaps(false);
            optionButton.setPadding(8, 8, 8, 8);
            optionButton.setBackground(null);
            optionButton.setOnClickListener(v -> checkAnswer(optionButton));

            card.addView(optionButton);
            optionsContainer.addView(card);
        }

        nextButton.setVisibility(View.GONE);
        skipButton.setVisibility(View.VISIBLE);
    }

    private void checkAnswer(Button selectedButton) {
        String selectedAnswer = selectedButton.getText().toString();
        boolean isCorrect = selectedAnswer.equals(currentCorrectAnswer);

        if (isCorrect) {
            selectedButton.setBackgroundResource(R.drawable.option_correct);
            score++;
        } else {
            selectedButton.setBackgroundResource(R.drawable.option_wrong);
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                LinearLayout card = (LinearLayout) optionsContainer.getChildAt(i);
                Button btn = (Button) card.getChildAt(0);
                if (btn.getText().toString().equals(currentCorrectAnswer)) {
                    btn.setBackgroundResource(R.drawable.option_correct);
                }
            }
        }

        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            LinearLayout card = (LinearLayout) optionsContainer.getChildAt(i);
            Button btn = (Button) card.getChildAt(0);
            btn.setEnabled(false);
        }

        userAnswers.add(new UserAnswer(currentQuestionText, selectedAnswer, currentCorrectAnswer));

        skipButton.setVisibility(View.GONE);

        if (questionCount >= totalQuestions) {
            new Handler(Looper.getMainLooper()).postDelayed(this::showResults, 1000);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private void showResults() {
        quizLayout.setVisibility(View.GONE);
        resultsLayout.setVisibility(View.VISIBLE);
        resultsContainer.removeAllViews();

        scoreTextView.setText("Your Score: " + score + " / " + totalQuestions);

        for (UserAnswer ua : userAnswers) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.option_card_background); // reuse existing card bg
            card.setPadding(20, 20, 20, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);
            card.setElevation(8f);

            TextView questionView = new TextView(this);
            questionView.setText("Q: " + ua.getQuestion());
            questionView.setTextColor(getResources().getColor(android.R.color.white));
            questionView.setTextSize(16f);
            questionView.setTypeface(null, android.graphics.Typeface.BOLD);
            questionView.setPadding(0, 0, 0, 8);

            TextView userAnswerView = new TextView(this);
            userAnswerView.setText("Your Answer: " + ua.getUserAnswer());
            userAnswerView.setTextColor(
                    ua.getUserAnswer().equals(ua.getCorrectAnswer())
                            ? getResources().getColor(android.R.color.holo_green_light)
                            : getResources().getColor(android.R.color.holo_red_light)
            );

            TextView correctAnswerView = new TextView(this);
            correctAnswerView.setText("Correct Answer: " + ua.getCorrectAnswer());
            correctAnswerView.setTextColor(getResources().getColor(android.R.color.holo_green_light));

            card.addView(questionView);
            card.addView(userAnswerView);
            card.addView(correctAnswerView);

            resultsContainer.addView(card);
        }
    }


    private void showLoading(boolean isLoading) {
        loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loadingText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        questionTextView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        optionsContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        skipButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) {
            startLoadingAnimation();
        } else {
            stopLoadingAnimation();
        }
        restartButton.setVisibility(View.VISIBLE);
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
