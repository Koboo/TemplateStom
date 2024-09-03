package eu.koboo.minestom.api.module.annotation;

import eu.koboo.minestom.api.module.annotation.dependencies.LoadOption;
import eu.koboo.minestom.api.module.annotation.dependencies.ModuleDependency;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {

    String name();

    String version() default "1.0.0";

    String[] authors();

    String description() default "";

    ModuleDependency[] moduleDependencies() default {};

    LoadOption loadOption() default LoadOption.PREWORLD;

}
