package dev.silverandro.servshred;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.values.TrackedValue;

public class ServShredClientConfig extends ReflectiveConfig {
    @Comment("""
    What the keybind should do
    ENABLE - Keybind is required to vein mine
    DISABLE - Keybind turns off vein mining
    NOTHING - Vein mining is always on
    This value does nothing if the keybind is unbound (the default), instead falling back to the server
    """)
    public final TrackedValue<ServShredConfig.ShiftBehavior> keybindBehavior = value(ServShredConfig.ShiftBehavior.ENABLE);
}
