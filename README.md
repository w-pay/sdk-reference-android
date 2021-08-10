# Woolies Village Wallet Android SDK Reference App

This app show cases the use of the Village Customer APIs.

# Usage

The app is designed to scan a QR code representing a `Payment Request`
from a merchant via the devices Camera Application. If the user selects
to open the app, the `Payment Request` details are fetched from an
(authenticated) API along with some `Payment Instruments`. The user can
then make the payment using an instrument.

This workflow showcases the use of the Village Customer API, including
how to instantiate the SDK and integrate it into an application.

The entry point into the app is the `PaymentConfirm` Activity which holds
an instance of the `Village` API in the `ViewModel`.

In order to retrieve the `Payment Request` details, the
`Payment Instruments` and to actually make a payment, the application
demonstrates the use of Bearer tokens to access the API.

For an existing application, hopefully it can be deduced how to incorporate
the SDK from the `VillageFactory`. Everything in the SDK conforms to an
interface, so if applications have a specific requirement or existing
technology/authentication requirements and class that implements the
interface can be used.

### Postman collection

In order to use the app, a merchant has to create the `Payment Request`
for a basket of goods. The Postman collection in this repo can be used
to make API calls to the Village "Merchant" API to simulate a merchant.
To use the collection, import both the collection and the environment details
into Postman. The collection is parameterised so it can be used against
different environments.

In order to create a `Payment Request` the `Create Payment Request` request
in the Postman collection can be used. To get the QR code (as an image)
for the `Payment Request` the collection's `Get QR Code` request can be used
(with the `qrId` from the `Create Payment Request` response).

If wanting to launch the app on a Emulator where it's not possible to
scan a QR code, use an Android Studio Run Configuration where the `Launch`
option is set to `URL` and the URL is the same URL as what is in the QR code
itself (and is in `qr.content` in the `Create Payment Request` response).

For example: `https://dev.mobile-api.woolworths.com.au:443/wow/v1/dpwallet/customer/qr/c533c84f-a5d5-4873-bb68-8921d8c91197`

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