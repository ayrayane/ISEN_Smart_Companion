package fr.isen.ahmedyahia.isensmartcompagnion

import java.io.Serializable

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String
) : Serializable
