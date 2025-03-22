package com.example.quiz_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiService = ApiService.create()
        setContent {
            QuizAppTheme {
                TriviaApp(apiService)
            }
        }
    }
}

@Composable
fun TriviaApp(apiService: ApiService) {
    var question by remember { mutableStateOf("Click the button to generate a question!") }
    var options by remember { mutableStateOf(listOf<String>()) }
    var correctAnswer by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        options.forEach { option ->
            Button(
                onClick = {
                    if (option == correctAnswer) {
                        score++
                    }
                    showResults = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = option)
            }
        }
        if (showResults) {
            Text(
                text = if (score > 0) "Correct! Your score: $score" else "Incorrect. Your score: $score",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val triviaQuestion = apiService.getTriviaQuestion()
                        question = triviaQuestion.question
                        options = triviaQuestion.options
                        correctAnswer = triviaQuestion.correctAnswer
                        showResults = false
                    } catch (e: Exception) {
                        question = "Failed to load question. Please try again."
                        println("Error: ${e.message}")
                    }
                }
            }
        ) {
            Text(text = "Generate Question")
        }
    }
}

@Composable
fun QuizAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}