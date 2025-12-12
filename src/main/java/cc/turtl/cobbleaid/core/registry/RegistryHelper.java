package cc.turtl.cobbleaid.core.registry;

import cc.turtl.cobbleaid.config.CobbleAidLogger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 * Helper class for registering client-side components.
 * <p>
 * This class provides utility methods for registering commands, event listeners,
 * and other client-side resources in a consistent manner.
 * </p>
 */
public class RegistryHelper {
    
    private final CobbleAidLogger logger;
    
    public RegistryHelper(CobbleAidLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Register a client command with helpful logging.
     * 
     * @param dispatcher the command dispatcher
     * @param command the command to register
     * @param commandName the name of the command for logging
     */
    public void registerCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            LiteralArgumentBuilder<FabricClientCommandSource> command,
            String commandName) {
        try {
            dispatcher.register(command);
            logger.debug("Registered command: {}", commandName);
        } catch (Exception e) {
            logger.error("Failed to register command: {}", commandName, e);
        }
    }
    
    /**
     * Execute a registration task with error handling and logging.
     * 
     * @param taskName descriptive name of the task
     * @param task the task to execute
     */
    public void executeRegistration(String taskName, Runnable task) {
        try {
            logger.debug("Executing registration: {}", taskName);
            task.run();
            logger.debug("Registration complete: {}", taskName);
        } catch (Exception e) {
            logger.error("Registration failed: {}", taskName, e);
        }
    }
}
