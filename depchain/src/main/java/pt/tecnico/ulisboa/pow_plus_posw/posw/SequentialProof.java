package pt.tecnico.ulisboa.pow_plus_posw.posw;

import java.math.BigInteger;
import java.util.List;

public class SequentialProof {
    private final BigInteger output; // VDF output y
    private final List<BigInteger> proofElements; // Pietrzak proof π
    private final long timeParameter; // T value used
    private final long timestamp; // When proof was generated
    
    public SequentialProof(BigInteger output, List<BigInteger> proofElements, long timeParameter) {
        this.output = output;
        this.proofElements = proofElements;
        this.timeParameter = timeParameter;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BigInteger getOutput() {
        return output;
    }
    
    public List<BigInteger> getProofElements() {
        return proofElements;
    }
    
    public long getTimeParameter() {
        return timeParameter;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public int getProofSize() {
        // Calculate proof size in bytes
        int size = 0;
        size += output.toByteArray().length;
        for (BigInteger element : proofElements) {
            size += element.toByteArray().length;
        }
        size += 8; // timestamp
        size += 8; // time parameter
        return size;
    }
    
    @Override
    public String toString() {
        return "SequentialProof{" +
                "output=" + output +
                ", proofSize=" + getProofSize() + " bytes" +
                ", timeParameter=" + timeParameter +
                ", timestamp=" + timestamp +
                '}';
    }
}

