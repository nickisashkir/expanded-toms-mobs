package de.tomalbrc.toms_mobs.mixins.datafix;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import de.tomalbrc.toms_mobs.util.NautilusRenameFix;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.filefix.FileFixerUpper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataFixers.class)
public abstract class DatafixersMixin {

    @Inject(
        method = "addFixers",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/fixes/OptionsMusicToastFix;<init>(Lcom/mojang/datafixers/schemas/Schema;Z)V")
    )
    private static void tm$addNaughtyFixer(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci, @Local(name = "v4658") Schema v4658) {
        fixerUpper.addFixer(new NautilusRenameFix(v4658, true));
    }
}
