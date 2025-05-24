package com.example.studqr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.studqr.SessionManager.currentLanguage
import com.example.studqr.api.AttentifyClient
import com.example.studqr.api.Lesson
import com.example.studqr.api.getLoc
import kotlinx.coroutines.launch

suspend fun getLessonData(): List<Lesson> {
    val attentifyClient = SessionManager.scheduleDeferred.await()
    return attentifyClient.getScheduleDay("2025-05-20")
}

@Composable
fun ScheduleComponent() {
    var lessons by remember { mutableStateOf<List<Lesson>?>(null) }

    LaunchedEffect(Unit) {
        SessionManager.scope.launch {
            lessons = getLessonData()
        }
    }

    if (lessons != null) {
        LazyColumn {
            items(lessons ?: emptyList()) { lesson ->
                LessonCard(lesson = lesson)
            }
        }
    } else {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }

}

@Composable
fun LessonCard(lesson: Lesson) {
    val lang = currentLanguage
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = getLoc(lesson.subject.name, lang), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(R.string.teacher) + ": ${getLoc(lesson.teacher.first_name, lang)} ${getLoc(lesson.teacher.last_name, lang)}")
            Text(text = stringResource(R.string.room) + ": ${lesson.location.room_number}")
            Text(
                text = "${lesson.lesson_period.start_time} - ${lesson.lesson_period.end_time}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
