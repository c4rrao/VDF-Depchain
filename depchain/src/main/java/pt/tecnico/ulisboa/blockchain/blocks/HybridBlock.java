package pt.tecnico.ulisboa.blockchain.blocks;

import java.util.List;

import pt.tecnico.ulisboa.consensus.pow_plus_posw.posw.SequentialProof;
import pt.tecnico.ulisboa.protocol.ClientReq;
import pt.tecnico.ulisboa.utils.CryptoUtils;

public class HybridBlock extends Block {
    // PoW fields
    private long nonce = 0;
    private Integer difficulty = 0;
    private String POWHash = null;
    
    // PoSW fields
    private SequentialProof vdfProof = null;
    private long timeParameter = 0;

    private boolean finalized = false;
    
    public HybridBlock(String prevHash, int height, List<ClientReq> txs, Integer difficulty) {
        super(prevHash, height, txs);
        this.difficulty = difficulty;
    }

    public HybridBlock() {
        super();
    }
    
    @Override
    public String computeBlockHash() {
        return computeBlockHash(false);
    }

    public String computeBlockHash(boolean onlyPOW) {
        StringBuilder blockData = new StringBuilder();
        blockData.append(super.computeBlockHash());
        blockData.append(nonce);
        blockData.append(difficulty);
        
        if (!onlyPOW && vdfProof != null) {
            blockData.append(timeParameter);
            blockData.append(vdfProof.toString());
        }
        
        return CryptoUtils.hashSHA256(blockData.toString().getBytes());
    }
    
    // PoW-specific methods
    public void incrementNonce() {
        nonce++;
    }
    
    public String getPOWHash() {
        return POWHash;
    }

    public void setPOWHash(String POWHash) {
        this.POWHash = POWHash;
    }

    public long getNonce() {
        return nonce;
    }
    
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
    
    public Integer getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }
    
    public boolean meetsDifficulty() {
        String hash = computeBlockHash(true);
        String requiredZeros = "0".repeat(difficulty); // difficulty as int (number of zeros)
        return hash.startsWith(requiredZeros);
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
        if (!meetsDifficulty()) {
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
