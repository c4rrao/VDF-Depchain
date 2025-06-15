package pt.tecnico.ulisboa.consensus.pow_plus_posw.pow;

import java.util.List;
import java.util.TreeMap;

import pt.tecnico.ulisboa.blockchain.blocks.Block;
import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.consensus.ConsensusInterface;
import pt.tecnico.ulisboa.protocol.ClientReq;
public class PoWConsensus implements ConsensusInterface {
    private final DifficultyAdjustment difficultyAdjuster;
    private final PoWValidator validator;
    private TreeMap<Integer, Integer> currentDifficulty = new TreeMap<>();
    
    public PoWConsensus() {
        this.difficultyAdjuster = new DifficultyAdjustment();
        this.validator = new PoWValidator();
        // Initialize difficulty for the first block
        currentDifficulty.put(0, Integer.valueOf(2));
    }
    
    @Override
    public boolean validateBlock(Block block) {
        if (block == null || !(block instanceof HybridBlock)) {
            throw new IllegalArgumentException("Block must be a valid HybridBlock");
        }
        HybridBlock hybridBlock = (HybridBlock) block;

        return validator.validatePoWProof(hybridBlock, currentDifficulty.floorEntry(block.getHeight()).getValue());
    }
    
    public HybridBlock mineBlock(Block _previousBlock, List<ClientReq> transactions) {
        if (_previousBlock == null || !(_previousBlock instanceof HybridBlock)) {
            throw new IllegalArgumentException("Previous block must be a valid HybridBlock");
        }

        HybridBlock previousBlock = (HybridBlock) _previousBlock;

        HybridBlock block = new HybridBlock(
            previousBlock.getHash(), 
            previousBlock.getHeight() + 1, 
            transactions, 
            currentDifficulty.lastEntry().getValue()
        );

        // We chose to start with a nonce of 0 but it's not mandatory
        block.setNonce(0);
        
        // we could do this in parallel (real world mining) but for academic purposes,
        // we will do it sequentially because we're not focusing in exploring POW mining algorithms
        while (!meetsDifficulty(block)) {
            // Add periodic progress reporting
            if (block.getNonce() % 100000 == 0) {
                System.out.println("Mining progress: " + block.getNonce() + " attempts");
            }
            // We're incrementing the nonce until we find a valid hash, but other strategies could be used
            block.setNonce(block.getNonce() + 1);    
        }

        // Recompute hash with new nonce
        String newHash = block.computeBlockHash(true);
        block.setPOWHash(newHash);
        block.setHash(newHash);
        
        System.out.println("Block mined! Hash: " + block.getHash() + 
                         ", Nonce: " + block.getNonce());
        return block;
    }
    
    @Override
    public void finalizeBlock(Block previousBlock) {
        throw new UnsupportedOperationException("PoWConsensus does not support finalizing blocks directly. Use for mining instead.");
    }

    public void adjustDifficulty(List<HybridBlock> recentBlocks) {
        Integer new_difficulty = difficultyAdjuster.calculateNewDifficulty(
            recentBlocks, getCurrentDifficulty());

        currentDifficulty.put(recentBlocks.get(recentBlocks.size() - 1).getHeight(), new_difficulty);
    }
    
    public Integer getCurrentDifficulty() {
        return currentDifficulty.lastEntry().getValue();
    }

    private boolean meetsDifficulty(HybridBlock block) {
        Integer difficulty = currentDifficulty.floorEntry(block.getHeight()).getValue();
        
        String hash = block.computeBlockHash(true);
        String requiredZeros = "0".repeat(difficulty); // difficulty as int (number of zeros)

        // System.out.println("Checking if block meets difficulty:\n" + 
        //                    "Hash: " + hash + ",\nRequired Zeros: " + requiredZeros);

        return hash.startsWith(requiredZeros);
    }


}
