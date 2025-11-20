package cc.turtl.cobbleaid.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 * A generic suggestion provider that suggests names from a pre-defined list.
 */
public class ListSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    private final Collection<String> namesToSuggest;

    // constructor to inject the list of names to be suggested
    public ListSuggestionProvider(Collection<String> namesToSuggest) {
        // use an unmodifiable view for safety, or an empty collection if null
        this.namesToSuggest =
                namesToSuggest != null
                        ? Collections.unmodifiableCollection(namesToSuggest)
                        : Collections.emptyList();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder)
            throws CommandSyntaxException {
        for (String name : namesToSuggest) {
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}
