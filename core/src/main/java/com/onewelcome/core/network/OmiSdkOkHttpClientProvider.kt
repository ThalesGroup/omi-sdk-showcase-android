package com.onewelcome.core.network

import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmiSdkOkHttpClientProvider @Inject constructor(
    private val omiSdkFacade: OmiSdkFacade
) {
    fun getUnauthenticatedResourceClient(): OkHttpClient {
        return omiSdkFacade.oneginiClient
            .getDeviceClient()
            .unauthenticatedResourceOkHttpClient
    }

    fun getAnonymousResourceClient(): OkHttpClient {
        return omiSdkFacade.oneginiClient
            .getDeviceClient()
            .anonymousResourceOkHttpClient
    }

    fun getUserAuthenticatedResourceClient(): OkHttpClient {
        return omiSdkFacade.oneginiClient
            .getUserClient()
            .resourceOkHttpClient
    }

    fun getImplicitUserResourceClient(): OkHttpClient {
        return omiSdkFacade.oneginiClient
            .getUserClient()
            .implicitResourceOkHttpClient
    }

    fun getResourceBaseUrl(): String {
        return omiSdkFacade.oneginiClient.configModel.resourceBaseUrl
    }
}
