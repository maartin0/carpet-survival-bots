package me.maartin0.CarpetSurvivalBots.mixin;

import me.maartin0.CarpetSurvivalBots.BotCommand;
import me.maartin0.CarpetSurvivalBots.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class InitMixin {
    @Shadow
    public abstract void stop(boolean waitForShutdown);

    @Shadow public abstract CommandFunctionManager getCommandFunctionManager();

    @Inject(at = @At("HEAD"), method = "runServer")
    private void init(CallbackInfo info) {
        Main.stop = () -> stop(true);
        BotCommand.register(getCommandFunctionManager().getDispatcher());
    }
}
