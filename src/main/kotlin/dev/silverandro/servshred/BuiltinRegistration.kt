package dev.silverandro.servshred

import net.minecraft.util.Identifier
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.resource.loader.api.PackActivationType
import org.quiltmc.qsl.resource.loader.api.ResourceLoader

object BuiltinRegistration {
    fun register(container: ModContainer) {
        ResourceLoader.registerBuiltinPack(Identifier("servshred:default"), container, PackActivationType.DEFAULT_ENABLED)
        ResourceLoader.registerBuiltinPack(Identifier("servshred:blend_everything"), container, PackActivationType.NORMAL)
        ResourceLoader.registerBuiltinPack(Identifier("servshred:logs"), container, PackActivationType.NORMAL)
    }
}