package com.tecsup.authfirebaseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tecsup.authfirebaseapp.ui.theme.AuthFirebaseAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AuthFirebaseAppTheme {
                AppNavigation()   //  ← sin parámetros
            }
        }
    }
}
