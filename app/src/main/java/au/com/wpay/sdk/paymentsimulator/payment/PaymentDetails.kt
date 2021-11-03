package au.com.wpay.sdk.paymentsimulator.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.woolworths.village.sdk.model.CreditCard
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.sdk.paymentsimulator.model.PaymentOptions
import au.com.wpay.sdk.paymentsimulator.ui.components.LayoutBox
import au.com.wpay.sdk.paymentsimulator.ui.components.PrimaryButton
import au.com.wpay.sdk.paymentsimulator.ui.theme.Typography
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun PaymentDetails(
    props: PaymentDetailsProps,
    actions: PaymentDetailsActions
) {
    LayoutBox {
        Column {
            Heading()
            PaymentChoices(props, actions)
        }
    }
}

@Preview
@Composable
private fun PaymentDetailsPreview() {
    val scope = rememberCoroutineScope()

    PaymentDetails(
        props = PaymentDetailsProps(
            cards = fakeCreditCards(),
            framesConfig = fakeFramesConfig()
        ),
        actions = object : PaymentDetailsActions {
            @Suppress("DeferredIsResult")
            override fun makePayment(paymentOption: PaymentOptions): Deferred<Unit> {
                return scope.async {}
            }
        }
    )
}

@Composable
private fun Heading() {
    Row(
        modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 20.dp)
    ) {
        Text(
            text = "How would you like to pay?",
            style = Typography.subtitle1
        )
    }
}

@Composable
private fun PaymentChoices(
    props: PaymentDetailsProps,
    actions: PaymentDetailsActions
) {
    val (selectedPaymentOption, onPaymentOptionSelected)
            = remember { mutableStateOf<PaymentOptions>(PaymentOptions.NoOption) }

    var makingPayment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.selectableGroup()) {
        NewCardDetails(
            config = props.framesConfig,
            selectedPaymentOption = selectedPaymentOption,
            onPaymentOptionSelected = { isValid ->
                onPaymentOptionSelected(PaymentOptions.NewCard(isValid))
            }
        )

        ExistingCardDetails(
            selectedPaymentOption = selectedPaymentOption,
            onPaymentOptionSelected = { card ->
                onPaymentOptionSelected(PaymentOptions.ExistingCard(card))
            },
            cards = props.cards
        )
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 20.dp, 0.dp, 0.dp)
    ) {
        PrimaryButton(
            modifier = Modifier.fillMaxWidth(0.6f),
            enabled = selectedPaymentOption.isValid() && !makingPayment,
            waiting = makingPayment,
            text = "Pay Now",
            onClick = {
                scope.launch {
                    makingPayment = true
                    actions.makePayment(selectedPaymentOption).await()
                    makingPayment = false
                }
            }
        )
    }
}

@Composable
private fun NewCardDetails(
    config: FramesConfig,
    selectedPaymentOption: PaymentOptions?,
    onPaymentOptionSelected: (Boolean) -> Unit
) {
    var valid: Boolean by remember { mutableStateOf(false) }

    PaymentOption(
        text = "Enter card details",
        isSelectedOption = selectedPaymentOption is PaymentOptions.NewCard,
        onOptionSelected = { onPaymentOptionSelected(valid) }
    )

    FramesHost(
        config = config,
        actions = object : FramesHostActions {
            override fun onCardValid(isValid: Boolean) {
                valid = isValid

                onPaymentOptionSelected(valid)
            }

            override fun onActionComplete(response: String) {
                TODO("Not yet implemented")
            }
        }
    )
}

@Composable
private fun ExistingCardDetails(
    selectedPaymentOption: PaymentOptions?,
    onPaymentOptionSelected: (CreditCard) -> Unit,
    cards: List<CreditCard>
) {
    var selectedCard: CreditCard? by remember { mutableStateOf(null) }

    PaymentOption(
        text = "Use an existing card stored in your digital wallet",
        isSelectedOption = selectedPaymentOption is PaymentOptions.ExistingCard,
        onOptionSelected = { selectedCard?.let { onPaymentOptionSelected(it) } }
    )

    Column(
        modifier = Modifier
            .selectableGroup()
            .padding(32.dp, 0.dp, 0.dp, 0.dp)
    ) {
        cards.forEach { card ->
            CardRow(
                selectedCard = selectedCard,
                cardForRow = card,
                onRowSelected = { chosenCard ->
                    selectedCard = chosenCard

                    onPaymentOptionSelected(selectedCard!!)
                }
            )
        }
    }
}

@Composable
private fun CardRow(
    selectedCard: CreditCard?,
    onRowSelected: (CreditCard) -> Unit,
    cardForRow: CreditCard
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedCard == cardForRow,
            onClick = { onRowSelected(cardForRow) }
        )

        Text(
            text = "${cardForRow.scheme} XXXX-${cardForRow.cardSuffix} - ${cardForRow.expiryMonth}/${cardForRow.expiryYear}",
            modifier = Modifier.weight(3f)
        )

        IconButton(
            modifier = Modifier.weight(1f),
            onClick = { /* doSomething() */ }
        ) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete card")
        }
    }
}

@Composable
private fun PaymentOption(
    text: String,
    isSelectedOption: Boolean,
    onOptionSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelectedOption,
                onClick = { onOptionSelected() },
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelectedOption,
            onClick = null // null recommended for accessibility with screenreaders
        )
        Text(
            text = text,
            style = Typography.body1,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}