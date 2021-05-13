# BasicMinestomServer

This an ***EXTREMELY*** basic Minestom server in that it produces a jar that
will run, but does little more than that. It does however add a `/stop` command,
so closing down your server is now native to the server itself.

## Environment

This server requires Java 17.

## Mixins

Minestom specifically allows for Mixins, which BasicMinestomServer takes full
advantage of and has provided hooks for easy Mixin injections. But first you'll
need to include BasicMinestomServer as a dependency,
[read to learn more](https://jitpack.io/#Protonull/BasicMinestomServer), or you
can clone this repository and do: `mvn clean install`

Below is an example Mixin:

    import uk.protonull.minestom.server.Server;
    import org.spongepowered.asm.mixin.Mixin;
    import org.spongepowered.asm.mixin.injection.At;
    import org.spongepowered.asm.mixin.injection.Inject;
    import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

    @Mixin(Server.class)
    public class PreServerStartMixin {

        @Inject(method = "calledBeforeStart", at = @At("HEAD"))
        private void runBeforeServerStart(final CallbackInfo ci) {
            System.out.println("Hello from Mixin!!!");
        }

    }
