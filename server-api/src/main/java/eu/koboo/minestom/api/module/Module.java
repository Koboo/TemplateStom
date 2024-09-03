package eu.koboo.minestom.api.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PUBLIC)
public abstract class Module {

    private boolean isEnabled = false;

    public abstract void onEnable();

    public abstract void onDisable();

    public boolean isEnabled() {
        return isEnabled;
    }



}
