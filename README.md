# OMI Showcase App

The OMI Showcase App is a sample app that gives implementation examples of every feature provided by the OMI Mobile SDK.
Please have a look at the[Getting started page](https://thalesdocs.com/oip/omi-sdk/android-sdk/android-sdk-getting-started/index.html),
if you want more information about how to get setup with this sample app.

## Resolving dependencies

Before you can compile the application it must be able to resolve it's dependencies. The Onegini Android SDK is one of those dependencies.
We have an Artifactory repository that distributes the required dependencies. Make sure that you have access to the Onegini Artifactory
repository (https://thalescpliam.jfrog.io). If you don't have access, no problem just go to
the [App developer quickstart](https://thalesdocs.com/oip/omi-sdk/android-sdk/android-sdk-getting-started/android-sdk-setup-project/index.html#add-the-android-sdk-as-a-dependency)
and perform the first step. Access to
Artifactory is required to let Gradle download the Onegini Android SDK library.

When you have access you have to make sure that your Artifactory username and password are set in the `gradle.properties` file in your
Gradle user home
(e.g. ~/.gradle):

Example contents of the `gradle.properties` file in you Gradle user home:

```
artifactory_user=<username>
artifactory_password=<password>
```

See the documentation below for instructions on setting Gradle properties:
[https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties)

## Receiving push messages (optional)

In order to use the Firebase Cloud Messaging you need to replace sample `app/google-services.json` file with one downloaded from
the [Firebase Console](https://console.firebase.google.com/). There are to approaches you can take to add your own configuration:

1. You can simply create two new apps in you Firebase project with following application IDs: `com.onewelcome.showcaseapp.developer` and
   `com.onewelcome.showcaseapp.internal`.
   Then you just need to replace `app/google-services.json` with your own.
2. `com.onewelcome.showcaseapp.internal` is an internal variant of the app used for testing purposes. If you don't want to create two
   separate apps, you can just setup the app for developer variant `com.onewelcome.showcaseapp.developer`.
   You can see that in current `google-services.json` file there are dummy values. You can just replace the values for
   `com.onewelcome.showcaseapp.developer` with you own and leave the internal variant unchanged, as you won't need it.

If you need more information on how to set up Firebase Messaging, please take a look at
[Google Documentation](https://firebase.google.com/docs/android/setup#manually_add_firebase).

If you ignore this step, you won't be able to receive push messages from the Access.
