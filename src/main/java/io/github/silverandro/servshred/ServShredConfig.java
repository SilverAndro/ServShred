package io.github.silverandro.servshred;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;

public class ServShredConfig extends WrappedConfig {
    @Comment("The max amount of blocks that can be scanned")
    public final int maxBlocks = 64;
    
    @Comment("If blocks should break all at once or progressively break over time (better on lower end servers or with higher maxBlocksMined counts)")
    public final boolean progressiveMining = false;
    
    @Comment("If blocks should be checked at diagonals")
    public final boolean diagonalMining = false;
    
    @Comment("What shifting should do\nENABLE - Shifting is required to vein mine\nDISABLE - Shifting turns off vein mining\nNOTHING - Vein mining is always on")
    public final ShiftBehavior shiftBehavior = ShiftBehavior.ENABLE;
    
    public final ExhaustBehavior miningCost = new ExhaustBehavior();
    
    public static class ExhaustBehavior implements Section {
        @Comment("What to do if the player runs out of exhaustion to spend\nSTOP - Stops the mining\nALLOW - Continues mining\nBLOOD - Starts taking health from the player")
        public final NoExhaustionBehavior noExhaustionLeftBehavior = NoExhaustionBehavior.STOP;
        
        @Comment("Amount of exhaustion per block mined")
        public final float exhaustionPerBlock = 0.05f;
        
        @Comment("How much health to take when out of exhaustion (if noExhaustionLeftBehavior set to BLOOD)")
        public final float bloodCost = 0.5f;
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
