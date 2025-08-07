package com.onewelcome.showcaseapp.fakes

import com.onewelcome.core.facade.PermissionsFacade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsFacadeFake @Inject constructor() : PermissionsFacade {
  var postNotificationsPermissionGranted = false
  override fun checkPostNotificationsPermission(): Boolean {
    return postNotificationsPermissionGranted
  }
}
