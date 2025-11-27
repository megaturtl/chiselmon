package cc.turtl.cobbleaid.mixin.accessor;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.MarkingsWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PCGUI.class)
public interface PCGUIAccessor {

    @Accessor(value = "isPreviewInParty", remap = false)
    Boolean getIsPreviewInParty();
    
    @Accessor(value = "modelWidget", remap = false)
    void setModelWidget(ModelWidget widget);

    @Accessor(value = "markingsWidget", remap = false)
    MarkingsWidget getMarkingsWidget();

    @Invoker(value = "saveMarkings", remap = false)
    void invokeSaveMarkings(boolean save);
}