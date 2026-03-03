package cc.turtl.chiselmon.fabric.mixin;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.ChiselmonPacks;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public class MixinPackRepository {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void chiselmon$addUserPackSource(CallbackInfo ci) {
        // Create a new mutable set from the existing sources
        Set<RepositorySource> newSources = new HashSet<>(this.sources);

        // Create the Custom Wallpapers pack structure
        ChiselmonPacks.getOrCreateCustomWallpaperDir();

        // Add config folder as a pack source
        newSources.add(new FolderRepositorySource(
                ChiselmonConstants.CONFIG_PATH,
                PackType.CLIENT_RESOURCES,
                PackSource.BUILT_IN,
                new DirectoryValidator(path -> true)));

        // Reassign the mutated sources set
        this.sources = ImmutableSet.copyOf(newSources);
    }
}