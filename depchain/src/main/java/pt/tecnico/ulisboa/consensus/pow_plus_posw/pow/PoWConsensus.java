package pt.tecnico.ulisboa.consensus.pow_plus_posw.pow;

import java.math.BigInteger;
import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.consensus.ConsensusInterface;
import pt.tecnico.ulisboa.protocol.ClientReq;

public class PoWConsensus implements ConsensusInterface {
    private final DifficultyAdjustment difficultyAdjuster;
    private final PoWValidator validator;
    private BigInteger currentDifficulty;
    private static final int TARGET_BLOCK_TIME = 600; // 10 minutes in seconds
    
    public PoWConsensus() {
        this.difficultyAdjuster = new DifficultyAdjustment();
        this.validator = new PoWValidator();
        this.currentDifficulty = BigInteger.valueOf(0x1d00ffff); // Initial difficulty
    }
    
    @Override
    public void start() {
        System.out.println("PoW Consensus started with initial difficulty: " + currentDifficulty);
    }
    
    @Override
    public HybridBlock proposeBlock(List<ClientReq> transactions, String previousHash) {
        // Create HybridBlock with proper constructor parameters
        int height = calculateNextHeight(previousHash);
        int difficultyLevel = calculateDifficultyLevel(currentDifficulty);
        
        HybridBlock block = new HybridBlock(previousHash, height, transactions, difficultyLevel);
        
        // Start PoW mining
        return mineBlock(block);
    }
    
    @Override
    public void finalizeBlock(HybridBlock block) {
        // PoW doesn't finalize blocks, it proposes them
        // Finalization is handled by PoSW
        System.out.println("PoW block ready for PoSW finalization");
    }
    
    @Override
    public boolean validateBlock(HybridBlock block) {
        return validator.validatePoWProof(block, currentDifficulty);
    }
    
    @Override
    public boolean isReadyForConsensus() {
        return difficultyAdjuster != null && validator != null;
    }
    
    private HybridBlock mineBlock(HybridBlock block) {
        System.out.println("Starting PoW mining with difficulty: " + block.getDifficulty());
        
        while (!block.meetsDifficulty()) {
            block.incrementNonce();
            
            // Recompute hash with new nonce
            String newHash = block.computeBlockHash();
            block.setHash(newHash);
            
            // Optional: Add periodic progress reporting
            if (block.getNonce() % 100000 == 0) {
                System.out.println("Mining progress: " + block.getNonce() + " attempts");
            }
        }
        
        System.out.println("Block mined! Hash: " + block.getHash() + 
                         ", Nonce: " + block.getNonce());
        return block;
    }
    
    private int calculateNextHeight(String previousHash) {
        // You'll need to implement this based on your blockchain structure
        // For now, returning a placeholder
        return 1; // This should be calculated from the blockchain
    }
    
    private int calculateDifficultyLevel(BigInteger difficulty) {
        // Convert BigInteger difficulty to number of leading zeros
        return Math.max(1, (int) (Math.log(difficulty.doubleValue()) / Math.log(16)));
    }
    
    public void adjustDifficulty(List<HybridBlock> recentBlocks) {
        currentDifficulty = difficultyAdjuster.calculateNewDifficulty(
            recentBlocks, currentDifficulty, TARGET_BLOCK_TIME);
    }
    
    public BigInteger getCurrentDifficulty() {
        return currentDifficulty;
    }
}
