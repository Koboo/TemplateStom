package eu.koboo.minestom.server.listener;

import eu.koboo.minestom.server.ServerImpl;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.InstanceContainer;

@AllArgsConstructor
public class PlayerLoginListener implements Consumer<PlayerLoginEvent> {

    private final InstanceContainer instanceContainer;

    @Override
    public void accept(PlayerLoginEvent event) {
        event.setSpawningInstance(instanceContainer);
        event.getPlayer().setRespawnPoint(new Pos(0, 44, 0));
        if(ServerImpl.getInstance().isOperator(event.getPlayer())) {
            event.getPlayer().setPermissionLevel(4);
        }
    }
}
