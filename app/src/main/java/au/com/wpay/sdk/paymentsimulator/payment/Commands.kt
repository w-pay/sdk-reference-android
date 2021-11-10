package au.com.wpay.sdk.paymentsimulator.payment

import au.com.wpay.frames.*
import au.com.wpay.frames.types.ActionType
import au.com.wpay.frames.types.ControlType
import au.com.wpay.frames.types.ThreeDSEnv

const val CAPTURE_CARD_ACTION = "cardCapture"
const val VALIDATE_CARD_ACTION = "validateCard"

fun cardCaptureCommand(): JavascriptCommand =
    BuildFramesCommand(
        ActionType.CaptureCard(cardCaptureOptions()).toCommand(CAPTURE_CARD_ACTION),
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
        CompleteActionCommand(VALIDATE_CARD_ACTION)
    )

fun cardCaptureOptions() =
    ActionType.CaptureCard.Payload(
        verify = true,
        save = true,
        env3DS = null
    )

fun validateCardOptions(sessionId: String, windowSize: ActionType.AcsWindowSize) =
    ActionType.ValidateCard.Payload(
        sessionId = sessionId,
        acsWindowSize = windowSize,
        env3DS = ThreeDSEnv.STAGING
    )