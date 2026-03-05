package com.onewelcome.core.network

import com.onewelcome.core.network.api.AnonymousApi
import com.onewelcome.core.network.api.ImplicitApi
import com.onewelcome.core.network.api.UnauthenticatedApi
import com.onewelcome.core.network.api.UserAuthenticatedApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RetrofitServiceFactory @Inject constructor(
  val clientProvider: OmiSdkOkHttpClientProvider
) {
  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.HEADERS
  }

  fun createUnauthenticatedApi(): UnauthenticatedApi {
    return createRetrofit(createokhhtpClientWithInterceptor(clientProvider.getUnauthenticatedResourceClient()))
      .create(UnauthenticatedApi::class.java)
  }

  fun createAnonymousApi(): AnonymousApi {
    return createRetrofit(clientProvider.getAnonymousResourceClient())
      .create(AnonymousApi::class.java)
  }

  fun createUserAuthenticatedApi(): UserAuthenticatedApi {
    return createRetrofit(clientProvider.getUserAuthenticatedResourceClient())
      .create(UserAuthenticatedApi::class.java)
  }

  fun createImplicitApi(): ImplicitApi {
    return createRetrofit(clientProvider.getImplicitUserResourceClient())
      .create(ImplicitApi::class.java)
  }

  fun createokhhtpClientWithInterceptor(okHttpClient: OkHttpClient): OkHttpClient {
    return okHttpClient.newBuilder().addInterceptor(loggingInterceptor).build()
  }

  fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(clientProvider.getResourceBaseUrl())
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }
}

