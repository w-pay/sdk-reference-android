package au.com.wpay.sdk.paymentsimulator.payment

import au.com.woolworths.village.sdk.Wallet
import au.com.wpay.frames.*
import au.com.wpay.frames.types.ActionType
import au.com.wpay.frames.types.ControlType
import au.com.wpay.frames.types.ThreeDSEnv

const val CAPTURE_CARD_ACTION = "cardCapture"
const val VALIDATE_CARD_ACTION = "validateCard"

data class CardCaptureOptions(
    val wallet: Wallet?,
    val require3DS: Boolean
)

fun cardCaptureCommand(options: CardCaptureOptions): JavascriptCommand =
    BuildFramesCommand(
        ActionType.CaptureCard(cardCaptureOptions(options)).toCommand(CAPTURE_CARD_ACTION),
        StartActionCommand(CAPTURE_CARD_ACTION),
        CreateActionControlCommand(CAPTURE_CARD_ACTION, ControlType.CARD_NUMBER, CARD_NO_DOM_ID),
        CreateActionControlCommand(CAPTURE_CARD_ACTION, ControlType.CARD_EXPIRY, CARD_EXPIRY_DOM_ID),
        CreateActionControlCommand(CAPTURE_CARD_ACTION, ControlType.CARD_CVV, CARD_CVV_DOM_ID)
    )

fun cardValidateCommand(
    sessionId: String,
    windowSize: ActionType.AcsWindowSize
): JavascriptCommand =
    GroupCommand("validateCard",
        ActionType.ValidateCard(validateCardOptions(sessionId, windowSize)).toCommand(VALIDATE_CARD_ACTION),
        StartActionCommand(VALIDATE_CARD_ACTION),
        CreateActionControlCommand(VALIDATE_CARD_ACTION, ControlType.VALIDATE_CARD, VALIDATE_CARD_DOM_ID),
        CompleteActionCommand(VALIDATE_CARD_ACTION)
    )

fun cardCaptureOptions(options: CardCaptureOptions) =
    ActionType.CaptureCard.Payload(
        verify = true,
        save = true,
        useEverydayPay = options.wallet == Wallet.EVERYDAY_PAY,
        env3DS = when(options.require3DS) {
            true -> ThreeDSEnv.STAGING
            else -> null
        }
    )

fun validateCardOptions(sessionId: String, windowSize: ActionType.AcsWindowSize) =
    ActionType.ValidateCard.Payload(
        sessionId = sessionId,
        acsWindowSize = windowSize,
        env3DS = ThreeDSEnv.STAGING
    )

object ShowValidationChallenge : JavascriptCommand(
    """
      frames.showValidationChallenge = function() {
        const cardCapture = document.getElementById('${CARD_CAPTURE_DOM_ID}');
        cardCapture.style.display = "none";
        
        const challenge = document.getElementById('${VALIDATE_CARD_DOM_ID}');
        challenge.style.display = "block";
      };
      
      frames.showValidationChallenge();
    """.trimMargin()
)

object HideValidationChallenge : JavascriptCommand(
    """
      frames.showValidationChallenge = function() {
        const cardCapture = document.getElementById('${CARD_CAPTURE_DOM_ID}');
        cardCapture.style.display = "block";
        
        const challenge = document.getElementById('${VALIDATE_CARD_DOM_ID}');
        challenge.style.display = "none";
      };
      
      frames.showValidationChallenge();
    """.trimMargin()
)