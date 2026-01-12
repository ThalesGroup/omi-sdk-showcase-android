package com.onewelcome.core.network.api

import retrofit2.Response
import retrofit2.http.GET

interface UserAuthenticatedApi {
    @GET("devices")
    suspend fun getDevices(): Response<Devices>
}

data class Device(
    val application: String,
    val id: String,
    val mobile_authentication_enabled: Boolean,
    val model: String,
    val name: String,
    val platform: String,
    val push_authentication_enabled: Boolean
)
data class Devices(
    val devices: List<Device>
)
