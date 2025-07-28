package com.onewelcome.showcaseapp.utils

import java.util.function.BiPredicate

class ThrowableEquals : BiPredicate<Throwable, Throwable> {

  override fun test(actual: Throwable, expected: Throwable): Boolean {
    return actual.message == expected.message
        && actual::class == expected::class
        && actual.cause == expected.cause
  }
}
