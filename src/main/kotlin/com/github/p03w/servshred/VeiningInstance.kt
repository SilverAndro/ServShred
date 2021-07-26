package com.github.p03w.servshred

import net.minecraft.block.Block
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

data class VeiningInstance(
    val world: ServerWorld,
    val origin: BlockPos,
    val miner: ServerPlayerEntity,
    val toMine: Block,
    val floodFillPoints: MutableSet<BlockPos> = mutableSetOf(origin),
    var remainingBlocks: Int = ServShredMain.config.maxBlocks
)
