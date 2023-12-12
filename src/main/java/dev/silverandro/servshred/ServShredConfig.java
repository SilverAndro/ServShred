package dev.silverandro.servshred;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.values.TrackedValue;

public class ServShredConfig extends ReflectiveConfig {
    @Comment("The max amount of blocks that can be scanned")
    public final TrackedValue<Integer> maxBlocks = value(64);

    @Comment("If blocks should break all at once or progressively break over time (better on lower end servers or with higher maxBlocksMined counts)")
    public final TrackedValue<Boolean> progressiveMining = value(false);

    @Comment("If blocks should be checked at diagonals")
    public final TrackedValue<Boolean> diagonalMining = value(false);

    @Comment("""
    What shifting should do
    ENABLE - Shifting is required to vein mine
    DISABLE - Shifting turns off vein mining
    NOTHING - Vein mining is always on
    Note that clients can override this for themselves.
    """)
    public final TrackedValue<ShiftBehavior> shiftBehavior = value(ShiftBehavior.ENABLE);

    public final ExhaustBehavior miningCost = new ExhaustBehavior();

    public static class ExhaustBehavior extends Section {
        @Comment("""
        What to do if the player runs out of exhaustion to spend
        STOP - Stops the mining
        ALLOW - Continues mining
        BLOOD - Starts taking health from the player
        """)
        public final TrackedValue<NoExhaustionBehavior> noExhaustionLeftBehavior = value(NoExhaustionBehavior.STOP);

        @Comment("Amount of exhaustion per block mined")
        public final TrackedValue<Float> exhaustionPerBlock = value(0.05f);

        @Comment("How much health to take when out of exhaustion (if noExhaustionLeftBehavior set to BLOOD)")
        public final TrackedValue<Float> bloodCost = value(0.5f);
    }
    
    public enum NoExhaustionBehavior {
        ALLOW,
        STOP,
        BLOOD
    }
    
    public enum ShiftBehavior {
        ENABLE,
        DISABLE,
        @SuppressWarnings("unused") NOTHING
    }
}
