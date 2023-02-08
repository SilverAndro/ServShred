package io.github.silverandro.servshred

import net.minecraft.block.Block
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

data class VeiningInstance(
    @JvmField
    val world: ServerWorld,
    @JvmField
    val origin: BlockPos,
    @JvmField
    val miner: ServerPlayerEntity,
    @JvmField
    val toMine: Block,
    @JvmField
    val floodFillPoints: MutableSet<BlockPos> = mutableSetOf(origin),
    @JvmField
    var remainingBlocks: Int = ServShredMain.config.maxBlocks
)
