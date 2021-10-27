package au.com.wpay.sdk.paymentsimulator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import au.com.woolworths.village.sdk.VillageCustomerOptions
import au.com.woolworths.village.sdk.VillageMerchantOptions
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettings
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettingsActions
import au.com.wpay.sdk.paymentsimulator.settings.defaultSettingsProps
import au.com.wpay.sdk.paymentsimulator.ui.theme.WPayPaymentSimulatorAppTheme
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val scaffoldState = rememberScaffoldState()
            val model: PaymentSimulatorModel by viewModels()

            MainActivityContent(
                scaffoldState = scaffoldState
            ) {
                Navigation(model, navController)
            }

            observeErrors(model, scaffoldState)
        }
    }

    private fun observeErrors(
        model: PaymentSimulatorModel,
        scaffoldState: ScaffoldState
    ) {
        model.error.observe(this, {
            val message = "Something went wrong"

            Log.e("PaymentSimulator", message, it)

            model.viewModelScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = "$message. Check the logs",
                    actionLabel = "OK",
                    duration = SnackbarDuration.Indefinite
                )
            }
        })
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun MainActivityContentPreview() =
    MainActivityContent {}

@ExperimentalMaterialApi
@Composable
private fun MainActivityContent(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navigation: @Composable () -> Unit
) {
    WPayPaymentSimulatorAppTheme {
        Surface(color = MaterialTheme.colors.background) {
            Scaffold(
                scaffoldState = scaffoldState,
                modifier = Modifier.fillMaxHeight()
            ) {
                Column {
                    Toolbar()
                    navigation()
                }
            }
        }
    }
}

@Composable
private fun Toolbar() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.wpay_logo),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(0.7f)
                    .padding(0.dp, 20.dp, 0.dp, 0.dp)
            )

            Text(
                text = "Payment Simulator",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun Navigation(
    viewModel: PaymentSimulatorModel,
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = "settings") {
        composable(route = Routes.Settings.route) {
            WPaySettings(
                props = defaultSettingsProps(),
                actions = object : WPaySettingsActions {
                    override fun onCreatePaymentRequest(
                        merchant: VillageMerchantOptions,
                        customer: VillageCustomerOptions,
                        paymentRequest: NewPaymentRequest
                    ) {
                        navController.navigate(Routes.PaymentDetails.route)
                    }

                    override fun onError(error: Exception) {
                        viewModel.error.postValue(error)
                    }
                }
            )
        }

        composable(route = Routes.PaymentDetails.route) {
            PaymentDetails()
        }
    }
}