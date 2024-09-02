package eu.koboo.minestom.api.world;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class World {

    String name;
    InstanceContainer instanceContainer;
    DimensionType dimensionType;

}
