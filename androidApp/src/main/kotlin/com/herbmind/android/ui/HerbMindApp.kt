package com.herbmind.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.herbmind.android.ui.screens.HomeScreen

@Composable
fun HerbMindApp() {
    Scaffold { innerPadding ->
        HomeScreen(modifier = Modifier.padding(innerPadding))
    }
}
