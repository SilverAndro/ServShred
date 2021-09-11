package com.github.p03w.servshred

import mc.microconfig.MicroConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction


class ServShredMain : ModInitializer {
    override fun onInitialize() {
        println("ServShred is starting")

        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, _ ->
            if (world is ServerWorld && player is ServerPlayerEntity && !isMining) {
                when (config.shiftBehavior) {
                    ServShredConfig.ShiftBehavior.ENABLE -> if (!player.isSneaking) return@register
                    ServShredConfig.ShiftBehavior.DISABLE -> if (player.isSneaking) return@register
                    else -> {}
                }
                if (player.mainHandStack.isSuitableFor(state) || player.isCreative) {
                    val tag = BlockTags.getTagGroup().getTagOrEmpty(Identifier("servshred:veinmine"))
                    if (tag.contains(state.block)) {
                        activeVeining.add(
                            VeiningInstance(
                                world,
                                pos,
                                player,
                                state.block
                            )
                        )
                    }
                }
            }
        }

        ServerTickEvents.END_WORLD_TICK.register { world ->
            activeVeining = activeVeining.filterNotTo(mutableListOf()) { it.miner.isDisconnected || it.floodFillPoints.size <= 0 }

            isMining = true
            activeVeining.forEach { instance ->
                if (instance.world == world) {
                    do {
                        val next = mutableSetOf<BlockPos>()
                        val cache = mutableSetOf<BlockPos>()
                        instance.floodFillPoints.forEach { pos ->
                            forEachDirection(pos) eachDir@{ offset ->
                                if (instance.remainingBlocks > 0 && !cache.contains(offset)) {
                                    cache.add(offset)
                                    val state = world.getBlockState(offset)
                                    if (state.block == instance.toMine) {
                                        if (!instance.floodFillPoints.contains(offset)) {
                                            next.add(offset)
                                        }

                                        val player = instance.miner
                                        val canContinue = PlayerBlockBreakEvents
                                            .BEFORE
                                            .invoker()
                                            .beforeBlockBreak(world, player, offset, state, world.getBlockEntity(offset))
                                        if (!canContinue) {
                                            return@eachDir
                                        }

                                        if (
                                            (player.mainHandStack.isSuitableFor(state) || player.isCreative) &&
                                            player.interactionManager.tryBreakBlock(offset)
                                        ) {
                                            instance.remainingBlocks--
                                            if (!player.isCreative) {
                                                if (!afterMine(instance, state)) {
                                                    instance.remainingBlocks = 0
                                                }
                                            }
                                        } else {
                                            next.remove(offset)
                                            return@eachDir
                                        }
                                    }
                                }
                            }
                        }
                        instance.floodFillPoints.clear()
                        instance.floodFillPoints.addAll(next)
                    } while (instance.floodFillPoints.size > 0 && !config.progressiveMining)
                }
            }
            isMining = false
        }
    }

    private inline fun forEachDirection(pos: BlockPos, action: (BlockPos)->Unit) {
        Direction.values().forEach {
            action(pos.offset(it))
        }
        if (config.diagonalMining) {
            action(pos.add(1, 1, 0))
            action(pos.add(1, -1, 0))
            action(pos.add(-1, 1, 0))
            action(pos.add(-1, -1, 0))
            action(pos.add(0, 1, 1))
            action(pos.add(0, -1, 1))
            action(pos.add(0, 1, -1))
            action(pos.add(0, -1, -1))
        }
    }

    private fun afterMine(instance: VeiningInstance, state: BlockState): Boolean {
        val miner = instance.miner
        miner.incrementStat(Stats.MINED.getOrCreateStat(state.block))
        return if (miner.hungerManager.foodLevel > 0) {
            miner.addExhaustion(config.miningCost.exhaustionPerBlock)
            true
        } else {
            when (config.miningCost.noExhaustionLeftBehavior) {
                ServShredConfig.NoExhaustionBehavior.ALLOW -> true
                ServShredConfig.NoExhaustionBehavior.STOP -> false
                ServShredConfig.NoExhaustionBehavior.BLOOD -> {
                    miner.health -= config.miningCost.bloodCost
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        val config: ServShredConfig = MicroConfig.getOrCreate("servshred", ServShredConfig())

        var isMining = false
        var activeVeining: MutableList<VeiningInstance> = mutableListOf()
    }
}
