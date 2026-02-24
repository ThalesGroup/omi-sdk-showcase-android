package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.network.api.ApplicationDetails
import com.onewelcome.core.network.api.DecoratedIdModel
import com.onewelcome.core.network.api.Devices
import com.onewelcome.core.usecase.resourcecall.AnonymousResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.ImplicitResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.UnauthenticatedResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.UserAuthenticatedResourceCallUseCase
import com.onewelcome.showcaseapp.feature.resourcecall.ResourceCallViewModel
import com.onewelcome.showcaseapp.feature.resourcecall.ResourceType
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class ResourceCallViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Mock
    private lateinit var unauthenticatedUseCase: UnauthenticatedResourceCallUseCase

    @Mock
    private lateinit var anonymousUseCase: AnonymousResourceCallUseCase

    @Mock
    private lateinit var userAuthenticatedUseCase: UserAuthenticatedResourceCallUseCase

    @Mock
    private lateinit var implicitUseCase: ImplicitResourceCallUseCase

    private lateinit var viewModel: ResourceCallViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        viewModel = ResourceCallViewModel(
            unauthenticatedUseCase,
            anonymousUseCase,
            userAuthenticatedUseCase,
            implicitUseCase
        )
    }

    @Test
    fun `When executeUnauthenticatedCall is successful, Then state is updated with success message`() = runTest {
        whenever(unauthenticatedUseCase.getPathResources()).thenReturn(Ok(true))

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteUnauthenticatedCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).contains("Unauthenticated Call Success")
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `When executeUnauthenticatedCall fails, Then state is updated with error message`() = runTest {
        val error = RuntimeException("Network error")
        whenever(unauthenticatedUseCase.getPathResources()).thenReturn(Err(error))

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteUnauthenticatedCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).isNull()
        assertThat(state.errorMessage).contains("Unauthenticated Call Failed")
    }

    @Test
    fun `When executeAnonymousCall is successful, Then state is updated with app details`() = runTest {
        val appDetails = ApplicationDetails(
            application_identifier = "id",
            application_platform = "android",
            application_version = "1.0"
        )
        whenever(anonymousUseCase.authenticateDevice()).thenReturn(Ok(Unit))
        whenever(anonymousUseCase.authenticateAndGetAppDetails(any())).thenReturn(Ok(appDetails))

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteAnonymousCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).contains("Anonymous Call Success")
        assertThat(state.result).contains("1.0")
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `When executeAnonymousCall authentication fails, Then state is updated with error`() = runTest {
        val error = RuntimeException("Auth error")
        whenever(anonymousUseCase.authenticateDevice()).thenReturn(Err(error))

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteAnonymousCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).isNull()
        assertThat(state.errorMessage).contains("Device Authentication Failed")
    }

    @Test
    fun `When executeUserAuthenticatedCall is successful, Then state is updated with device list`() = runTest {
        val devices = Devices(listOf())
        whenever(userAuthenticatedUseCase.getDeviceList()).thenReturn(Ok(devices))

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteUserAuthenticatedCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).contains("User Authenticated Call Success")
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `When executeImplicitCall is successful, Then state is updated with user id`() = runTest {
        val user = mock<UserProfile>()
        val decoratedUser = DecoratedIdModel("user123")
        whenever(implicitUseCase.getImplicitlyAuthenticatedUserProfile()).thenReturn(user)
        whenever(implicitUseCase.getUserId()).thenReturn(Ok(decoratedUser))

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteImplicitCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).contains("Implicit Call Success")
        assertThat(state.result).contains("user123")
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `When executeImplicitCall has no implicit user, Then state is updated with error`() = runTest {
        whenever(implicitUseCase.getImplicitlyAuthenticatedUserProfile()).thenReturn(null)

        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteImplicitCall)

        val state = viewModel.uiState
        assertThat(state.isLoading).isFalse
        assertThat(state.result).isNull()
        assertThat(state.errorMessage).contains("No Implicitly Authenticated User Found")
    }

    @Test
    fun `When resource type is selected, Then state is updated`() = runTest {
        viewModel.onEvent(ResourceCallViewModel.UiEvent.SelectResourceType(ResourceType.ANONYMOUS))

        assertThat(viewModel.uiState.selectedResourceType).isEqualTo(ResourceType.ANONYMOUS)
    }

    @Test
    fun `When ClearResult event, Then result and error are cleared`() = runTest {
        // Set some initial state
        whenever(unauthenticatedUseCase.getPathResources()).thenReturn(Ok(true))
        viewModel.onEvent(ResourceCallViewModel.UiEvent.ExecuteUnauthenticatedCall)
        
        viewModel.onEvent(ResourceCallViewModel.UiEvent.ClearResult)

        assertThat(viewModel.uiState.result).isNull()
        assertThat(viewModel.uiState.errorMessage).isNull()
    }
}
