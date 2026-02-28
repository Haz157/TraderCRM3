package apps.farm

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.ui.navigation.AppNavigation
import apps.farm.ui.theme.Task1Task2Theme
import apps.farm.ui.screens.AuthScreen
import apps.farm.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import apps.farm.R

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(FarmManagementApplication.setLocale(newBase, "ar"))
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
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val isAuthEnabled by authViewModel.isAuthEnabled.collectAsState(initial = false)
                    val useBiometric by authViewModel.useBiometric.collectAsState(initial = false)
                    val isAuthorized by authViewModel.isAuthorized.collectAsState()
                    val trialExpired by authViewModel.trialExpired.collectAsState()

                    if (trialExpired) {
                        apps.farm.ui.screens.TrialActivationScreen(
                            onActivate = { key -> authViewModel.verifyActivationKey(key) }
                        )
                    } else if (isAuthEnabled && !isAuthorized) {
                        val authError by authViewModel.authError.collectAsState()
                        AuthScreen(
                            onPinEntered = { authViewModel.checkPin(it) },
                            onBiometricSuccess = { authViewModel.onBiometricSuccess() },
                            useBiometric = useBiometric,
                            error = authError
                        )
                    } else {
                        val navController = rememberNavController()
                        AppNavigation(navController = navController)
                    }
                }
            }
        }
    }
}
