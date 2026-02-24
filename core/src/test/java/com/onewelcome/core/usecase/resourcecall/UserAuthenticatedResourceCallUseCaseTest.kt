package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onewelcome.core.network.RetrofitServiceFactory
import com.onewelcome.core.network.api.Device
import com.onewelcome.core.network.api.Devices
import com.onewelcome.core.network.api.UserAuthenticatedApi
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
class UserAuthenticatedResourceCallUseCaseTest {

    @Mock
    private lateinit var retrofitServiceFactory: RetrofitServiceFactory

    @Mock
    private lateinit var userAuthenticatedApi: UserAuthenticatedApi

    private lateinit var useCase: UserAuthenticatedResourceCallUseCase

    @Before
    fun setup() {
        useCase = UserAuthenticatedResourceCallUseCase(retrofitServiceFactory)
        whenever(retrofitServiceFactory.createUserAuthenticatedApi()).thenReturn(userAuthenticatedApi)
    }

    @Test
    fun `Given API call is successful, When getDeviceList is called, Then return Ok with devices`() = runTest {
        val device = Device(
            application = "app",
            id = "1",
            mobileAuthenticationEnabled = true,
            model = "model",
            name = "name",
            platform = "android",
            pushAuthenticationEnabled = true
        )
        val devices = Devices(listOf(device))
        val successResponse = Response.success(devices)
        whenever(userAuthenticatedApi.getDevices()).thenReturn(successResponse)

        val result = useCase.getDeviceList()

        assertThat(result).isEqualTo(Ok(devices))
    }

    @Test
    fun `Given API call returns 401, When getDeviceList is called, Then return Err with UserAuthenticationRequiredException`() = runTest {
        val errorResponse = Response.error<Devices>(401, mock<ResponseBody>())
        whenever(userAuthenticatedApi.getDevices()).thenReturn(errorResponse)

        val result = useCase.getDeviceList()

        assertThat(result).isInstanceOf(Err::class.java)
        assertThat((result as Err).error).isInstanceOf(UserAuthenticationRequiredException::class.java)
    }

    @Test
    fun `Given API call fails with other code, When getDeviceList is called, Then return Err with ResourceCallException`() = runTest {
        val errorResponse = Response.error<Devices>(500, mock<ResponseBody>())
        whenever(userAuthenticatedApi.getDevices()).thenReturn(errorResponse)

        val result = useCase.getDeviceList()

        assertThat(result).isInstanceOf(Err::class.java)
        val error = (result as Err).error
        assertThat(error).isInstanceOf(ResourceCallException::class.java)
        assertThat((error as ResourceCallException).code).isEqualTo(500)
    }

    @Test
    fun `Given API call throws exception, When getDeviceList is called, Then return Err with exception`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(userAuthenticatedApi.getDevices()).thenThrow(exception)

        val result = useCase.getDeviceList()

        assertThat(result).isEqualTo(Err(exception))
    }
}
