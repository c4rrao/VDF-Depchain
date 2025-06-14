package pt.tecnico.ulisboa.consensus.pow_plus_posw.hybrid;

import java.util.List;

import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.consensus.pow_plus_posw.posw.SequentialProof;
import pt.tecnico.ulisboa.protocol.ClientReq;

public class BlockProposal extends HybridBlock {
    private boolean powCompleted = false;
    private boolean vdfCompleted = false;
    private long powCompletionTime;
    private long vdfCompletionTime;
    
    public BlockProposal(List<ClientReq> transactions, String previousHash, int difficulty, int height) {
        super(previousHash, height, transactions, difficulty);
    }
    
    public void markPoWCompleted() {
        this.powCompleted = true;
        this.powCompletionTime = System.currentTimeMillis();
    }
    
    public void markVDFCompleted(SequentialProof proof) {
        this.vdfCompleted = true;
        this.vdfCompletionTime = System.currentTimeMillis();
        super.setVDFProof(proof);
    }
    
    public boolean isFullyValidated() {
        return powCompleted && vdfCompleted;
    }
    
    public long getTotalProcessingTime() {
        if (!isFullyValidated()) {
            return -1; // Not completed yet
        }
        return vdfCompletionTime - getTimestamp();
    }
    
    public long getPoWTime() {
        if (!powCompleted) {
            return -1;
        }
        return powCompletionTime - getTimestamp();
    }
    
    public long getVDFTime() {
        if (!vdfCompleted || !powCompleted) {
            return -1;
        }
        return vdfCompletionTime - powCompletionTime;
    }
    
    @Override
    public String toString() {
        return "BlockProposal{" +
                "hash='" + getHash() + '\'' +
                ", height=" + getHeight() +
                ", powCompleted=" + powCompleted +
                ", vdfCompleted=" + vdfCompleted +
                ", totalTime=" + getTotalProcessingTime() + "ms" +
                '}';
    }
}
