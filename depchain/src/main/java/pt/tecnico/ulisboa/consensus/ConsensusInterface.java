package pt.tecnico.ulisboa.consensus;

import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.protocol.ClientReq;

public interface ConsensusInterface {
    
    /**
     * Start the consensus mechanism
     */
    void start();
    
    /**
     * Propose a new block with given transactions and previous hash
     */
    HybridBlock proposeBlock(List<ClientReq> transactions, String previousHash);
    
    /**
     * Finalize a block (add VDF proof, etc.)
     */
    void finalizeBlock(HybridBlock block);
    
    /**
     * Validate a block according to consensus rules
     */
    boolean validateBlock(HybridBlock block);
    
    /**
     * Check if the consensus mechanism is ready to process blocks
     */
    boolean isReadyForConsensus();
    
    /**
     * Get the consensus type identifier
     */
    default String getConsensusType() {
        return this.getClass().getSimpleName();
    }
}
