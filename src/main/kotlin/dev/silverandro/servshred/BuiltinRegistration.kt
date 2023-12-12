package dev.silverandro.servshred

import net.minecraft.util.Identifier
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.resource.loader.api.ResourceLoader
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType

object BuiltinRegistration {
    fun register(container: ModContainer) {
        ResourceLoader.registerBuiltinResourcePack(Identifier("servshred:default"), container, ResourcePackActivationType.DEFAULT_ENABLED)
        ResourceLoader.registerBuiltinResourcePack(Identifier("servshred:blend_everything"), container, ResourcePackActivationType.NORMAL)
        ResourceLoader.registerBuiltinResourcePack(Identifier("servshred:logs"), container, ResourcePackActivationType.NORMAL)
    }
}