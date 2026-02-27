package apps.farm

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import apps.farm.ui.navigation.AppNavigation
import apps.farm.ui.theme.Task1Task2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(apps.farm.FarmManagementApplication.setLocale(newBase, "ar"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from splash theme to main app theme before super.onCreate
        setTheme(R.style.Theme_Task1Task2)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Task1Task2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
