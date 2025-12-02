package ru.justneedcoffee.apishenanigans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.justneedcoffee.apishenanigans.ui.DetailScreen
import ru.justneedcoffee.apishenanigans.ui.GiphyScreen
import ru.justneedcoffee.apishenanigans.ui.viewmodels.GiphyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "list") {

                composable("list") {
                    val viewModel: GiphyViewModel = hiltViewModel()
                    GiphyScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { url ->
                            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                            navController.navigate("detail/$encodedUrl")
                        }
                    )
                }

                composable(
                    route = "detail/{imageUrl}",
                    arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
                ) {
                    val encodedUrl = it.arguments?.getString("imageUrl") ?: ""
                    val decodedUrl = URLDecoder.decode(encodedUrl,  StandardCharsets.UTF_8.toString())

                    DetailScreen(
                        url = decodedUrl,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}