package au.com.wpay.sdk.paymentsimulator.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import au.com.wpay.frames.DebugLogger
import au.com.wpay.frames.FramesView
import au.com.wpay.frames.JavascriptCommand
import au.com.wpay.frames.types.FramesConfig

const val CARD_CAPTURE_DOM_ID = "cardCaptureGroup"
const val CARD_NO_DOM_ID = "cardNoElement"
const val CARD_EXPIRY_DOM_ID = "cardExpiryElement"
const val CARD_CVV_DOM_ID = "cardCvvElement"
const val VALIDATE_CARD_DOM_ID = "validateCardElement"

private const val HTML = """
<html>
  <body>
    <div id="$CARD_CAPTURE_DOM_ID">
      <div id="$CARD_NO_DOM_ID"></div>
      <div>
        <div id="$CARD_EXPIRY_DOM_ID" style="display: inline-block; width: 50%"></div>
        <div id="$CARD_CVV_DOM_ID" style="display: inline-block; width: 40%; float: right;"></div>
      </div>
    </div>
    <div id="$VALIDATE_CARD_DOM_ID" style="display: none;"></div>
  </body>
</html>
"""

data class FramesHostProps(
    val config: FramesConfig,
    val callback: FramesView.Callback,
    val command: State<JavascriptCommand?>,
    val framesMessage: State<String>
)

@Composable
fun FramesHost(
    props: FramesHostProps
) {
    // save across orientation changes
    var currentCommand: JavascriptCommand? by rememberSaveable { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp, 0.dp)
    ) {
        Text(
            text = props.framesMessage.value,
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
                        callback = props.callback,
                        logger = DebugLogger()
                    )

                    this.loadFrames(props.config)
                }
            },
            update = { framesView ->
                /*
                 * We never want to execute the same command twice. Guard by using reference
                 * equality to check if we've seen the command before
                 */
                if (props.command.value !== currentCommand) {
                    currentCommand = props.command.value

                    currentCommand?.post(framesView)
                }
            }
        )
    }
}
