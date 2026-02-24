package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrapError
import com.onewelcome.core.network.RetrofitServiceFactory
import com.onewelcome.core.network.api.UnauthenticatedApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class UnauthenticatedResourceCallUseCaseTest {

    @Mock
    private lateinit var retrofitServiceFactory: RetrofitServiceFactory

    @Mock
    private lateinit var unauthenticatedApi: UnauthenticatedApi

    private lateinit var useCase: UnauthenticatedResourceCallUseCase

    @Before
    fun setup() {
        useCase = UnauthenticatedResourceCallUseCase(retrofitServiceFactory)
        whenever(retrofitServiceFactory.createUnauthenticatedApi()).thenReturn(unauthenticatedApi)
    }

    @Test
    fun `Given API call is successful, When getPathResources is called, Then return Ok with true`() = runTest {
        val responseBody = mock<ResponseBody>()
        val successResponse = Response.success(responseBody)
        whenever(unauthenticatedApi.getPathToResource()).thenReturn(successResponse)

        val result = useCase.getPathResources()

        assertThat(result).isEqualTo(Ok(true))
    }

    @Test
    fun `Given API call fails, When getPathResources is called, Then return Err with ResourceCallException`() = runTest {
        val errorResponse = Response.error<ResponseBody>(500, mock<ResponseBody>())
        whenever(unauthenticatedApi.getPathToResource()).thenReturn(errorResponse)

        val result = useCase.getPathResources()

        assertThat(result.isErr).isTrue()
        val error = result.unwrapError()
        assertThat(error).isInstanceOf(ResourceCallException::class.java)
        assertThat((error as ResourceCallException).code).isEqualTo(500)
    }

    @Test
    fun `Given API call throws exception, When getPathResources is called, Then return Err with exception`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(unauthenticatedApi.getPathToResource()).thenThrow(exception)

        val result = useCase.getPathResources()

        assertThat(result).isEqualTo(Err(exception))
    }
}
