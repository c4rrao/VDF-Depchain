package pt.tecnico.ulisboa.consensus.pow_plus_posw.pow;

import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;

public class PoWValidator {
    
    public boolean validatePoWProof(HybridBlock block, Integer difficulty) {
        // Recalculate block hash
        String calculatedHash = block.computeBlockHash(true);
        
        // Check if calculated hash matches stored hash
        if (!calculatedHash.equals(block.getPOWHash())) {
            System.out.println("Invalid PoW: Hash mismatch");
            return false;
        }
        
        if (block.getDifficulty().compareTo(difficulty) < 0) {
            System.out.println("Invalid PoW: Block difficulty is lower than expected");
            return false; // Block difficulty is lower than expected
        }
        
        return block.meetsDifficulty();
    }
}
