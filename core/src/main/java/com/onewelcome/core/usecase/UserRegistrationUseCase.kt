package com.onewelcome.core.usecase

//POC-START
import android.util.Log
//POC-END
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class UserRegistrationUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun register(
    identityProvider: OneginiIdentityProvider?,
    scopes: List<String>
  ): Result<Pair<UserProfile, CustomInfo?>, Throwable> {
    //POC-START
    Log.d("FidoRegistration", "UserRegistrationUseCase: calling SDK registerUser - IDP=${identityProvider?.id}, scopes=$scopes")
    //POC-END
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().registerUser(
          identityProvider = identityProvider,
          scopes = scopes.toTypedArray(),
          registrationHandler = object : OneginiRegistrationHandler {
            override fun onSuccess(
              userProfile: UserProfile,
              customInfo: CustomInfo?
            ) {
              //POC-START
              Log.d("FidoRegistration", "SDK registerUser onSuccess: profileId=${userProfile.profileId}, customInfo status=${customInfo?.status}, data=${customInfo?.data}")
              //POC-END
              continuation.resume(Ok(Pair(userProfile, customInfo)))
            }

            override fun onError(error: OneginiRegistrationError) {
              //POC-START
              Log.e("FidoRegistration", "SDK registerUser onError: errorType=${error.errorType}, message=${error.message}")
              //POC-END
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure {
        //POC-START
        Log.e("FidoRegistration", "SDK registerUser exception: ${it.message}", it)
        //POC-END
        continuation.resume(Err(it))
      }
    }
  }
}
