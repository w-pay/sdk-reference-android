# WPay Wallet Android SDK Reference App

This app is designed to show how to use the WPay APIs and SDKs

(Before WPay was released, the initial version was called "Village". Over time, "Village" will be
removed and replaced)

# Usage

The app shows the usage of the WPay
- [Android SDK](https://github.com/w-pay/sdk-wpay-android)
- [Android Frames SDK](https://github.com/w-pay/sdk-wpay-android-frames/)

The workflow the app demonstrates is the creation of a payment request by a given merchant, and
allowing a customer to make a payment. When the customer makes the payment they have the option
to use a preexisting card in their wallet, or capture a new credit card using the WPay Frames
API/SDKs.

The settings screen allows for different settings to be applied such as which merchant to use,
customer details and payment details.

The second screen allows the management of payment instruments and the making of a payment.

## Places of interest

For new developers, the main place of interest is the `PaymentSimulatorModel`. This is the View Model
that holds all the state for the app including the SDK instances, implementations of callbacks
and orchestration logic.

The `PaymentSimulatorModel` implements callbacks that are invoked from the Frames SDK with the
results of posting `JavascriptCommand`s into the SDK. This is the bridge between native apps, and
the JS code being executed to capture a card.

The `PaymentSimulatorModel` also demonstrates the usages of the WPay API SDK both as a customer
and as a merchant.

# Building

```shell
$ ./gradlew clean installDebug
```

# Versioning

The original code for the application was colocated with the SDK in
the SDK repo to facilitate development. So for history, please see that repo.

The application continues with the versioning scheme from the previous
repo to make it less confusing, however the application built from this
repo has a different package name, so any previous versions will need to
be uninstalled as they are deprecated.