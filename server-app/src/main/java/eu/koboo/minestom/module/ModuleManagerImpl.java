package eu.koboo.minestom.module;

import eu.koboo.minestom.api.module.Module;
import eu.koboo.minestom.api.module.ModuleManager;
import eu.koboo.minestom.api.module.annotation.ModuleInfo;
import eu.koboo.minestom.api.module.annotation.dependencies.LoadOption;
import eu.koboo.minestom.api.module.annotation.dependencies.ModuleDependency;
import eu.koboo.minestom.server.ServerImpl;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

public class ModuleManagerImpl implements ModuleManager {

    Map<String, Module> modules;
    Map<String, ModuleInfo> moduleInfos;

    Map<Module, LoadOption> moduleLoadOptions;

    public ModuleManagerImpl() {
        this.modules = new HashMap<>();
        this.moduleInfos = new HashMap<>();
        this.moduleLoadOptions = new HashMap<>();
    }

    @Override
    public void enableModule(Module module) {
        if (module == null) {
            if (ServerImpl.DEBUG) Logger.error("Module is null");
            return;
        }
        ModuleInfo moduleInfo = module.getClass().getAnnotation(ModuleInfo.class);
        if (moduleInfo == null) {
            if (ServerImpl.DEBUG) Logger.error("ModuleInfo is null");
            return;
        }
        if (moduleInfos.containsKey(moduleInfo.name())) {
            if (ServerImpl.DEBUG) Logger.error("Module with name " + moduleInfo.name() + " is already enabled");
            return;
        }
        Logger.info("Enabling module " + moduleInfo.name() + " v" + moduleInfo.version() + " by " + String.join(", ", moduleInfo.authors()));
        for (ModuleDependency moduleDependency : moduleInfo.moduleDependencies()) {
            if (!moduleInfos.containsKey(moduleDependency.name())) {
                if (ServerImpl.DEBUG) Logger.error("Module " + moduleInfo.name() + " requires module " + moduleDependency.name() + " to be enabled. Please enable it first.");
                return;
            }
        }
        modules.put(moduleInfo.name(), module);
        moduleInfos.put(moduleInfo.name(), moduleInfo);
        moduleLoadOptions.put(module, moduleInfo.loadOption());
        if (moduleInfo.loadOption() == LoadOption.POSTWORLD) {
            module.onEnable();
            return;
        }
        //Load module after worlds are loaded
    }

    @Override
    public void disableModule(Module module) {
        if (module == null) {
            if (ServerImpl.DEBUG) Logger.error("Module is null");
            return;
        }
        ModuleInfo moduleInfo = module.getClass().getAnnotation(ModuleInfo.class);
        if (moduleInfo == null) {
            if (ServerImpl.DEBUG) Logger.error("ModuleInfo is null");
            return;
        }
        if (!moduleInfos.containsKey(moduleInfo.name())) {
            if (ServerImpl.DEBUG) Logger.error("Module with name " + moduleInfo.name() + " is not enabled");
            return;
        }
        for (ModuleInfo moduleInfo1 : moduleInfos.values()) {
            for (ModuleDependency moduleDependency : moduleInfo1.moduleDependencies()) {
                if (moduleDependency.name().equals(moduleInfo.name())) {
                    if (ServerImpl.DEBUG) Logger.error("Module " + moduleInfo1.name() + " requires module " + moduleInfo.name() + " to be enabled. Please disable it first.");
                    return;
                }
            }
        }
        Logger.info("Disabling module " + moduleInfo.name() + " v" + moduleInfo.version() + " by " + String.join(", ", moduleInfo.authors()));
        modules.remove(moduleInfo.name());
        moduleInfos.remove(moduleInfo.name());
        moduleLoadOptions.remove(module);
        module.onDisable();
    }

    @Override
    public void enableAllModules() {
        //TODO: Check module folder and load all modules
        //TODO: Have fun with reflection and class loading! <3
    }

    @Override
    public void disableAllModules() {
        for (Module module : modules.values()) {
            disableModule(module);
        }
    }

    @Override
    public Module getModule(String name) {
        return modules.getOrDefault(name, null);
    }

    @Override
    public Module[] getModules() {
        return modules.values().toArray(new Module[0]);
    }

    @Override
    public ModuleInfo getModuleInfo(String name) {
        return moduleInfos.getOrDefault(name, null);
    }

    @Override
    public Map<String, ModuleInfo> getModuleInfos() {
        return moduleInfos;
    }

    @Override
    public boolean isModuleEnabled(String name) {
        return modules.containsKey(name) && modules.get(name) != null && modules.get(name).isEnabled();
    }

    public LoadOption getModuleLoadOption(Module module) {
        return moduleLoadOptions.getOrDefault(module, LoadOption.PREWORLD);
    }

    public Map<String, LoadOption> getModuleLoadOptions() {
        Map<String, LoadOption> moduleLoadOptions = new HashMap<>();
        for (Map.Entry<Module, LoadOption> entry : this.moduleLoadOptions.entrySet()) {
            moduleLoadOptions.put(entry.getKey().getClass().getAnnotation(ModuleInfo.class).name(), entry.getValue());
        }
        return moduleLoadOptions;
    }

    private List<JarFile> getModuleJars() {
        List<JarFile> jarFiles = new ArrayList<>();

        Path moduleFolder = Path.of("modules");
        if (!moduleFolder.toFile().exists()) {
            moduleFolder.toFile().mkdir();
            //no modules to load, yet
            return jarFiles;
        }

        File moduleFolderAsFile = moduleFolder.toFile();

        if (!moduleFolderAsFile.isDirectory()) {
            Logger.error("Module folder is not a directory");
            return jarFiles;
        }

        if (moduleFolderAsFile.listFiles() == null) {
            Logger.error("No modules found; maybe you should create some? :)");
            return jarFiles;
        }

        List<File> filesInModuleFolder = List.of(moduleFolderAsFile.listFiles());

        if (filesInModuleFolder.isEmpty()) {
            Logger.error("No modules found; maybe you should create some? :)");
            return jarFiles;
        }

        for (File file : filesInModuleFolder) {
            if (file.getName().endsWith(".jar")) {
                try {
                    jarFiles.add(new JarFile(file));
                } catch (IOException e) {
                    Logger.error("Failed to load module jar " + file.getName());
                    e.printStackTrace();
                }
            }
        }
        return jarFiles;
    }
}
