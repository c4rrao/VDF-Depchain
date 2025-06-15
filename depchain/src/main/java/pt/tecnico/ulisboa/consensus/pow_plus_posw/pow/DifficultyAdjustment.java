package pt.tecnico.ulisboa.consensus.pow_plus_posw.pow;

import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;

public class DifficultyAdjustment {
    public static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 2016; // blocks
    private static final int TARGET_BLOCK_TIME = 600; // 10 minutes in seconds
    private static final double MAX_ADJUSTMENT_FACTOR = 4.0;
    private static final double MIN_ADJUSTMENT_FACTOR = 0.25;
    
    public Integer calculateNewDifficulty(List<HybridBlock> recentBlocks, 
                                        Integer currentDifficulty) {
        
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
        long targetTimespan = TARGET_BLOCK_TIME * DIFFICULTY_ADJUSTMENT_INTERVAL;
        
        // Calculate adjustment factor
        double adjustmentFactor = (double) targetTimespan / actualTimespan;
        
        // Apply bounds to prevent extreme changes
        adjustmentFactor = Math.max(MIN_ADJUSTMENT_FACTOR, 
                        Math.min(MAX_ADJUSTMENT_FACTOR, adjustmentFactor));
        
        // Calculate new difficulty (number of leading zeros)
        int newDifficulty;
        
        if (adjustmentFactor > 1.0) {
            // Blocks are being mined too slowly, increase difficulty (more zeros)
            newDifficulty = currentDifficulty + 1;
        } else if (adjustmentFactor < 1.0) {
            // Blocks are being mined too fast, decrease difficulty (fewer zeros)
            newDifficulty = Math.max(1, currentDifficulty - 1);
        } else {
            // Keep current difficulty
            newDifficulty = currentDifficulty;
        }
        
        // Ensure minimum difficulty of 1 (at least 1 leading zero)
        newDifficulty = Math.max(1, newDifficulty);
        
        System.out.println("Difficulty adjusted from " + currentDifficulty + 
                        " to " + newDifficulty + " leading zeros" +
                        " (factor: " + adjustmentFactor + ")");
        
        return newDifficulty;
    }

}
