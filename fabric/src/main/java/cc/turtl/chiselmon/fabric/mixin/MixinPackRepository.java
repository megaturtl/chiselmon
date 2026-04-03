package cc.turtl.chiselmon.fabric.mixin;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.ChiselmonPacks;
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

import java.util.Set;

@Mixin(PackRepository.class)
public class MixinPackRepository {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void chiselmon$addUserPackSource(CallbackInfo ci) {
        // Create a mutable copy (using LinkedHashSet to maintain vanilla/mod pack order)
        Set<RepositorySource> newSources = new java.util.LinkedHashSet<>(this.sources);

        ChiselmonPacks.getOrCreateCustomWallpaperDir();

        // Add custom source
        newSources.add(new FolderRepositorySource(
                ChiselmonConstants.CONFIG_PATH,
                PackType.CLIENT_RESOURCES,
                PackSource.BUILT_IN,
                new DirectoryValidator(path -> true)));

        // Reassign as a mutable set
        this.sources = newSources;
    }
}