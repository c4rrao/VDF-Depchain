package pt.tecnico.ulisboa.consensus.pow_plus_posw.posw;

import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.Block;
import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.consensus.ConsensusInterface;
import pt.tecnico.ulisboa.protocol.ClientReq;

public class PoSWConsensus implements ConsensusInterface {
    private final VDFEngine vdfEngine;
    private final ProofVerifier verifier;
    private final VDFParameters vdfParams;
    
    public PoSWConsensus() {
        this.vdfParams = new VDFParameters();
        this.vdfEngine = new VDFEngine(vdfParams);
        this.verifier = new ProofVerifier(vdfParams);
    }
    
    @Override
    public Block mineBlock(Block previousBlock, List<ClientReq> transactions) {
        throw new UnsupportedOperationException("PoSWConsensus does not support mining directly. Use finalizeBlock instead.");
    }

    @Override
    public void finalizeBlock(Block _block) {
        if (_block == null || !(_block instanceof HybridBlock)) {
            throw new IllegalArgumentException("Previous block must be a valid HybridBlock");
        }

        HybridBlock block = (HybridBlock) _block;

        // Generate VDF input from block hash
        byte[] vdfInput = generateVDFInput(block);
        
        // Compute VDF proof (this takes time T)
        long startTime = System.nanoTime();  // Start timer
        SequentialProof proof = vdfEngine.computeVDF(vdfInput);
        long endTime = System.nanoTime();
        
        double elapsedSeconds = (endTime - startTime) / 1e6;
        System.out.printf("[PRECISE] VDF Solving Time: %.3f ms (T=%d)%n", 
                            elapsedSeconds, vdfParams.getTimeParameter());
        
        // Add VDF proof to the block
        block.setVDFProof(proof);
        block.setFinalized(true);
    }
    
    @Override
    public boolean validateBlock(Block block) {
        if (block == null || !(block instanceof HybridBlock)) {
            throw new IllegalArgumentException("Block must be a valid HybridBlock");
        }

        HybridBlock powBlock = (HybridBlock) block;
        
        // Verify VDF proof
        byte[] vdfInput = generateVDFInput(powBlock);
        return verifier.verifyVDFProof(vdfInput, powBlock.getVDFProof());
    }
    
    private byte[] generateVDFInput(HybridBlock block) {
        // Use block hash as VDF input
        String blockData = block.getPOWHash();
        return blockData.getBytes();
    }
    
    public VDFParameters getVDFParameters() {
        return vdfParams;
    }
}
