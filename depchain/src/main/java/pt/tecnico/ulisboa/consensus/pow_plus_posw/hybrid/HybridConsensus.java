package pt.tecnico.ulisboa.consensus.pow_plus_posw.hybrid;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.Block;
import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.consensus.ConsensusInterface;
import pt.tecnico.ulisboa.consensus.pow_plus_posw.posw.PoSWConsensus;
import pt.tecnico.ulisboa.consensus.pow_plus_posw.pow.PoWConsensus;
import pt.tecnico.ulisboa.protocol.ClientReq;

public class HybridConsensus implements ConsensusInterface {
    private final PoWConsensus powConsensus;
    private final PoSWConsensus poswConsensus;
    
    public HybridConsensus() {
        this.powConsensus = new PoWConsensus();
        this.poswConsensus = new PoSWConsensus();
    }
    
    public static void main (String[] args) {
        // This method is not used in the context of the consensus implementation
        // but can be used for testing or demonstration purposes.
        
        HybridConsensus consensus = new HybridConsensus();

        HybridBlock previousBlock = new HybridBlock("0000000000000000", 0, new ArrayList<>(), 10);

        while (true) {
            try {
                System.out.println("Mining new block...");

                HybridBlock newBlock = consensus.mineBlock(previousBlock, new ArrayList<>());

                if (!consensus.validateBlock(newBlock)) {
                    System.out.println("BLOCK IS INVALID");
                }
                
                System.out.println("New block mined: height: " + newBlock.getHeight());
                
                previousBlock = newBlock; // Update previous block for next iteration
            } catch (Exception e) {
                System.err.println("Error during mining: " + e.getMessage());
            }

            try {
                Thread.sleep(1000); // Sleep for 1 second before mining the next block
            } catch (InterruptedException e) {
                System.err.println("Mining interrupted: " + e.getMessage());
                break; // Exit loop if interrupted
            }
            break;
        }
    }

    @Override
    public HybridBlock mineBlock(Block previousBlock, List<ClientReq> transactions) {
        if (previousBlock == null || previousBlock instanceof HybridBlock == false) {
            throw new IllegalArgumentException("Previous block must be a valid HybridBlock");
        }

        // Phase 1: PoW Block Proposal
        // System.out.println("Phase 1: Starting PoW mining...");

        long timeSpent = -System.nanoTime();

        HybridBlock proposedBlock = powConsensus.mineBlock(previousBlock, new ArrayList<>());

        timeSpent += System.nanoTime();

        System.out.println("TIME: PoW mining: " + timeSpent + "ns");

        // System.out.println("Phase 1 Complete: PoW block mined successfully");

        // Phase 2: PoSW Finalization
        // System.out.println("Phase 2: Starting PoSW finalization...");

        timeSpent = -System.nanoTime();

        poswConsensus.finalizeBlock(proposedBlock);

        timeSpent += System.nanoTime();

        System.out.println("TIME: PoSW finalizing: " + timeSpent + "ns");

        // System.out.println("Phase 2 Complete: PoSW finalization successful");

        return proposedBlock;
    }

    @Override
    public boolean validateBlock(Block block) {
        // Both PoW and VDF proofs must be valid

        long timeSpent = -System.nanoTime();

        boolean powValid = powConsensus.validateBlock(block);

        timeSpent += System.nanoTime();
        System.out.println("TIME: PoW validation: " + timeSpent + "ns");

        if (!powValid) {
            System.out.println("PoW validation failed for block: " + block.getHash());
            return false;
        }

        timeSpent = -System.nanoTime();

        boolean vdfValid = poswConsensus.validateBlock(block);

        timeSpent += System.nanoTime();
        System.out.println("TIME: PoSW validation: " + timeSpent + "ns");

        // printt vdf params
        System.out.println("VDF Parameters: " + poswConsensus.getVDFParameters());
        
        if (!vdfValid) {
            System.out.println("PoSW validation failed for block: " + block.getHash());
            return false;
        }
        
        // System.out.println("Block validation successful for block: " + block.getHash());

        return true;
    }

    @Override
    public void finalizeBlock(Block block) {
        throw new UnsupportedOperationException("Use mineBlock instead to handle both PoW and PoSW phases.");
    }
    
    // Public method for external difficulty adjustment
    public void adjustDifficulty(List<HybridBlock> recentBlocks) {
        powConsensus.adjustDifficulty(recentBlocks);
    }
}
