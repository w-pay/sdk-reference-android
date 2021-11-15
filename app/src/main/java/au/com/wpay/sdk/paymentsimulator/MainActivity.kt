package au.com.wpay.sdk.paymentsimulator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import au.com.wpay.sdk.paymentsimulator.model.PaymentOptions
import au.com.wpay.sdk.paymentsimulator.model.PaymentOutcomes
import au.com.wpay.sdk.paymentsimulator.payment.PaymentDetails
import au.com.wpay.sdk.paymentsimulator.payment.PaymentDetailsFramesConfig
import au.com.wpay.sdk.paymentsimulator.payment.PaymentDetailsProps
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettings
import au.com.wpay.sdk.paymentsimulator.settings.defaultSettingsProps
import au.com.wpay.sdk.paymentsimulator.ui.components.PaymentResultDialog
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
            val showDialog =  rememberSaveable { mutableStateOf(false) }

            MainActivityContent(
                scaffoldState = scaffoldState
            ) {
                Navigation(model, navController)
                PaymentResultDialog(
                    result = model.paymentOutcome.observeAsState(PaymentOutcomes.NoOutcome),
                    show = showDialog.value,
                    dismiss = { showDialog.value = false }
                )
            }

            observeErrors(model, scaffoldState)
            observePaymentRequest(model, navController)
            observePaymentOutcome(model, showDialog)
        }
    }

    private fun observePaymentRequest(
        model: PaymentSimulatorModel,
        navController: NavHostController
    ) {
        model.paymentInstruments.observe(this, {
            navController.navigate(Routes.PaymentDetails.route)
        })
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

    private fun observePaymentOutcome(
        model: PaymentSimulatorModel,
        showDialog: MutableState<Boolean>
    ) {
        model.paymentOutcome.observe(this, { outcome ->
            when (outcome) {
                is PaymentOutcomes.NoOutcome -> {}
                else -> {
                    showDialog.value = true
                }
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
                actions = viewModel
            )
        }

        composable(route = Routes.PaymentDetails.route) {
            PaymentDetails(
                props = PaymentDetailsProps(
                    framesConfig = PaymentDetailsFramesConfig(
                        config = viewModel.framesConfig,
                        callback = viewModel,
                        command = viewModel.framesCommand.observeAsState(),
                        message = viewModel.framesMessage.observeAsState(initial = "")
                    ),
                    cards = viewModel.paymentInstruments.observeAsState(),
                    selectedPaymentOption = viewModel.paymentOption.observeAsState(PaymentOptions.NoOption),
                    paymentOutcome = viewModel.paymentOutcome.observeAsState(PaymentOutcomes.NoOutcome)
                ),
                actions = viewModel
            )
        }
    }
}