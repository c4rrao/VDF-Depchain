package pt.tecnico.ulisboa.pow_plus_posw.posw;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class VDFEngine {
    private final VDFParameters params;
    private final MessageDigest hasher;
    
    public VDFEngine(VDFParameters params) {
        this.params = params;
        try {
            this.hasher = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize hasher", e);
        }
    }
    
    public SequentialProof computeVDF(byte[] input) {
        // Pietrzak VDF Construction
        BigInteger x = hashToGroup(input);
        BigInteger y = sequentialSquaring(x, params.getTimeParameter());
        
        // Generate Pietrzak proof
        List<BigInteger> proof = generatePietrzakProof(x, y, params.getTimeParameter());
        
        return new SequentialProof(y, proof, params.getTimeParameter());
    }
    
    private BigInteger hashToGroup(byte[] input) {
        // Hash input to group element
        byte[] hash = hasher.digest(input);
        BigInteger hashInt = new BigInteger(1, hash);
        
        // Ensure the result is in the valid range modulo N
        return hashInt.mod(params.getModulus());
    }
    
    private BigInteger sequentialSquaring(BigInteger x, long T) {
        // Compute x^(2^T) mod N sequentially
        BigInteger result = x;
        
        for (long i = 0; i < T; i++) {
            result = result.multiply(result).mod(params.getModulus());
            
            // Progress indicator for long computations
            if (i % 10000 == 0) {
                System.out.println("VDF Progress: " + (i * 100 / T) + "%");
            }
        }
        
        return result;
    }
    
    private List<BigInteger> generatePietrzakProof(BigInteger x, BigInteger y, long T) {
        List<BigInteger> proof = new ArrayList<>();
        
        // Recursive halving approach for Pietrzak proof
        generateProofRecursive(x, y, T, proof);
        
        return proof;
    }
    
    private void generateProofRecursive(BigInteger x, BigInteger y, long T, 
                                      List<BigInteger> proof) {
        if (T <= 1) {
            return; // Base case
        }
        
        long halfT = T / 2;
        
        // Compute middle value μ = x^(2^halfT) mod N
        BigInteger mu = sequentialSquaring(x, halfT);
        proof.add(mu);
        
        // Generate challenge
        byte[] challengeInput = (x.toString() + y.toString() + mu.toString()).getBytes();
        BigInteger r = new BigInteger(1, hasher.digest(challengeInput))
                          .mod(BigInteger.valueOf(2).pow(128)); // 128-bit challenge
        
        // Recursive calls
        BigInteger x_new = x.modPow(r, params.getModulus()).multiply(mu).mod(params.getModulus());
        BigInteger y_new = mu.modPow(r, params.getModulus()).multiply(y).mod(params.getModulus());
        
        generateProofRecursive(x_new, y_new, halfT, proof);
    }
}
