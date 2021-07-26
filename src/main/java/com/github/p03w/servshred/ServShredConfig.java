package com.github.p03w.servshred;

import mc.microconfig.Comment;
import mc.microconfig.ConfigData;

public class ServShredConfig implements ConfigData {
    @Comment("The max amount of blocks that can be scanned")
    public int maxBlocks = 64;
    
    @Comment("If blocks should break all at once or progressively break over time (better on lower end servers or with higher maxBlocksMined counts)")
    public boolean progressiveMining = false;
    
    @Comment("If blocks should be checked at diagonals")
    public boolean diagonalMining = false;
    
    @Comment("What shifting should do\nENABLE - Shifting is required to vein mine\nDISABLE - Shifting turns off vein mining\nNOTHING - Vein mining is always on")
    public ShiftBehavior shiftBehavior = ShiftBehavior.ENABLE;
    
    public ExhaustBehavior miningCost = new ExhaustBehavior();
    
    public static class ExhaustBehavior implements ConfigData {
        @Comment("What to do if the player runs out of exhaustion to spend\nSTOP - Stops the mining\nALLOW - Continues mining\nBLOOD - Starts taking health from the player")
        public NoExhaustionBehavior noExhaustionLeftBehavior = NoExhaustionBehavior.STOP;
        
        @Comment("Amount of exhaustion per block mined")
        public float exhaustionPerBlock = 0.01f;
        
        @Comment("How much health to take when out of exhaustion")
        public float bloodCost = 0.05f;
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
