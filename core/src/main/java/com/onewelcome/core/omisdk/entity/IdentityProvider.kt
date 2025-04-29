package com.onewelcome.core.omisdk.entity

sealed class IdentityProvider(open val name: String, open val id: String) {
  data class BrowserIdentityProvider(override val name: String, override val id: String) : IdentityProvider(name, id)
  data class ApiIdentityProvider(override val name: String, override val id: String) : IdentityProvider(name, id)
}
