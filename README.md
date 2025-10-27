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

To use Firebase Cloud Messaging, replacement of `app/google-services.json` with file downloaded from your own project
on [Firebase Console](https://console.firebase.google.com/) is needed.

**If you ignore this step, you won't be able to receive push messages from the backend.**

1. Create a new project on [Firebase Console](https://console.firebase.google.com/).
2. Navigate to project settings and create two new Android apps with following application IDs:

- `com.onewelcome.showcaseapp.developer`
- `com.onewelcome.showcaseapp.internal`

3. Download the `google-services.json` file and replace it with sample `app/google-services.json` present in the project.

Alternatively, if internal build variant is not needed, you can create just `com.onewelcome.showcaseapp.developer` project and edit the
content for the `developer` part inside the sample file.

4. Now, you need to setup Push Messaging Configuration on the backend side. To do that, navigate again to your project
   on [Firebase Console](https://console.firebase.google.com/) -> Project settings -> Cloud messaging. Click on Manage Service Accounts and
   Create Service Account with Cloud Messaging permissions and copy its Key ID. Next, open your backend admin web page and navigate to
   Configuration -> Mobile authentication -> Push messaging configuration. Create new configuration and upload the Key. 

For more information on how to set up Firebase Messaging, please take a look at
[Google Documentation](https://firebase.google.com/docs/android/setup#manually_add_firebase).
