package pt.tecnico.ulisboa.pow_plus_posw.hybrid;

import java.util.List;

import pt.tecnico.ulisboa.blocks.HybridBlock;
import pt.tecnico.ulisboa.pow_plus_posw.posw.PoSWConsensus;
import pt.tecnico.ulisboa.pow_plus_posw.pow.PoWConsensus;
import pt.tecnico.ulisboa.protocol.ClientReq;

public class HybridConsensus implements ConsensusInterface {
    private final PoWConsensus powConsensus;
    private final PoSWConsensus poswConsensus;
    private final ConsensusManager manager;
    
    public HybridConsensus() {
        this.powConsensus = new PoWConsensus();
        this.poswConsensus = new PoSWConsensus();
        this.manager = new ConsensusManager();
    }
    
    @Override
    public void start() {
        powConsensus.start();
        poswConsensus.start();
    }
    
    @Override
    public HybridBlock proposeBlock(List<ClientReq> transactions, String previousHash) {
        // Phase 1: PoW Block Proposal
        System.out.println("Phase 1: Starting PoW mining...");
        HybridBlock proposedBlock = powConsensus.proposeBlock(transactions, previousHash);
        
        // Validate PoW
        if (!powConsensus.validateBlock(proposedBlock)) {
            throw new RuntimeException("PoW validation failed");
        }
        
        System.out.println("Phase 1 Complete: PoW block mined successfully");
        return proposedBlock;
    }
    
    @Override
    public void finalizeBlock(HybridBlock block) {
        // Phase 2: VDF Sequential Work
        System.out.println("Phase 2: Starting VDF computation...");
        poswConsensus.finalizeBlock(block);
        
        // Validate VDF proof
        if (!poswConsensus.validateBlock(block)) {
            throw new RuntimeException("VDF validation failed");
        }
        
        System.out.println("Phase 2 Complete: VDF proof generated and verified");
    }
    
    @Override
    public boolean validateBlock(HybridBlock block) {
        // Both PoW and VDF proofs must be valid
        boolean powValid = powConsensus.validateBlock(block);
        boolean vdfValid = poswConsensus.validateBlock(block);
        
        System.out.println("Block validation: PoW=" + powValid + ", VDF=" + vdfValid);
        
        return powValid && vdfValid;
    }
    
    @Override
    public boolean isReadyForConsensus() {
        return manager.isNetworkReady() && 
               powConsensus.isReadyForConsensus() && 
               poswConsensus.isReadyForConsensus();
    }
    
    // Public method for external difficulty adjustment
    public void adjustDifficulty(List<HybridBlock> recentBlocks) {
        powConsensus.adjustDifficulty(recentBlocks);
    }
}
