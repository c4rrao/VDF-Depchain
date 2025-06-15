package pt.tecnico.ulisboa.consensus;

import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.Block;
import pt.tecnico.ulisboa.protocol.ClientReq;

public interface ConsensusInterface {
    /**
     * Propose a new block with given transactions and previous hash
     */
    Block mineBlock(Block previousBlock, List<ClientReq> transactions);
    
    /**
     * Finalize a block with given transactions and previous hash
     */
    void finalizeBlock(Block block);

    /**
     * Validate a block according to consensus rules
     */
    boolean validateBlock(Block block);

    /**
     * Get the consensus type identifier
     */
    default String getConsensusType() {
        return this.getClass().getSimpleName();
    }
}
