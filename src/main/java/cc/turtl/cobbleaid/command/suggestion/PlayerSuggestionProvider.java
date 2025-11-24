package cc.turtl.cobbleaid.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder)
            throws CommandSyntaxException {
        FabricClientCommandSource source = context.getSource();

        Collection<String> playerNames = source.getOnlinePlayerNames();

        for (String playerName : playerNames) {
            builder.suggest(playerName);
        }

        return builder.buildFuture();
    }
}
