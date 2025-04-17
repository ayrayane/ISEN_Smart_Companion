package fr.isen.ahmedyahia.isensmartcompagnion.api

import com.google.gson.annotations.SerializedName
import fr.isen.ahmedyahia.isensmartcompagnion.Event
import java.io.Serializable

data class EventResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String,
    @SerializedName("location") val location: String,
    @SerializedName("category") val category: String
) : Serializable {
    // Fonction de conversion vers le modèle Event
    fun toEvent(): Event {
        return Event(
            id = id.hashCode(), // Convertir l'ID string en int pour la compatibilité
            title = title,
            description = description,
            date = date,
            location = location,
            category = category
        )
    }
}
