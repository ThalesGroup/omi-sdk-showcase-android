package com.onewelcome.core.network.api

import retrofit2.Response
import retrofit2.http.GET

interface AnonymousApi {
  @GET("application-details")
  suspend fun getApplicationDetails(): Response<ApplicationDetails>
}
data class ApplicationDetails(
  val applicationIdentifier: String,
  val applicationPlatform: String,
  val applicationVersion: String,
)
