package fr.isen.ahmedyahia.isensmartcompagnion.api

import fr.isen.ahmedyahia.isensmartcompagnion.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository {
    private val apiService = RetrofitClient.eventApiService
    
    suspend fun getEvents(): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEvents()
                
                if (response.isSuccessful) {
                    val eventList = response.body()
                    if (eventList != null) {
                        // Convertir la liste de réponses en liste d'événements
                        val events = eventList.map { it.toEvent() }
                        Result.success(events)
                    } else {
                        Result.failure(Exception("Aucun événement trouvé"))
                    }
                } else {
                    Result.failure(Exception("Erreur lors de la récupération des événements: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
