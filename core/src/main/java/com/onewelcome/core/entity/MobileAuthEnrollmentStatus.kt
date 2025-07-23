package com.onewelcome.core.entity

import com.onegini.mobile.sdk.android.model.entity.UserProfile

data class MobileAuthEnrollmentStatus(
  val userProfile: UserProfile,
  val isEnrolledForMobileAuth: Boolean,
  val isEnrolledForMobileAuthWithPush: Boolean
)
