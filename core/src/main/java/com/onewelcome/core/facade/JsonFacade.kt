package com.onewelcome.core.facade

interface JsonFacade {
  fun <T> fromJson(json: String, clazz: Class<T>): T
}
