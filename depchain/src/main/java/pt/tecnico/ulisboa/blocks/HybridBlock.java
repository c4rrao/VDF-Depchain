package pt.tecnico.ulisboa.blocks;

import pt.tecnico.ulisboa.protocol.ClientReq;
import pt.tecnico.ulisboa.utils.CryptoUtils;
import pt.tecnico.ulisboa.pow_plus_posw.posw.SequentialProof;

import java.util.List;

public class HybridBlock extends Block {
    // PoW fields
    private long nonce;
    private int difficulty;
    
    // PoSW fields
    private SequentialProof vdfProof;
    private long timeParameter;
    private boolean finalized;
    
    public HybridBlock(String prevHash, int height, List<ClientReq> txs, int difficulty) {
        super(prevHash, height, txs);
        this.difficulty = difficulty;
        this.nonce = 0;
        this.timeParameter = 0;
        this.finalized = false;
    }
    
    public HybridBlock(String prevHash, int height, List<ClientReq> txs, int difficulty, long timeParameter) {
        super(prevHash, height, txs);
        this.difficulty = difficulty;
        this.nonce = 0;
        this.timeParameter = timeParameter;
        this.finalized = false;
    }
    
    @Override
    public String computeBlockHash() {
        StringBuilder blockData = new StringBuilder();
        blockData.append(super.computeBlockHash());
        blockData.append(nonce);
        blockData.append(difficulty);
        blockData.append(timeParameter);
        
        if (vdfProof != null) {
            blockData.append(vdfProof.toString());
        }
        
        return CryptoUtils.hashSHA256(blockData.toString().getBytes());
    }
    
    // PoW-specific methods
    public void incrementNonce() {
        nonce++;
    }
    
    public long getNonce() {
        return nonce;
    }
    
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    public boolean meetsDifficulty() {
        return getHash().startsWith("0".repeat(difficulty));
    }
    
    // PoSW-specific methods
    public SequentialProof getVDFProof() {
        return vdfProof;
    }
    
    public void setVDFProof(SequentialProof vdfProof) {
        this.vdfProof = vdfProof;
        // Recompute hash after setting VDF proof
        setHash(computeBlockHash());
    }
    
    public long getTimeParameter() {
        return timeParameter;
    }
    
    public void setTimeParameter(long timeParameter) {
        this.timeParameter = timeParameter;
    }
    
    public boolean hasVdfProof() {
        return vdfProof != null;
    }
    
    public boolean isFinalized() {
        return finalized;
    }
    
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }
    
    // Combined validation
    public boolean isValidHybrid() {
        // Check PoW validity
        if (difficulty > 0 && !meetsDifficulty()) {
            return false;
        }
        
        // Check PoSW validity if finalized
        if (finalized && (timeParameter > 0 && vdfProof == null)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public void printBlock() {
        super.printBlock();
        System.out.println("Nonce: " + nonce);
        System.out.println("Difficulty: " + difficulty);
        System.out.println("Time Parameter: " + timeParameter);
        System.out.println("VDF Proof: " + (vdfProof != null ? vdfProof.toString() : "null"));
        System.out.println("Finalized: " + finalized);
    }
}
