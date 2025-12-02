package ru.justneedcoffee.apishenanigans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.justneedcoffee.apishenanigans.ui.GiphyScreen
import ru.justneedcoffee.apishenanigans.ui.viewmodels.GiphyViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: GiphyViewModel = hiltViewModel()
            GiphyScreen(viewModel)
        }
    }
}