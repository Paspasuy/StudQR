package com.example.studqr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.example.studqr.api.AttentifyClient
import com.example.studqr.api.Lesson
import com.example.studqr.api.getLoc
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Locale

suspend fun getLessonData(date: String): List<Lesson> {
    val attentifyClient = SessionManager.scheduleDeferred.await()
    return attentifyClient.getScheduleDay(date)
}

fun getWeekDates(dateStr: String): List<String> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(dateStr, formatter)

    // Get the Monday of the same week
    val monday = date.with(DayOfWeek.MONDAY)

    // Generate all 7 days starting from Monday
    return (0..6).map { offset ->
        monday.plusDays(offset.toLong()).format(formatter)
    }
}

@Composable
fun ScheduleComponent() {
    var lessons by remember { mutableStateOf<List<Lesson>?>(null) }

    val today = "2025-05-20"

    Column {
        WeekDateSelector(baseDate=today)
        DayScheduleComponent(today)
    }
}

@Composable
fun WeekDateSelector(
    baseDate: String = LocalDate.now().toString(), // default to today
    onDateSelected: (String) -> Unit = {}
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(baseDate, formatter)

    val monday = date.with(DayOfWeek.MONDAY)
    val weekDates = (0..6+7).map { monday.plusDays(it.toLong()) }

    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(date) }
    var todayDate by remember { mutableStateOf(today) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDates.forEach { day ->
            val isSelected = day == selectedDate
            val isToday = day == todayDate
            Button(
                onClick = {
                    selectedDate = day
                    onDateSelected(day.format(formatter))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isToday) Color.Gray else if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                    contentColor =
                    if (isSelected) Color.White
                    else if (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) Color.Red
                    else Color.DarkGray
                ),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .defaultMinSize(minWidth = 64.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale(SessionManager.currentLanguage)).uppercase())
                    Text(day.dayOfMonth.toString(), fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun DayScheduleComponent(date: String) {
    var lessons by remember { mutableStateOf<List<Lesson>?>(null) }

    LaunchedEffect(Unit) {
        SessionManager.scope.launch {
            lessons = getLessonData(date)
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
    val lang = SessionManager.currentLanguage
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
