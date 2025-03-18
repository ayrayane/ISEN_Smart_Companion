@file:OptIn(ExperimentalMaterial3Api::class)

package fr.isen.ahmedyahia.isensmartcompagnion

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Définition unique des routes pour la navigation incluant "Chat"
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Accueil")
    object Events : Screen("events", "Événements")
    object Agenda : Screen("agenda", "Agenda")
    object History : Screen("history", "Historique")
    object Chat : Screen("chat", "Chat")
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        // Utilisation d'EventListScreen pour afficher la liste des événements
        composable(Screen.Events.route) { EventListScreen() }
        composable(Screen.Agenda.route) { AgendaScreen() }
        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.Chat.route) { ChatScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // On exclut Chat de la barre de navigation inférieure (accessible via l'icône dans Home)
    val items = listOf(Screen.Home, Screen.Events, Screen.Agenda, Screen.History)
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = false, // À adapter pour gérer la sélection active
                onClick = { navController.navigate(screen.route) },
                icon = { /* Vous pouvez ajouter une icône spécifique ici */ },
                label = { Text(screen.title) }
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    // Variables d'état pour le texte saisi et la réponse de l'assistant
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("Réponse fictive de l'IA") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Ligne contenant le logo et le titre
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.isen), // Assurez-vous d'avoir un drawable nommé "isen"
                contentDescription = "Logo de l'application",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ISEN Smart Companion",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Champ de saisie pour poser la question
        TextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Posez votre question") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Bouton d'envoi qui affiche un Toast et met à jour la réponse
        Button(
            onClick = {
                Toast.makeText(context, "Question envoyée", Toast.LENGTH_SHORT).show()
                answer = "Vous avez demandé : $question"
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Envoyer")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Affichage de la réponse de l'assistant
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EventListScreen() {
    val context = LocalContext.current

    // Liste fictive d'événements
    val events = listOf(
        Event(
            id = 1,
            title = "Soirée BDE",
            description = "Une soirée animée par le BDE pour se détendre et socialiser.",
            date = "2025-05-15",
            location = "Campus",
            category = "BDE"
        ),
        Event(
            id = 2,
            title = "Gala",
            description = "Un gala élégant pour célébrer la fin d'année.",
            date = "2025-06-20",
            location = "Grand Hôtel",
            category = "Institutionnel"
        ),
        Event(
            id = 3,
            title = "Journée de cohésion",
            description = "Des activités pour renforcer l'esprit d'équipe entre étudiants.",
            date = "2025-07-10",
            location = "Parc",
            category = "Cohésion"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(events) { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        // Au clic, démarrez l'activité de détail en passant l'objet Event via Serializable
                        val intent = android.content.Intent(context, EventDetailActivity::class.java)
                        intent.putExtra("event", event)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = event.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Date : ${event.date}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Lieu : ${event.location}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun AgendaScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Agenda",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historique",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ChatScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ici se trouve le chat",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
