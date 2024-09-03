package eu.koboo.minestom.api.module.annotation.dependencies;

public @interface ModuleDependency {

    String name();

    LoadOption loadOption() default LoadOption.PREWORLD;

}
