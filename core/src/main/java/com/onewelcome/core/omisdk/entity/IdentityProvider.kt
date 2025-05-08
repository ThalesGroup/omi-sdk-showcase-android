package com.onewelcome.core.omisdk.entity

import android.annotation.SuppressLint
import android.os.Parcel
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider

@SuppressLint("ParcelCreator")
class BrowserIdentityProvider(override val name: String, override val id: String) : OneginiIdentityProvider {
  override fun describeContents(): Int = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(id)
    dest.writeString(name)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BrowserIdentityProvider

    if (name != other.name) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + id.hashCode()
    return result
  }
}

@SuppressLint("ParcelCreator")
data class ApiIdentityProvider(override val name: String, override val id: String) : OneginiIdentityProvider {
  override fun describeContents(): Int = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(id);
    dest.writeString(name); }
}

