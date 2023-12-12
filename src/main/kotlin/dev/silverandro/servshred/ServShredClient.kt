package dev.silverandro.servshred

import com.mojang.blaze3d.platform.InputUtil
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBind
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.config.v2.QuiltConfig
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking

object ServShredClient : ClientModInitializer {
    private val keyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBind(
            "key.servshred.mine",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.keyCode,
            "category.servshred"
        ).also { it.setBoundKey(InputUtil.UNKNOWN_KEY) }
    )

    var forceSend = true
    var lastStatus = false
    var wasUnbound = true

    @JvmField
    val config = QuiltConfig.create(
        "servershred",
        "client",
        ServShredClientConfig::class.java
    )

    override fun onInitializeClient(mod: ModContainer) {
        ClientTickEvents.END.register {
            if (it.player != null) {
                val status = when (config.keybindBehavior.value()) {
                    ServShredConfig.ShiftBehavior.ENABLE -> keyBinding.isPressed
                    ServShredConfig.ShiftBehavior.DISABLE -> keyBinding.isPressed
                    ServShredConfig.ShiftBehavior.NOTHING -> true
                    else -> throw IllegalStateException("Quilt Config some how gave us a null enum :/")
                }

                if (forceSend || (wasUnbound != keyBinding.isUnbound ) || (status != lastStatus)) {
                    lastStatus = status
                    wasUnbound = keyBinding.isUnbound
                    forceSend = false

                    val buf = PacketByteBufs.create()
                    buf.writeBoolean(wasUnbound)
                    buf.writeBoolean(status)
                    ClientPlayNetworking.send(ServShredMain.STATUS_PACKET_ID, buf)
                }
            }
        }

        ServerLifecycleEvents.READY.register {
            forceSend = true
        }
    }
}