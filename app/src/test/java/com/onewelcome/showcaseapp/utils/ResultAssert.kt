package com.onewelcome.showcaseapp.utils

import com.github.michaelbull.result.Result
import org.assertj.core.api.AbstractAssert

class ResultAssert<V, E>(actual: Result<V, E>) : AbstractAssert<ResultAssert<V, E>, Result<V, E>>(actual, ResultAssert::class.java) {

  fun isOk(): ResultAssert<V, E> {
    isNotNull

    if (!actual.isOk) {
      failWithMessage("Expected Result to be Ok but was Err: <%s>", actual)
    }

    return this
  }

  fun isErr(): ResultAssert<V, E> {
    isNotNull

    if (!actual.isErr) {
      failWithMessage("Expected Result to be Err but was Ok: <%s>", actual)
    }

    return this
  }

  fun hasValue(expected: V): ResultAssert<V, E> {
    isOk()

    val value = actual.value
    if (value != expected) {
      failWithMessage("Expected Ok value to be <%s> but was <%s>", expected, value)
    }

    return this
  }

  fun hasError(expected: E): ResultAssert<V, E> {
    isErr()

    val error = actual.error
    if (error != expected) {
      failWithMessage("Expected Err value to be <%s> but was <%s>", expected, error)
    }

    return this
  }

  fun hasErrorInstance(expectedClass: Class<*>, expectedMessage: String? = null): ResultAssert<V, E> {
    isErr()

    val error = actual.error
      ?: failWithMessage("Expected error to be instance of <%s> but was null", expectedClass.name)

    if (!expectedClass.isInstance(error)) {
      failWithMessage(
        "Expected error to be instance of <%s> but was <%s>",
        expectedClass.name,
        error::class.java.name
      )
    }

    expectedMessage?.let {
      val errorMessage = (error as? Throwable)?.message
      if (errorMessage != it) {
        failWithMessage(
          "Expected error message to be <%s> but was <%s>",
          it,
          errorMessage
        )
      }
    }
    return this
  }

  companion object {
    fun <V, E> assertThat(result: Result<V, E>): ResultAssert<V, E> = ResultAssert(result)
  }
}
