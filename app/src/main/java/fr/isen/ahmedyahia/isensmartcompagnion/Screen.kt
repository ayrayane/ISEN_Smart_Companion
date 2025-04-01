@file:OptIn(ExperimentalMaterial3Api::class)

package fr.isen.ahmedyahia.isensmartcompagnion

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

// ------------------------------
// 0) Thème personnalisé avec toutes les couleurs en rouge et fond blanc
// ------------------------------
private val RedColorScheme = lightColorScheme(
    primary = Color.Red,
    onPrimary = Color.White,
    secondary = Color.Red,
    onSecondary = Color.White,
    background = Color.White,        // Fond en blanc
    onBackground = Color.Black,        // Texte par défaut sur fond blanc
    surface = Color.Red,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.White
)

@Composable
fun RedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RedColorScheme,
        typography = typography,
        content = content
    )
}

// ------------------------------
// 1) Définition unique des routes pour la navigation
// ------------------------------
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

// ------------------------------
// 2) Repositories pour stocker l'état global
// ------------------------------
object AgendaRepository {
    val agendaEvents = mutableStateListOf<Event>()

    fun addEvent(event: Event) {
        if (!agendaEvents.contains(event)) {
            agendaEvents.add(event)
        }
    }
}

// Nouveau repository pour le "chat"
object ChatRepository {
    val messages = mutableStateListOf<String>()

    fun addMessage(message: String) {
        messages.add(message)
    }
}

// ------------------------------
// 3) Écran principal (MainScreen) avec la NavBar
// ------------------------------
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Définition des onglets
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
        badgeAmount = AgendaRepository.agendaEvents.size
    )
    val infoTab = TabBarItem(
        screen = Screen.Historique,
        selectedIcon = Icons.Default.History,
        unselectedIcon = Icons.Default.History
    )

    val tabBarItems = listOf(homeTab, eventsTab, agendaTab, infoTab)

    // Gestion de la navigation courante
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    // 4) Nouvel écran d'accueil = assistant (avec GeminiAI)
                    HomeScreen()
                }
                composable(Screen.Events.route) {
                    EventListScreen()
                }
                composable(Screen.Agenda.route) {
                    AgendaScreen()
                }
                composable(Screen.Historique.route) {
                    // 5) On affiche l'historique des requêtes
                    HistoryScreen()
                }
            }
        }

        // Barre de navigation
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
        if (badgeAmount != null && badgeAmount > 0) {
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

// ------------------------------
// 4) HomeScreen = assistant (chat minimaliste avec GeminiAI et logo ISEN)
// ------------------------------
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var userInput by remember { mutableStateOf("") }
    val messages = ChatRepository.messages
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        // Affichage du logo de l'ISEN
        Image(
            painter = painterResource(id = R.drawable.isen), // Vérifiez que l'image "isen.png" est présente dans drawable
            contentDescription = "Logo de l'ISEN",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Assistant virtuel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary // Texte en rouge
        )

        // Affichage des messages (chat)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages) { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onBackground // Texte en noir sur fond blanc
                )
            }
        }

        // Champ de texte et bouton pour envoyer la requête
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userInput,
                onValueChange = { newValue ->
                    userInput = newValue
                    println("HomeScreen - userInput modifié: $newValue")
                },
                modifier = Modifier.weight(1f),
                label = { Text("Posez votre question...", color = MaterialTheme.colorScheme.primary) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        println("HomeScreen - Envoyer cliqué avec input: $userInput")
                        ChatRepository.addMessage("Vous: $userInput")
                        val currentInput = userInput  // stocke la valeur actuelle
                        userInput = ""               // vide ensuite le champ
                        coroutineScope.launch {
                            println("HomeScreen - Lancement de GeminiAI.analyzeText")
                            val answer = GeminiAI.analyzeText(currentInput)
                            println("HomeScreen - Réponse de GeminiAI: $answer")
                            ChatRepository.addMessage("Assistant: $answer")
                        }
                    } else {
                        Toast.makeText(context, "Veuillez saisir une question", Toast.LENGTH_SHORT).show()
                    }
                }

            ) {
                Text("Envoyer", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}


// ------------------------------
// 5) Écran Historique = affiche la liste complète des messages du chat
// ------------------------------
@Composable
fun HistoryScreen() {
    val messages = ChatRepository.messages

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        Text(
            text = "Historique des requêtes",
            style = typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (messages.isEmpty()) {
            Text("Aucune requête pour le moment.", color = MaterialTheme.colorScheme.onBackground)
        } else {
            LazyColumn {
                items(messages) { message ->
                    Text(
                        text = message,
                        style = typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

// ------------------------------
// 6) Écran EventListScreen et EventCard (autres écrans existants)
// ------------------------------
@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { EventRepository() }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

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

    LaunchedEffect(Unit) {
        loadEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Événements à venir",
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { loadEvents() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chargement des événements...", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Erreur: $errorMessage",
                            style = typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadEvents() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Réessayer", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            events.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun événement disponible",
                        style = typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Ici, toujours rouge (selon le thème)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    text = event.title,
                    style = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.category,
                        style = typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.date,
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
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
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Détail", color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Voir détails",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

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
                        color = MaterialTheme.colorScheme.onBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Description complète",
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(context, EventDetailActivity::class.java)
                            intent.putExtra("event", event)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Voir plus", color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Voir plus de détails",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            AgendaRepository.addEvent(event)
                            Toast.makeText(context, "Événement ajouté à l'agenda", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Ajouter à l'agenda", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaScreen() {
    val agendaEvents = AgendaRepository.agendaEvents

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        Text(
            text = "Agenda",
            style = typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (agendaEvents.isEmpty()) {
            Text(
                text = "Aucun événement ajouté à l'agenda",
                style = typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            LazyColumn {
                items(agendaEvents) { event ->
                    EventCard(event)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
