package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiDeregisterUserProfileHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiDeregistrationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.usecase.DeregisterUserUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationViewModel
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationViewModel.State
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationViewModel.UiEvent
import com.onewelcome.showcaseapp.utils.withEqualsForThrowable
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class UserDeregistrationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var deregisterUserUseCase: DeregisterUserUseCase

  @Inject
  lateinit var omiSdkFacade: OmiSdkFacade

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val userClientMock = mock<UserClient>()
  private val oneginiDeregistrationErrorMock = mock<OneginiDeregistrationError>()

  private lateinit var viewModel: UserDeregistrationViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  @Test
  fun `should load initial data when SDK is not initialized`() {
    initViewModel()

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)
  }

  @Test
  fun `should load initial data when SDK is initialized and no profiles registered`() {
    whenNoUserProfilesRegistered()

    initViewModel()

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE.copy(isSdkInitialized = true))
  }

  @Test
  fun `should load initial data when SDK is initialized and profiles are registered`() {
    whenUserProfilesAreRegistered()

    initViewModel()

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        selectedUserProfile = REGISTERED_USER_PROFILES.first(),
        registeredUserProfiles = REGISTERED_USER_PROFILES
      )
    )
  }

  @Test
  fun `should update selected user profile`() {
    whenUserProfilesAreRegistered()
    initViewModel()
    assertThat(viewModel.uiState).isEqualTo(USER_PROFILES_LOADED_STATE)

    viewModel.onEvent(UiEvent.UpdateSelectedUserProfile(UserProfile("QWERTY")))

    assertThat(viewModel.uiState).isEqualTo(USER_PROFILES_LOADED_STATE.copy(selectedUserProfile = UserProfile("QWERTY")))
  }

  @Test
  fun `should successfully deregister user`() {
    whenUserProfilesAreRegistered()
    whenever(userClientMock.deregisterUser(eq(UserProfile("123456")), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiDeregisterUserProfileHandler>(1).onSuccess()
      }
    initViewModel()

    viewModel.onEvent(UiEvent.DeregisterUser)

    assertThat(viewModel.uiState).isEqualTo(
      USER_PROFILES_LOADED_STATE.copy(
        selectedUserProfile = UserProfile("QWERTY"),
        registeredUserProfiles = setOf(UserProfile("QWERTY")),
        result = Ok(Unit)
      )
    )
  }


  @Test
  fun `should show loading when deregistering user`() {
    whenUserProfilesAreRegistered()
    initViewModel()
    whenever(userClientMock.deregisterUser(eq(UserProfile("123456")), any()))
      .thenAnswer { invocation ->
        assertThat(viewModel.uiState.isLoading).isTrue()
        invocation.getArgument<OneginiDeregisterUserProfileHandler>(1).onSuccess()
      }

    viewModel.onEvent(UiEvent.DeregisterUser)

    assertThat(viewModel.uiState.isLoading).isFalse()
  }

  @Test
  fun `should update profiles list after user deregistration`() {
    whenUserProfilesAreRegistered()
    whenever(userClientMock.deregisterUser(eq(UserProfile("123456")), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiDeregisterUserProfileHandler>(1).onSuccess()
      }
    initViewModel()

    viewModel.onEvent(UiEvent.DeregisterUser)

    verify(userClientMock, times(2)).userProfiles
  }

  @Test
  fun `should return error when user profile is not selected`() {
    val expectedState = INITIAL_STATE.copy(
      isSdkInitialized = true,
      result = Err(IllegalArgumentException("User profile not selected"))
    )
    whenNoUserProfilesRegistered()
    initViewModel()

    viewModel.onEvent(UiEvent.DeregisterUser)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(expectedState)
  }

  @Test
  fun `should return error when user deregistration failed`() {
    whenUserProfilesAreRegistered()
    whenever(userClientMock.deregisterUser(eq(UserProfile("123456")), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiDeregisterUserProfileHandler>(1).onError(oneginiDeregistrationErrorMock)
      }
    initViewModel()

    viewModel.onEvent(UiEvent.DeregisterUser)

    assertThat(viewModel.uiState.result).isEqualTo(Err(oneginiDeregistrationErrorMock))
  }

  private fun initViewModel() {
    viewModel = UserDeregistrationViewModel(isSdkInitializedUseCase, getUserProfilesUseCase, deregisterUserUseCase)
  }

  private fun whenNoUserProfilesRegistered() {
    mockSdkInitialized()
    mockNoUserProfiles()
  }

  private fun whenUserProfilesAreRegistered() {
    mockSdkInitialized()
    mockRegisteredUserProfiles()
  }

  private fun mockSdkInitialized() {
    omiSdkFacade.initialize(OmiSdkInitializationSettings(true, null, null, null))
  }

  private fun mockNoUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(emptySet())
  }

  private fun mockRegisteredUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(REGISTERED_USER_PROFILES, REGISTERED_USER_PROFILES_AFTER_DEREGISTRATION)
  }

  companion object {
    private val REGISTERED_USER_PROFILES = setOf(UserProfile("123456"), UserProfile("QWERTY"))
    private val REGISTERED_USER_PROFILES_AFTER_DEREGISTRATION = setOf(UserProfile("QWERTY"))
    private val INITIAL_STATE = State(
      isSdkInitialized = false,
      selectedUserProfile = null,
      registeredUserProfiles = emptySet(),
      isLoading = false,
      result = null
    )
    private val USER_PROFILES_LOADED_STATE = INITIAL_STATE.copy(
      isSdkInitialized = true,
      selectedUserProfile = UserProfile("123456"),
      registeredUserProfiles = REGISTERED_USER_PROFILES
    )
  }
}
