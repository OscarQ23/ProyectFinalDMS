package com.example.proyectofinaldms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectofinaldms.ui.auth.AuthScreen
import com.example.proyectofinaldms.ui.auth.AuthViewModel
import com.example.proyectofinaldms.ui.events.CreateEditEventScreen
import com.example.proyectofinaldms.ui.events.EventDetailScreen
import com.example.proyectofinaldms.ui.events.EventViewModel
import com.example.proyectofinaldms.ui.events.HomeScreen
import com.example.proyectofinaldms.ui.history.HistoryScreen
import com.example.proyectofinaldms.ui.notifications.NotificationsScreen
import com.example.proyectofinaldms.ui.profile.ProfileScreen

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val CREATE_EVENT = "create_event"
    const val EDIT_EVENT = "edit_event/{eventId}"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"

    fun eventDetail(eventId: String) = "event_detail/$eventId"
    fun editEvent(eventId: String) = "edit_event/$eventId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = eventViewModel,
                onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) },
                onCreateEvent = { navController.navigate(Routes.CREATE_EVENT) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        }

        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStack ->
            val eventId = backStack.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                viewModel = eventViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onEditEvent = { navController.navigate(Routes.editEvent(eventId)) }
            )
        }

        composable(Routes.CREATE_EVENT) {
            CreateEditEventScreen(
                eventId = null,
                viewModel = eventViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_EVENT,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStack ->
            val eventId = backStack.arguments?.getString("eventId") ?: return@composable
            CreateEditEventScreen(
                eventId = eventId,
                viewModel = eventViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = eventViewModel,
                onBack = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                viewModel = eventViewModel,
                onBack = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
