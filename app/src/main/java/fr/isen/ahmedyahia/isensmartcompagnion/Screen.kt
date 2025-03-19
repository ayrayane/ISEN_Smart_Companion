@file:OptIn(ExperimentalMaterial3Api::class)

package fr.isen.ahmedyahia.isensmartcompagnion

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import fr.isen.ahmedyahia.isensmartcompagnion.api.EventRepository
import kotlinx.coroutines.launch

// Définition unique des routes pour la navigation incluant "Chat"
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Accueil")
    object Events : Screen("events", "Événements")
    object Agenda : Screen("agenda", "Agenda")
    object Historique : Screen("historique", "Historique")
}

// Classe pour les éléments de la barre de navigation
data class TabBarItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Définition des éléments de la barre de navigation
    val homeTab = TabBarItem(
        screen = Screen.Home,
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Default.Home
    )

    val eventsTab = TabBarItem(
        screen = Screen.Events,
        selectedIcon = Icons.Default.DateRange,
        unselectedIcon = Icons.Default.DateRange
    )

    val agendaTab = TabBarItem(
        screen = Screen.Agenda,
        selectedIcon = Icons.Default.CalendarToday,
        unselectedIcon = Icons.Default.CalendarToday,
        badgeAmount = 3  // Exemple de badge pour les messages non lus
    )

    val infoTab = TabBarItem(
        screen = Screen.Historique,
        selectedIcon = Icons.Default.History,
        unselectedIcon = Icons.Default.History
    )

    // Liste de tous les onglets
    val tabBarItems = listOf(homeTab, eventsTab, agendaTab, infoTab)

    // État de navigation actuel
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    HomeScreen()
                }
                composable(Screen.Events.route) {
                    EventListScreen()
                }
                composable(Screen.Agenda.route) {
                    ChatScreen()
                }
                composable(Screen.Historique.route) {
                    InfoScreen()
                }
            }
        }

        // Barre de navigation améliorée avec icônes
        NavigationBar {
            tabBarItems.forEach { item ->
                val selected = currentRoute == item.screen.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.screen.route) {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        TabBarIconView(
                            isSelected = selected,
                            selectedIcon = item.selectedIcon,
                            unselectedIcon = item.unselectedIcon,
                            title = item.screen.title,
                            badgeAmount = item.badgeAmount
                        )
                    },
                    label = { Text(item.screen.title) }
                )
            }
        }
    }
}

// Composant pour afficher l'icône avec badge dans la barre de navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = {
        if (badgeAmount != null) {
            Badge {
                Text(badgeAmount.toString())
            }
        }
    }) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = title
        )
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Bienvenue à l'ISEN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Contenu de l'écran d'accueil
        Text(
            text = "Consultez les événements à venir et restez informé des actualités de l'école.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Bouton pour accéder aux événements (maintenant géré par la barre de navigation)
        Button(
            onClick = {
                Toast.makeText(context, "Vous pouvez accéder aux événements via l'onglet Événements", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voir les événements")
        }
    }
}

@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { EventRepository() }

    // États pour gérer le chargement et les erreurs
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    // Fonction pour charger les événements
    fun loadEvents() {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            repository.getEvents()
                .onSuccess { eventList ->
                    events = eventList
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Une erreur s'est produite"
                    isLoading = false
                }
        }
    }

    // Charger les événements au lancement
    LaunchedEffect(Unit) {
        loadEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // En-tête avec titre et bouton de rafraîchissement
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Événements à venir",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Bouton de rafraîchissement
            Button(
                onClick = { loadEvents() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage du contenu en fonction de l'état
        when {
            isLoading -> {
                // Affichage du chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chargement des événements...")
                    }
                }
            }

            errorMessage != null -> {
                // Affichage de l'erreur
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Erreur: $errorMessage",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadEvents() }
                        ) {
                            Text("Réessayer")
                        }
                    }
                }
            }

            events.isEmpty() -> {
                // Aucun événement trouvé
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun événement disponible",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                // Affichage de la liste des événements
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(events) { event ->
                        EventCard(event)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête de la carte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f).padding(0.dp,0.dp,8.dp,0.dp),
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Badge de catégorie
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Informations de date et lieu
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Lieu",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description courte
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bouton Détail
            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Détail")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Voir détails"
                )
            }

            // Section détaillée avec animation
            val visibilityState = remember { MutableTransitionState(false) }
            visibilityState.targetState = expanded

            AnimatedVisibility(
                visibleState = visibilityState,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "Description complète",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            // Au clic, démarrez l'activité de détail en passant l'objet Event via Serializable
                            val intent = android.content.Intent(context, EventDetailActivity::class.java)
                            intent.putExtra("event", event)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Voir plus")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Voir plus de détails"
                        )
                    }
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

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Info",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}