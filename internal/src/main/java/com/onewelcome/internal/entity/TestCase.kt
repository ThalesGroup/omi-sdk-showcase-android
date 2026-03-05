package com.onewelcome.internal.entity

data class TestCase(val name: String, val status: TestStatus = TestStatus.Pending, val testFunction: suspend () -> TestStatus)
