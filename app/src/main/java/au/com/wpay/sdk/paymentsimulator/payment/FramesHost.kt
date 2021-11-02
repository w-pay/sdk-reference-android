package au.com.wpay.sdk.paymentsimulator.payment

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import au.com.wpay.frames.*
import au.com.wpay.frames.types.ActionType
import au.com.wpay.frames.types.ControlType
import au.com.wpay.frames.types.FramesConfig

private const val CARD_NO_DOM_ID = "cardNoElement"
private const val CARD_EXPIRY_DOM_ID = "cardExpiryElement"
private const val CARD_CVV_DOM_ID = "cardCvvElement"

private const val HTML = """
<html>
  <body>
    <div id="$CARD_NO_DOM_ID"></div>
    <div>
      <div id="$CARD_EXPIRY_DOM_ID" style="display: inline-block; width: 50%"></div>
      <div id="$CARD_CVV_DOM_ID" style="display: inline-block; width: 40%; float: right;"></div>
    </div>
  </body>
</html>
"""

interface FramesHostActions {
    fun onCardValid(isValid: Boolean)
}

@Composable
fun FramesHost(
    config: FramesConfig,
    actions: FramesHostActions
) {
    var message: String by remember { mutableStateOf("") }
    var cardNumberValid: Boolean by remember { mutableStateOf(false) }
    var cardExpiryValid: Boolean by remember { mutableStateOf(false) }
    var cardCvvValid: Boolean by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp, 0.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            factory = { context ->
                FramesView(context).apply {
                    this.configure(
                        config = FramesView.FramesViewConfig(
                            html = HTML
                        ),
                        callback = object : FramesView.Callback {
                            override fun onComplete(response: String) {
                                TODO("Not yet implemented")
                            }

                            override fun onError(error: FramesError) {
                                debug("onError(error: $error)")

                                message = error.errorMessage.toString()
                            }

                            override fun onFocusChange(domId: String, isFocussed: Boolean) {
                                debug("onFocusChange($domId, isFocussed: $isFocussed)")
                            }

                            override fun onPageLoaded() {
                                debug("onPageLoaded()")

                                BuildFramesCommand(
                                    ActionType.CaptureCard(cardCaptureOptions()).toCommand(),
                                    StartActionCommand,
                                    CreateActionControlCommand(ControlType.CARD_NUMBER, CARD_NO_DOM_ID),
                                    CreateActionControlCommand(ControlType.CARD_EXPIRY, CARD_EXPIRY_DOM_ID),
                                    CreateActionControlCommand(ControlType.CARD_CVV, CARD_CVV_DOM_ID)
                                ).post(this@apply)
                            }

                            override fun onProgressChanged(progress: Int) {
                                debug("onProgressChanged(progress: $progress)")
                            }

                            override fun onRendered() {
                                debug("onRendered()")
                            }

                            override fun onValidationChange(domId: String, isValid: Boolean) {
                                debug("onValidationChange($domId, isValid: $isValid)")

                                when(domId) {
                                    CARD_NO_DOM_ID -> cardNumberValid = isValid
                                    CARD_EXPIRY_DOM_ID -> cardExpiryValid = isValid
                                    CARD_CVV_DOM_ID -> cardCvvValid = isValid
                                }

                                if (cardNumberValid && cardExpiryValid && cardCvvValid) {
                                    message = ""
                                    actions.onCardValid(true)
                                }
                            }
                        },
                        logger = DebugLogger()
                    )

                    this.loadFrames(config)
                }
            }
        )
    }
}

fun cardCaptureOptions() =
    ActionType.CaptureCard.Payload(
        verify = true,
        save = true,
        env3DS = null
    )

private fun debug(message: String) {
    Log.d("[Callback]", message)
}