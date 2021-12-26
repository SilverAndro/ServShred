package com.github.p03w.servshred

import mc.microconfig.MicroConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction


class ServShredMain : ModInitializer {
    fun blockIsMinable(blockState: BlockState): Boolean {
        val tag = BlockTags.getTagGroup().getTagOrEmpty(Identifier("servshred:veinmine"))
        return tag.contains(blockState.block)
    }

    override fun onInitialize() {
        println("ServShred is starting")

        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, _ ->
            // Make sure we're on the server and this isn't the result of a player vein-mining something
            if (world is ServerWorld && player is ServerPlayerEntity && !isMining) {
                // Don't try to vein-mine if the player is doing an action that would disable it
                when (config.shiftBehavior) {
                    ServShredConfig.ShiftBehavior.ENABLE -> if (!player.isSneaking) return@register
                    ServShredConfig.ShiftBehavior.DISABLE -> if (player.isSneaking) return@register
                    else -> {}
                }
                // Check the tool is the right one for the block, or is in creative
                if (player.mainHandStack.isSuitableFor(state) || player.isCreative) {
                    // If so, check if it's in the tag of blocks allowed to be vein-mined
                    if (blockIsMinable(state)) {
                        // Start the vein-mining action
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
            // Remove any points where the player has left or theres nothing left to mine
            activeVeining = activeVeining.filterNotTo(mutableListOf()) { it.miner.isDisconnected || it.floodFillPoints.size <= 0 }

            // Set the flag to ignore mining events
            isMining = true
            activeVeining.forEach { instance ->
                // If the instance belongs to this world
                if (instance.world == world) {
                    do {
                        // Position tracking sets
                        val next = mutableSetOf<BlockPos>()
                        val cache = mutableSetOf<BlockPos>()
                        // For every point from the previous iteration
                        instance.floodFillPoints.forEach { pos ->
                            // On every direction to check
                            forEachDirection(pos) eachDir@{ offset ->
                                // If we have remaining buffer of blocks that can be mined and havent already checked here
                                if (instance.remainingBlocks > 0 && !cache.contains(offset)) {
                                    // Mark it as checked and get it
                                    cache.add(offset)
                                    val state = world.getBlockState(offset)
                                    // If it matches the block we started with OR
                                    if (state.block == instance.toMine || (config.allowBlendingBlocks && blockIsMinable(state))) {
                                        // Add it to the list of points for next time
                                        if (!instance.floodFillPoints.contains(offset)) {
                                            next.add(offset)
                                        }

                                        val player = instance.miner
                                        // Make sure that no one is preventing this block from breaking (claim mods, ect.)
                                        val canContinue = PlayerBlockBreakEvents
                                            .BEFORE
                                            .invoker()
                                            .beforeBlockBreak(world, player, offset, state, world.getBlockEntity(offset))
                                        if (!canContinue) {
                                            return@eachDir
                                        }

                                        // If it's appropriate to mine, and trying to mine it succeeds
                                        if (
                                            (player.mainHandStack.isSuitableFor(state) || player.isCreative) &&
                                            player.interactionManager.tryBreakBlock(offset)
                                        ) {
                                            // Count down one and apply after effects if not in creative
                                            instance.remainingBlocks--
                                            if (!player.isCreative) {
                                                if (!afterMine(instance, state)) {
                                                    instance.remainingBlocks = 0
                                                }
                                            }
                                        } else {
                                            // If we failed to mine it for some reason, remove it from ones to check next time
                                            next.remove(offset)
                                            return@eachDir
                                        }
                                    }
                                }
                            }
                        }
                        // Copy the next iteration in
                        instance.floodFillPoints.clear()
                        instance.floodFillPoints.addAll(next)
                    // Do it again if we have points left and progressive mining is off
                    } while (instance.floodFillPoints.size > 0 && !config.progressiveMining)
                }
            }
            // Mark it safe to accept mining events again
            isMining = false
        }
    }

    private inline fun forEachDirection(pos: BlockPos, action: (BlockPos)->Unit) {
        // Manages block breaking by calling [action] on all the positions specified by the config
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
        // Grant stats and preform exhaustion costs
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
                    miner.damage(DamageSource.STARVE, config.miningCost.bloodCost)
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        @JvmField
        val config: ServShredConfig = MicroConfig.getOrCreate("servshred", ServShredConfig())

        @JvmField
        var isMining = false
        @JvmField
        var activeVeining: MutableList<VeiningInstance> = mutableListOf()
    }
}
