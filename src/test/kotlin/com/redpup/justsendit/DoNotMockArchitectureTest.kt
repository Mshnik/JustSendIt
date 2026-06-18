package com.redpup.justsendit

import com.google.errorprone.annotations.DoNotMock
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaMethodCall
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.Ignore
import org.junit.jupiter.api.Test

@Ignore // TODO: Fix this test to actually catch issues.
// TODO: Add no proto mocking to this test.
class DoNotMockArchitectureTest {

  @Test
  fun `interfaces annotated with DoNotMock must never be mocked`() {
    val importedClasses = ClassFileImporter().importPackages("com.redpup.justsendit")

    val rule = noClasses()
      .should(callMockitoMockOnDoNotMockTargets())
      .because("Interfaces annotated with @DoNotMock are blocklisted from being mocked.")

    rule.check(importedClasses)
  }

  private fun callMockitoMockOnDoNotMockTargets(): ArchCondition<JavaClass> {
    return object : ArchCondition<JavaClass>("call Mockito.mock() on @DoNotMock targets") {
      override fun check(item: JavaClass, events: ConditionEvents) {
        // Checking all classes (including nested/anonymous coroutine classes)
        item.methodCallsFromSelf.forEach { methodCall ->
          if (isMockitoMockCall(methodCall)) {
            val mockedType = extractMockedType(methodCall)
            if (mockedType != null && mockedType.isAnnotatedWith(DoNotMock::class.java)) {
              events.add(
                SimpleConditionEvent.violated(
                  item,
                  "Class ${item.name} attempts to mock ${mockedType.name} at " +
                    "line ${methodCall.lineNumber}, which is annotated with @DoNotMock"
                )
              )
            }
          }
        }
      }
    }
  }

  private fun isMockitoMockCall(methodCall: JavaMethodCall): Boolean {
    val target = methodCall.target
    val ownerName = target.owner.name

    // Catches standard Mockito, mockito-kotlin extensions, and inline reified variants
    return ownerName == "org.mockito.Mockito" ||
      ownerName.contains("mockito.kotlin") ||
      (ownerName.contains("Mockito") && target.name == "mock")
  }

  private fun extractMockedType(methodCall: JavaMethodCall): JavaClass? {
    val target = methodCall.target

    // 1. Try to resolve via ArchUnit's structural return type
    val rawReturnType = target.rawReturnType
    if (rawReturnType.name != "java.lang.Object") {
      return rawReturnType
    }

    return null
  }
}