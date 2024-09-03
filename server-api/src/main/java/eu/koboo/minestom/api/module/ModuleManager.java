package eu.koboo.minestom.api.module;

import eu.koboo.minestom.api.module.annotation.ModuleInfo;

import java.util.Map;

public interface ModuleManager {

    void enableModule(Module module);

    void disableModule(Module module);

    void enableAllModules();

    void disableAllModules();

    Module getModule(String name);

    Module[] getModules();

    ModuleInfo getModuleInfo(String name);

    Map<String, ModuleInfo> getModuleInfos();

    boolean isModuleEnabled(String name);

}
