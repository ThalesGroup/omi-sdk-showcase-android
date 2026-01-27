package com.onewelcome.core.network.api

import retrofit2.Response
import retrofit2.http.GET

interface ImplicitApi {
  @GET("user-id-decorated")
  suspend fun getDecoratedUserId(): Response<DecoratedIdModel>
}

data class DecoratedIdModel(val decoratedUserId: String)

