package eu.koboo.minestom.api.module;

import eu.koboo.minestom.api.module.dependencies.ModuleDependency;

public @interface Module {

    String name();

    String version() default "1.0.0";

    String[] authors();

    String description() default "";

    ModuleDependency[] moduleDependencies() default {};

}
