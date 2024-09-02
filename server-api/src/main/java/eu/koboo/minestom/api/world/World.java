package eu.koboo.minestom.api.world;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.simpleyaml.configuration.file.YamlFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class World {

    String name;
    InstanceContainer instanceContainer;
    DimensionType dimensionType;

    Pos spawnPoint;

    YamlFile worldConfig;

}
