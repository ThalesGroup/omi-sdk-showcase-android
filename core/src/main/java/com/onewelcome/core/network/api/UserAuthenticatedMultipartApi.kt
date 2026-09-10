package com.onewelcome.core.network.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserAuthenticatedMultipartApi {
    @Multipart
    @POST("file-upload")
    suspend fun uploadAttachment(
        @Part attachments: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody
    ): Response<UploadResponse>
}

data class UploadResponse(
    val message: String? = null,
    val status: String? = null
)
