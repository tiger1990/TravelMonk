package com.travelmonk

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Architectural guarding using Konsist.
 * These tests verify that the project structure adheres to the defined standards.
 */
class ArchitectureCheck {

    @Test
    fun `view models should reside in a ui package and end with ViewModel`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { 
                it.hasModifier(KoModifier.ABSTRACT) ||
                it.resideInPackage("..ui..") 
            }
    }

    @Test
    fun `repository interfaces should reside in domain repository package`() {
        Konsist
            .scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.resideInPackage("..domain.repository..") }
    }

    @Test
    fun `repository implementations should reside in data repository package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { it.resideInPackage("..data.repository..") }
    }

    @Test
    fun `use cases should reside in domain use case package and end with UseCase`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.resideInPackage("..domain.usecase..") }
    }

    @Test
    fun `hilt modules should be internal and reside in di package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Module")
            .assertTrue { 
                it.resideInPackage("..di..") && it.hasInternalModifier
            }
    }

    @Test
    fun `use cases should have a single public method named invoke`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                val publicFunctions = it.functions().filter { func -> 
                    func.hasPublicModifier || (!func.hasPrivateModifier && !func.hasInternalModifier && !func.hasProtectedModifier)
                }
                publicFunctions.size == 1 && publicFunctions.first().name == "invoke"
            }
    }
}
