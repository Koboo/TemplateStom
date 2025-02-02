package eu.koboo.minestom.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class CurrentBuild {

    public static final String NAME = "PROJECT_NAME";
    public static final String VERSION = "PROJECT_VERSION";
    public static final String GROUP = "PROJECT_GROUP";

    public static final String MINESTOM_VERSION = "DEP_MINESTOM";
    public static final String LITECOMMANDS_VERSION = "DEP_LITECOMMANDS";

    public static final String BUILD_COMMIT = "GIT_COMMIT";
    public static final String BUILD_BRANCH = "GIT_BRANCH";

    public static final String BUILD_ON_GRADLE = "BUILD_GRADLE";
    public static final String BUILD_ON_JDK = "BUILD_JDK";
    public static final String BUILD_ON_JVM = "BUILD_JVM";
    public static final String BUILD_ON_OS = "BUILD_OS";

}
