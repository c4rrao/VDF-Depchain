package pt.tecnico.ulisboa.pow_plus_posw.posw;

import java.util.List;

import pt.tecnico.ulisboa.blocks.HybridBlock;
import pt.tecnico.ulisboa.pow_plus_posw.hybrid.ConsensusInterface;
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
    public void start() {
        // Initialize VDF parameters and engine
        System.out.println("PoSW Consensus started with T = " + vdfParams.getTimeParameter());
    }
    
    @Override
    public HybridBlock proposeBlock(List<ClientReq> transactions, String previousHash) {
        // PoSW doesn't propose blocks, it finalizes them
        throw new UnsupportedOperationException("PoSW consensus doesn't propose blocks");
    }
    
    @Override
    public void finalizeBlock(HybridBlock block) {
        try {
            // Generate VDF input from block hash
            byte[] vdfInput = generateVDFInput(block);
            
            // Compute VDF proof (this takes time T)
            long startTime = System.currentTimeMillis();
            SequentialProof proof = vdfEngine.computeVDF(vdfInput);
            long computationTime = System.currentTimeMillis() - startTime;
            
            System.out.println("VDF computation completed in " + 
                             computationTime + "ms");
            
            // Add VDF proof to the block
            block.setVDFProof(proof);
            block.setFinalized(true);
            
        } catch (Exception e) {
            throw new RuntimeException("VDF computation failed", e);
        }
    }
    
    @Override
    public boolean validateBlock(HybridBlock block) {
        HybridBlock powBlock = (HybridBlock) block;
        
        // Verify VDF proof
        byte[] vdfInput = generateVDFInput(block);
        return verifier.verifyVDFProof(vdfInput, powBlock.getVDFProof());
    }
    
    @Override
    public boolean isReadyForConsensus() {
        return vdfEngine != null && verifier != null;
    }
    
    private byte[] generateVDFInput(HybridBlock block) {
        // Use block hash as VDF input
        String blockData = block.getHash() + block.getTimestamp();
        return blockData.getBytes();
    }
    
    public VDFParameters getVDFParameters() {
        return vdfParams;
    }
}
