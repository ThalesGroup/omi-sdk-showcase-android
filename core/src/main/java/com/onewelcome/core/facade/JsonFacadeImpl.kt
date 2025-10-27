package com.onewelcome.core.facade

import com.google.gson.Gson
import javax.inject.Inject

class JsonFacadeImpl @Inject constructor() : JsonFacade {

  private val gson = Gson()

  override fun <T> fromJson(json: String, clazz: Class<T>): T {
    return gson.fromJson(json, clazz)
  }
}
