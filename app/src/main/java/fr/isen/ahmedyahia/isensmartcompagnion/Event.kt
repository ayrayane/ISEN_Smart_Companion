package fr.isen.ahmedyahia.isensmartcompagnion

import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String
) : Serializable {
    // Convertit la chaîne de date en LocalDate
    fun getLocalDate(): LocalDate? {
        return try {
            // Essaie de parser avec le format dd/MM/yyyy
            LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: DateTimeParseException) {
            try {
                // Essaie avec un autre format possible (yyyy-MM-dd)
                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                try {
                    // Essaie avec le format "dd MMMM yyyy" en français
                    LocalDate.parse(date, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH))
                } catch (e: DateTimeParseException) {
                    try {
                        // Essaie avec le format "1er MMMM yyyy" en français (pour les premiers du mois)
                        val correctedDate = date.replace("1er", "1")
                        LocalDate.parse(correctedDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH))
                    } catch (e: DateTimeParseException) {
                        null // Retourne null si la date ne peut pas être parsée
                    }
                }
            }
        }
    }
}
