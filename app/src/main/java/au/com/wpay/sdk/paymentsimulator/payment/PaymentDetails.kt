package au.com.wpay.sdk.paymentsimulator.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.woolworths.village.sdk.model.CreditCard
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
            framesConfig = fakeFramesConfig(),
            cards = remember { mutableStateOf(fakeCreditCards()) },
            selectedPaymentOption = remember { mutableStateOf(PaymentOptions.NoOption) }
        ),
        actions = object : PaymentDetailsActions {
            override fun selectNewCardPaymentOption() {

            }

            override fun selectExistingCardPaymentOption(card: CreditCard) {

            }

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
    var makingPayment by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.selectableGroup()) {
        NewCardDetails(
            props = props.framesConfig,
            selectedPaymentOption = props.selectedPaymentOption,
            onPaymentOptionSelected = {
                actions.selectNewCardPaymentOption()
            }
        )

        ExistingCardDetails(
            selectedPaymentOption = props.selectedPaymentOption,
            onPaymentOptionSelected = { card ->
                actions.selectExistingCardPaymentOption(card)
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
            enabled = props.selectedPaymentOption.value.isValid() && !makingPayment,
            waiting = makingPayment,
            text = "Pay Now",
            onClick = {
                scope.launch {
                    makingPayment = true
                    actions.makePayment(props.selectedPaymentOption.value).await()
                    makingPayment = false
                }
            }
        )
    }
}

@Composable
private fun NewCardDetails(
    props: PaymentDetailsFramesConfig,
    selectedPaymentOption: State<PaymentOptions>,
    onPaymentOptionSelected: () -> Unit
) {
    PaymentOption(
        text = "Enter card details",
        isSelectedOption = selectedPaymentOption.value is PaymentOptions.NewCard,
        onOptionSelected = onPaymentOptionSelected
    )

    FramesHost(
        props = FramesHostProps(
            config = props.config,
            command = props.command,
            callback = props.callback,
            framesMessage = props.message
        )
    )
}

@Composable
private fun ExistingCardDetails(
    selectedPaymentOption: State<PaymentOptions>,
    onPaymentOptionSelected: (CreditCard) -> Unit,
    cards: State<List<CreditCard>?>
) {
    var selectedCard: CreditCard? by remember { mutableStateOf(null) }

    PaymentOption(
        text = "Use an existing card stored in your digital wallet",
        isSelectedOption = selectedPaymentOption.value is PaymentOptions.ExistingCard,
        onOptionSelected = { selectedCard?.let { onPaymentOptionSelected(it) } }
    )

    Column(
        modifier = Modifier
            .selectableGroup()
            .padding(32.dp, 0.dp, 0.dp, 0.dp)
    ) {
        cards.value?.forEach { card ->
            CardRow(
                selectedCard = selectedCard,
                cardForRow = card,
                onRowSelected = { chosenCard ->
                    selectedCard = chosenCard

                    onPaymentOptionSelected(chosenCard)
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