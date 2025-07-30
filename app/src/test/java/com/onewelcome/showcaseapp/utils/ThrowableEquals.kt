package com.onewelcome.showcaseapp.utils

import org.assertj.core.api.RecursiveComparisonAssert
import java.util.function.BiPredicate

fun RecursiveComparisonAssert<*>.withEqualsForThrowable(): RecursiveComparisonAssert<*> {
  return this.withEqualsForType(ThrowableEquals(), Throwable::class.java)
}

class ThrowableEquals : BiPredicate<Throwable, Throwable> {

  override fun test(actual: Throwable, expected: Throwable): Boolean {
    return actual.message == expected.message
        && actual::class == expected::class
        && actual.cause == expected.cause
  }
}
