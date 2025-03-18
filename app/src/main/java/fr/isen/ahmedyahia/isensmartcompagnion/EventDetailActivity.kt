package fr.isen.ahmedyahia.isensmartcompagnion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.isen.ahmedyahia.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Récupération de l'objet Event transmis via Serializable
        val event = intent.getSerializableExtra("event") as? Event

        setContent {
            ISENSmartCompagnionTheme {
                EventDetailScreen(event)
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (event != null) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date : ${event.date}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Lieu : ${event.location}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Catégorie : ${event.category}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "Aucun détail disponible pour cet événement.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
