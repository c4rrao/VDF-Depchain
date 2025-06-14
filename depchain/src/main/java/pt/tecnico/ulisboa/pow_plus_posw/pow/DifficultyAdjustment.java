package pt.tecnico.ulisboa.pow_plus_posw.pow;

import java.math.BigInteger;
import java.util.List;

import pt.tecnico.ulisboa.blocks.HybridBlock;

public class DifficultyAdjustment {
    private static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 2016; // blocks
    // private static final int TARGET_TIMESPAN = 1209600; // 2 weeks in seconds
    private static final double MAX_ADJUSTMENT_FACTOR = 4.0;
    private static final double MIN_ADJUSTMENT_FACTOR = 0.25;
    
    public BigInteger calculateNewDifficulty(List<HybridBlock> recentBlocks, 
                                           BigInteger currentDifficulty, 
                                           int targetBlockTime) {
        
        if (recentBlocks.size() < DIFFICULTY_ADJUSTMENT_INTERVAL) {
            return currentDifficulty; // Not enough blocks for adjustment
        }
        
        // Get the last DIFFICULTY_ADJUSTMENT_INTERVAL blocks
        List<HybridBlock> adjustmentBlocks = recentBlocks.subList(
            recentBlocks.size() - DIFFICULTY_ADJUSTMENT_INTERVAL, 
            recentBlocks.size()
        );
        
        long firstBlockTime = adjustmentBlocks.get(0).getTimestamp();
        long lastBlockTime = adjustmentBlocks.get(adjustmentBlocks.size() - 1).getTimestamp();
        
        long actualTimespan = lastBlockTime - firstBlockTime;
        long targetTimespan = targetBlockTime * DIFFICULTY_ADJUSTMENT_INTERVAL;
        
        // Calculate adjustment factor
        double adjustmentFactor = (double) targetTimespan / actualTimespan;
        
        // Apply bounds to prevent extreme changes
        adjustmentFactor = Math.max(MIN_ADJUSTMENT_FACTOR, 
                          Math.min(MAX_ADJUSTMENT_FACTOR, adjustmentFactor));
        
        // Calculate new difficulty
        BigInteger newDifficulty = currentDifficulty.multiply(
            BigInteger.valueOf((long) (adjustmentFactor * 1000000))
        ).divide(BigInteger.valueOf(1000000));
        
        System.out.println("Difficulty adjusted from " + currentDifficulty + 
                          " to " + newDifficulty + 
                          " (factor: " + adjustmentFactor + ")");
        
        return newDifficulty;
    }
}
