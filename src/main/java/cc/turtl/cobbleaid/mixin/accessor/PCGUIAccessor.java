package cc.turtl.cobbleaid.mixin.accessor;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.MarkingsWidget; // Note: MarkingsWidget is imported from 'summary.widgets' in the source!
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PCGUI.class)
public interface PCGUIAccessor {

    // --- Accessors for Fields (Getters/Setters) ---

    // Accessor for the private field 'isPreviewInParty' (used as a getter)
    // Source: var isPreviewInParty: Boolean? = null
    @Accessor(value = "isPreviewInParty", remap = false)
    Boolean getIsPreviewInParty();
    
    // Accessor for the private field 'modelWidget' (used as a setter)
    // Source: private var modelWidget: ModelWidget? = null
    // This solves your last error about setModelWidget(ModelWidget) being undefined!
    @Accessor(value = "modelWidget", remap = false)
    void setModelWidget(ModelWidget widget);

    // Accessor for the private field 'markingsWidget' (used as a getter)
    // Source: private lateinit var markingsWidget: MarkingsWidget
    // Mixin will generate 'getMarkingsWidget()' for this field.
    @Accessor(value = "markingsWidget", remap = false)
    MarkingsWidget getMarkingsWidget();


    // --- Invoker for Private Method ---

    // Invoker for the private method: saveMarkings(boolean)
    // Source: private fun saveMarkings(isParty: Boolean = false)
    @Invoker(value = "saveMarkings", remap = false)
    void invokeSaveMarkings(boolean save);
}