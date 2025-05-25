package com.example.studqr

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.example.studqr.api.AttentifyClient
import com.example.studqr.api.ConnectionException
import com.example.studqr.api.Lesson
import com.example.studqr.api.getLoc
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Locale

suspend fun getLessonData(date: String): List<Lesson> {
    val attentifyClient = SessionManager.scheduleDeferred.await()
    return attentifyClient.getScheduleDay(date)
}

@Composable
fun ScheduleComponent(baseDate: String = LocalDate.now().toString()) {

    var baseDate = "2025-05-20"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val baseLocalDate = LocalDate.parse(baseDate, formatter)

    val monday = baseLocalDate.with(DayOfWeek.MONDAY)
    val weekDates = (0..6 + 7).map { monday.plusDays(it.toLong()).format(formatter) }

    val pagerState = rememberPagerState(
        initialPage = weekDates.indexOfFirst { it == baseDate },
        pageCount = { weekDates.size })
    val coroutineScope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(weekDates[pagerState.currentPage]) }

    Column(modifier = Modifier.fillMaxWidth()) {
        WeekDateSelector(
            baseDate = baseDate,
            selectedDate = selectedDate,
            onDateSelected = { selected ->
                selectedDate = selected
                val index = weekDates.indexOf(selected)
                if (index != -1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            })

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState, modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()

        ) { page ->
            val date = weekDates[page]
            LaunchedEffect(pagerState.currentPage) {
                selectedDate = weekDates[pagerState.currentPage]

            }

            DayScheduleComponent(date = date.format(formatter))
        }
    }
}

@Composable
fun WeekDateSelector(
    baseDate: String = LocalDate.now().toString(),
    selectedDate: String,
    onDateSelected: (String) -> Unit = {}

) {


    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(baseDate, formatter)

    val monday = date.with(DayOfWeek.MONDAY)
    val weekDates = (0..6 + 7).map { monday.plusDays(it.toLong()) }

    val today = LocalDate.now().format(formatter)
    var todayDate by remember { mutableStateOf(today) }


    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val buttonWidthDp = 64.dp
    val spacingDp = 16.dp
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    LaunchedEffect(selectedDate) {

        val index = weekDates.indexOf(LocalDate.parse(selectedDate, formatter))
        if (index != -1) {
            val screenWidth = configuration.screenWidthDp.dp

            var offsetPx = scrollState.value

            val offsetRightPx = with(density) {
                (index * (buttonWidthDp + spacingDp).toPx()).toInt()
            }
            val offsetLeftPx = with(density) {
                (- screenWidth.toPx() + (index + 2) * (buttonWidthDp + spacingDp).toPx()).toInt()
            }
            if ((offsetLeftPx > offsetPx) || (offsetRightPx < offsetPx)) {
                offsetPx = if (offsetLeftPx > offsetPx) {
                    offsetLeftPx
                } else {
                    offsetRightPx
                }
                coroutineScope.launch {
                    scrollState.animateScrollTo(offsetPx)
                }
            }
        }
    }



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDates.forEach { day ->
            val dayString = day.format(formatter)

            val isSelected = dayString == selectedDate
            val isToday = dayString == todayDate
            Button(
                onClick = {
                    onDateSelected(day.format(formatter))
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = if (isToday) Color.Gray else if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                    contentColor = if (isSelected) Color.White
                    else if (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) Color.Red
                    else Color.DarkGray
                ), modifier = Modifier
                    .padding(end = 8.dp)
                    .defaultMinSize(minWidth = 64.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        day.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT, Locale(SessionManager.currentLanguage)
                        ).uppercase()
                    )
                    Text(
                        day.dayOfMonth.toString(), fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun DayScheduleComponent(date: String) {
    var lessons by remember { mutableStateOf<List<Lesson>?>(null) }

    var fetchFailureString = stringResource(R.string.fetch_failure)
    var context = LocalContext.current


    LaunchedEffect(date) {
        if (SessionManager.lessonCache.contains(date)) {
            lessons = SessionManager.lessonCache[date]
        } else {
            SessionManager.scope.launch {
                try {
                    val data = getLessonData(date)

                    lessons = data
                    SessionManager.lessonCache[date] = data
                } catch (e: ConnectionException) {
                    Toast.makeText(context, fetchFailureString, Toast.LENGTH_LONG).show()
                    Log.e("BaseClient", e.toString())
                }
            }
        }
    }

    if (lessons != null) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            items(lessons ?: emptyList()) { lesson ->
                LessonCard(lesson = lesson)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            items(listOf(1)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(64.dp),
                )
            }
        }
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
            Text(
                text = getLoc(lesson.subject.name, lang),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.teacher) + ": ${
                    getLoc(
                        lesson.teacher.first_name, lang
                    )
                } ${getLoc(lesson.teacher.last_name, lang)}"
            )
            Text(text = stringResource(R.string.room) + ": ${lesson.location.room_number}")
            Text(
                text = "${lesson.lesson_period.start_time} - ${lesson.lesson_period.end_time}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
