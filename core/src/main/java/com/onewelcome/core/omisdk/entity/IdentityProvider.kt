package com.onewelcome.core.omisdk.entity

import android.annotation.SuppressLint
import android.os.Parcel
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider

@SuppressLint("ParcelCreator")
class BrowserIdentityProvider(override val name: String, override val id: String) : OneginiIdentityProvider {
  override fun describeContents(): Int = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(id);
    dest.writeString(name);
  }
}

@SuppressLint("ParcelCreator")
data class ApiIdentityProvider(override val name: String, override val id: String) : OneginiIdentityProvider {
  override fun describeContents(): Int = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(id);
    dest.writeString(name); }
}

