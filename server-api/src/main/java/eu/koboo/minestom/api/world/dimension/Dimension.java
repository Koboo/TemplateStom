package eu.koboo.minestom.api.world.dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minestom.server.world.DimensionType;

@Getter
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public enum Dimension {

    OVERWORLD("minecraft:overworld", DimensionType.builder().build()),
    NETHER("minecraft:the_nether", DimensionType.builder().ultrawarm(true).coordinateScale(8.0f).hasSkylight(false).hasCeiling(true).piglinSafe(true).bedWorks(false).respawnAnchorWorks(true).hasRaids(false).effects("#minecraft:infiniburn_overworld").build()),
    END("minecraft:the_end", DimensionType.builder().natural(false).coordinateScale(1.0f).hasSkylight(false).hasCeiling(false).piglinSafe(true).bedWorks(false).respawnAnchorWorks(true).hasRaids(false).build());

    String name;
    DimensionType dimensionType;

}
