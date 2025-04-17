package fr.isen.ahmedyahia.isensmartcompagnion.api

import retrofit2.Response
import retrofit2.http.GET

interface EventApiService {
    @GET("events.json")
    suspend fun getEvents(): Response<List<EventResponse>>
}
