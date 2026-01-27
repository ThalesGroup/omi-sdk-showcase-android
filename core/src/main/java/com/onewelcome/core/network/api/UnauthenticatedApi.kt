package com.onewelcome.core.network.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface UnauthenticatedApi {
  @GET("path-to-the-resource")
  suspend fun getPathToResource(): retrofit2.Response<ResponseBody>
}

