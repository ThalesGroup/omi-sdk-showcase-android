package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.core.network.RetrofitServiceFactory
import com.onewelcome.core.network.api.UploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserAuthenticatedMultipartResourceCallUseCase @Inject constructor(
    private val retrofitServiceFactory: RetrofitServiceFactory
) {
    suspend fun uploadAttachment(
        fileBytes: ByteArray,
        fileName: String,
        name: String,
        email: String
    ): Result<UploadResponse, Throwable> {
        return try {
            val api = retrofitServiceFactory.createUserAuthenticatedMultipartApi()

            val fileRequestBody = fileBytes.toRequestBody("application/octet-stream".toMediaType())
            val attachmentsPart = MultipartBody.Part.createFormData("attachments", fileName, fileRequestBody)
            val namePart = name.toRequestBody("text/plain".toMediaType())
            val emailPart = email.toRequestBody("text/plain".toMediaType())

            val response = api.uploadAttachment(attachmentsPart, namePart, emailPart)

            if (response.isSuccessful) {
                Ok(response.body() ?: UploadResponse(status = "success"))
            } else if (response.code() == 401) {
                Err(UserAuthenticationRequiredException("User session expired, please log in again"))
            } else {
                Err(
                    ResourceCallException(
                        code = response.code(),
                        message = response.message() ?: "Failed to upload attachment"
                    )
                )
            }
        } catch (e: Exception) {
            Err(e)
        }
    }
}
