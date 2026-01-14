package com.frytes.cinemaPlus.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.frytes.cinemaPlus.CinemaPlusApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;


@AnalyzeClasses(packagesOf = CinemaPlusApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {


    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..")
                    .allowEmptyShould(true);


    @ArchTest
    static final ArchRule services_should_be_in_service_package =
            classes()
                    .that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..service..")
                    .allowEmptyShould(true);


    /*

    @ArchTest
    static final ArchRule layered_architecture = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("com.frytes.cinemaPlus..")
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");
    */
}