package com.example.proyectofinaldms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinaldms.ui.auth.AuthViewModel
import com.example.proyectofinaldms.ui.events.EventViewModel
import com.example.proyectofinaldms.ui.navigation.NavGraph
import com.example.proyectofinaldms.ui.navigation.Routes
import com.example.proyectofinaldms.ui.theme.ProyectoFinalDMSTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProyectoFinalDMSTheme {
                val navController = rememberNavController()

                val startDestination = remember {
                    if (FirebaseAuth.getInstance().currentUser != null) Routes.HOME
                    else Routes.AUTH
                }

                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel,
                    startDestination = startDestination
                )
            }
        }
    }
}
