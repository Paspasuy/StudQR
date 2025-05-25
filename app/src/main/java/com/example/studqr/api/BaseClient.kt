package com.example.studqr.api

import android.util.Log
import kotlin.getValue

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.*
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.*

class ConnectionException(message: String, val statusCode: Int) : Exception(message)

abstract class BaseClient {
    abstract val baseUrl: String

    var cHeaders = mutableMapOf<String, String>()

    val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        payload: Any,
    ): T {
        lateinit var response: HttpResponse
        response = client.post("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(payload)
            cHeaders.forEach { (key, value) ->
                header(key, value)
            }
        }
        Log.e("BaseClient", response.toString())

        if (response.status.value >= 300) {
            throw ConnectionException(
                response.status.toString() + response.body<String>(), response.status.value
            )
        }

        return response.body<T>()
    }

    suspend inline fun <reified T> postForm(
        endpoint: String,
        formParameters: Map<String, String>,
    ): T {
        try {
            lateinit var response: HttpResponse
            response = client.post("$baseUrl/$endpoint") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    formParameters.forEach { (key, value) ->
                        append(
                            key, value
                        )
                    }
                }))
                cHeaders.forEach { (key, value) ->
                    header(key, value)
                }
            }

            Log.e("BaseClient", response.toString())
            if (response.status.value >= 300) {
                throw ConnectionException(
                    response.status.toString() + response.body<String>(), response.status.value
                )
            }

            return response.body<T>()
        } catch (err: java.nio.channels.UnresolvedAddressException) {
            throw ConnectionException(err.toString(), 0)
        }
    }


    suspend inline fun <reified T> get(
        endpoint: String, queryParams: Map<String, String> = emptyMap()
    ): T {
        try {
            lateinit var response: HttpResponse
            response = client.get("$baseUrl/$endpoint") {
                url {
                    queryParams.forEach { (key, value) ->
                        parameters.append(key, value.toString())
                    }
                }
                cHeaders.forEach { (key, value) ->
                    header(key, value)
                }
            }
            Log.e("BaseClient", response.toString())
            if (response.status.value >= 300) {
                throw ConnectionException(
                    response.status.toString() + response.body<String>(), response.status.value
                )
            }

            return response.body<T>()
        } catch (err: java.nio.channels.UnresolvedAddressException) {
            throw ConnectionException(err.toString(), 0)
        }
    }
}