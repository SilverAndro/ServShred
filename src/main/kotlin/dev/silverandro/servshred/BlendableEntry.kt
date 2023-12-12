package dev.silverandro.servshred

import com.mojang.serialization.Codec
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagEntry
import net.minecraft.util.Identifier
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment
import kotlin.jvm.optionals.getOrNull

data class BlendableEntry(val entries: List<TagEntry>) {
    val blocks by lazy {
        buildList {
            entries.forEach {
                if (!it.build(lookup) { add(it) }) {
                    throw IllegalStateException("Failed to lookup blendable entry $it!")
                }
            }
        }
    }

    companion object {
        val lookup = object : TagEntry.Lookup<Block> {
            override fun getElement(id: Identifier): Block? {
                return Registries.BLOCK.getOrEmpty(id).getOrNull()
            }

            override fun getTag(id: Identifier): MutableCollection<Block>? {
                val out = mutableListOf<Block>()
                Registries.BLOCK.tags.forEach {
                    val key = it.first
                    if (key.id == id) {
                        val holderSet = it.second
                        val mapped = holderSet.tagOrContents.mapBoth(
                            { Registries.BLOCK.getTagOrEmpty(it).toList().map { it.value() } },
                            { it.map { it.value() } }
                        ).let { it.left().or { it.right() }.get() }
                        out.addAll(mapped)
                        return@forEach
                    }
                }
                return out.ifEmpty { null }
            }
        }

        fun shouldBlendWith(blockA: Block, blockB: Block): Boolean {
            val entry = REA.get(blockA).getOrNull() ?: return false
            return entry.blocks.any { it == blockB }
        }

        val REA: RegistryEntryAttachment<Block, BlendableEntry> = RegistryEntryAttachment.builder(
            Registries.BLOCK,
            Identifier("servshred", "blendable"),
            BlendableEntry::class.java,
            Codec.list(TagEntry.CODEC).xmap(
                { BlendableEntry(it) },
                { it.entries }
            )
        ).build()
    }
}
