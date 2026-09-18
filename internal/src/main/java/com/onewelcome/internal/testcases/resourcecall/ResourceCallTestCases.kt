package com.onewelcome.internal.testcases.resourcecall

import com.onewelcome.core.usecase.resourcecall.AnonymousResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.UnauthenticatedResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.UserAuthenticatedResourceCallUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class ResourceCallTestCases @Inject constructor(
  private val unauthenticatedResourceCallUseCase: UnauthenticatedResourceCallUseCase,
  private val anonymousResourceCallUseCase: AnonymousResourceCallUseCase,
  private val userAuthenticatedResourceCallUseCase: UserAuthenticatedResourceCallUseCase,
) {
  val tests = TestCategory(
    name = "Resource calls",
    testCases = listOf(
      TestCase(
        name = "unauthenticatedResourceCall",
        testFunction = ::unauthenticatedResourceCall
      ),
      TestCase(
        name = "anonymousResourceCall",
        testFunction = ::anonymousResourceCall
      ),
      TestCase(
        name = "userAuthenticatedResourceCall",
        testFunction = ::userAuthenticatedResourceCall
      ),
    )
  )

  // For OS compatibility, we verify the SDK's HTTP client can reach the server.
  // HTTP 4xx/5xx errors mean the network stack works — only exceptions (SSL failure,
  // no connection, etc.) indicate an OS-level problem.

  private suspend fun unauthenticatedResourceCall(): TestStatus {
    return try {
      unauthenticatedResourceCallUseCase.getPathResources()
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }

  private suspend fun anonymousResourceCall(): TestStatus {
    return try {
      anonymousResourceCallUseCase.authenticateDevice()
      anonymousResourceCallUseCase.getApplicationDetails()
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }

  private suspend fun userAuthenticatedResourceCall(): TestStatus {
    return try {
      userAuthenticatedResourceCallUseCase.getDeviceList()
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }
}
