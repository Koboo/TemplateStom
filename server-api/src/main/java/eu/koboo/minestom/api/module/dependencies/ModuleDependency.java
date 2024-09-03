package eu.koboo.minestom.api.module.dependencies;

public @interface ModuleDependency {

    String name();

    LoadOption loadOption() default LoadOption.PREWORLD;

}
