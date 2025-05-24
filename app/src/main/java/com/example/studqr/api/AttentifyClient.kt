package com.example.studqr.api

import android.util.Log
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable

@Serializable
data class BearerToken(
    val access_token: String, val token_type: String
)

@Serializable
data class User(
    val id: Int,
    val email: String,
    val is_active: Boolean,
    val is_superuser: Boolean,
    val is_verified: Boolean
)

@Serializable
data class Lesson(
    val id: Int,
    val lesson_period: LessonPeriod,
    val subject: Subject,
    val teacher: Teacher,
    val lesson_type: LessonType,
    val location: Location,
    val schedule: Schedule,
    val groups: List<Group>
)

@Serializable
data class LessonPeriod(
    val id: Int, val lesson_number: Int, val start_time: String, val end_time: String
)

@Serializable
data class Subject(
    val id: Int, val name: Multilang, val description: Multilang
)

@Serializable
data class Teacher(
    val id: Int,
    val user_id: Int,
    val first_name: Multilang,
    val last_name: Multilang,
    val patronymic: Multilang,
    val phone: String
)

@Serializable
data class LessonType(
    val id: Int, val name: Multilang
)

@Serializable
data class Location(
    val site: Site, val room_number: String, val is_virtual: Boolean
)

@Serializable
data class Site(
    val id: Int, val name: Multilang, val description: Multilang
)

@Serializable
data class Schedule(
    val term_id: Int, val day_of_week: DayOfWeek, val week_type: WeekType
)

@Serializable
data class DayOfWeek(
    val id: Int, val day_number: Int, val name: Multilang
)

@Serializable
data class WeekType(
    val id: Int, val name: Multilang
)

@Serializable
data class Group(
    val id: Int, val name: Multilang, val description: Multilang
)

@Serializable
data class Multilang(
    val ru: String?, val en: String?
)

fun getLoc(value: Multilang, locale: String): String {
    val result = when (locale) {
        "ru" -> value.ru ?: value.en
        else -> value.en ?: value.ru
    }
    return result!!
}

class AttentifyClient : BaseClient() {

    override val baseUrl = "https://api.parzivalll.my.id/attentify"

    private lateinit var token: String

    suspend fun login(username: String, password: String) {
        val body = mapOf("username" to username, "password" to password)
        val bearerToken: BearerToken = postForm("auth/jwt/login", body)
        token = bearerToken.access_token
        cHeaders[HttpHeaders.Authorization] = "Bearer $token"

//        val kuki: String = postForm("auth/cookie/login", body)
//        Log.e("pechenka", kuki)
    }

    suspend fun getScheduleDay(date: String): List<Lesson> {
        val lessons: List<Lesson> = get("student/schedule/day", mapOf("target_date" to date))
        return lessons
    }

    suspend fun getMe(): User {
        val me: User = get("users/me")
        return me
    }
}