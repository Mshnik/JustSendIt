package com.redpup.justsendit

import com.google.common.annotations.VisibleForTesting
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class VisibleForTestingArchitectureTest {

  @Test
  fun `production code cannot call VisibleForTesting unless it is also VisibleForTesting`() {
    // 1. Import ONLY production classes. This automatically allows your src/test
    // classes to do whatever they want.
    val productionClasses = ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages("com.redpup.justsendit")

    // 2. Define the transitive check
    val obeyVisibleForTesting = object :
      ArchCondition<JavaClass>("only call @VisibleForTesting code if the caller is also annotated") {
      override fun check(clazz: JavaClass, events: ConditionEvents) {
        // codeUnitCallsFromSelf covers both standard method calls AND constructor calls
        for (call in clazz.codeUnitCallsFromSelf) {

          // Is the thing being called (or its parent class) restricted?
          val targetIsRestricted = call.target.isAnnotatedWith(VisibleForTesting::class.java) ||
            call.target.owner.isAnnotatedWith(VisibleForTesting::class.java)

          // Is the thing making the call (or its parent class) restricted?
          val callerIsRestricted = call.origin.isAnnotatedWith(VisibleForTesting::class.java) ||
            call.origin.owner.isAnnotatedWith(VisibleForTesting::class.java)

          // The core logic: If the target is restricted, the caller MUST be restricted too.
          if (targetIsRestricted && !callerIsRestricted) {
            val message =
              "Violation: \nCaller: ${call.origin.fullName}\nTarget: ${call.target.fullName}\n"
            events.add(SimpleConditionEvent.violated(call, message))
          }
        }
      }
    }

    // 3. Execute the rule
    classes().should(obeyVisibleForTesting).check(productionClasses)
  }
}
