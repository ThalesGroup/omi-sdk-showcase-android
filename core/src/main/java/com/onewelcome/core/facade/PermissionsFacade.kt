package com.onewelcome.core.facade

interface PermissionsFacade {
  fun checkPostNotificationsPermission(): Boolean
  fun checkCameraPermission(): Boolean
}
