package com.onewelcome.showcaseapp.fakes

import com.onewelcome.core.facade.JsonFacade
import javax.inject.Inject
import javax.inject.Singleton


@Suppress("UNCHECKED_CAST")
@Singleton
class JsonFacadeFake @Inject constructor() : JsonFacade {
  var gsonResponse: Class<*>? = null
  override fun <T> fromJson(json: String, clazz: Class<T>): T {
    return gsonResponse as? T ?: throw IllegalArgumentException("No fake response for ${clazz.simpleName}")
  }

  fun setResponse(clazz: Class<*>) {
    gsonResponse = clazz
  }
}
