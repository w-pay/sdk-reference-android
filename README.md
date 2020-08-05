# Village Wallet Android SDK Reference App

This app show cases the use of the Village Customer APIs.

# Usage

The app is designed to scan a QR code representing a `Payment Request`
from a merchant via the devices Camera Application. If the user selects
to open the app, the `Payment Request` details are fetched from an
(authenticated) API along with some `Payment Instruments`. The user can
then make the payment using an instrument.

This workflow showcases the use of the Village Customer API, including
how to instantiate the SDK and integrate it into an application.

For an existing application, hopefully it can be deduced to incorporate
the SDK from the `VillageFactory`. Everything in the SDK conforms to an
interface, so if applications have a specific requirement or existing
technology/authentication requirements and class that implements the
interface can be used.

### Postman collection

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

# Dependency management

Due to not yet having access to a repository like JCenter or Artifactory
the dependencies are included via Gradle [Composite Builds](https://docs.gradle.org/current/userguide/composite_builds.html)
and as such need to be checked out in order for the application to be
built from source.

The directory structure expected is

```
wallet
    -> android
        -> app
        -> sdk
    -> openapi
 ```

The paths can be adjusted in the `settings.gradle` to have the Gradle
 find the projects.