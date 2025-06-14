package pt.tecnico.ulisboa.consensus.pow_plus_posw.posw;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class ProofVerifier {
    private final VDFParameters params;
    private final MessageDigest hasher;
    
    public ProofVerifier(VDFParameters params) {
        this.params = params;
        try {
            this.hasher = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize hasher", e);
        }
    }
    
    public boolean verifyVDFProof(byte[] input, SequentialProof proof) {
        try {
            // Hash input to group element
            BigInteger x = hashToGroup(input);
            
            // Verify Pietrzak proof
            return verifyPietrzakProof(x, proof.getOutput(), 
                                     proof.getProofElements(), proof.getTimeParameter());
            
        } catch (Exception e) {
            System.err.println("VDF proof verification failed: " + e.getMessage());
            return false;
        }
    }
    
    private BigInteger hashToGroup(byte[] input) {
        byte[] hash = hasher.digest(input);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.mod(params.getModulus());
    }
    
    private boolean verifyPietrzakProof(BigInteger x, BigInteger y, 
                                      List<BigInteger> proof, long T) {
        if (proof.isEmpty()) {
            // Direct verification for small T
            BigInteger expected = x;
            for (long i = 0; i < T; i++) {
                expected = expected.multiply(expected).mod(params.getModulus());
            }
            return expected.equals(y);
        }
        
        // Recursive verification
        return verifyRecursive(x, y, T, proof, 0);
    }
    
    private boolean verifyRecursive(BigInteger x, BigInteger y, long T, 
                                  List<BigInteger> proof, int proofIndex) {
        if (T <= 1) {
            // Base case: direct verification
            BigInteger expected = x.multiply(x).mod(params.getModulus());
            return expected.equals(y);
        }
        
        if (proofIndex >= proof.size()) {
            return false; // Not enough proof elements
        }
        
        long halfT = T / 2;
        BigInteger mu = proof.get(proofIndex);
        
        // Generate challenge
        byte[] challengeInput = (x.toString() + y.toString() + mu.toString()).getBytes();
        BigInteger r = new BigInteger(1, hasher.digest(challengeInput))
                          .mod(BigInteger.valueOf(2).pow(128));
        
        // Compute new values
        BigInteger x_new = x.modPow(r, params.getModulus()).multiply(mu).mod(params.getModulus());
        BigInteger y_new = mu.modPow(r, params.getModulus()).multiply(y).mod(params.getModulus());
        
        // Verify consistency: μ^r * y should equal y_new
        BigInteger check = mu.modPow(r, params.getModulus()).multiply(y).mod(params.getModulus());
        if (!check.equals(y_new)) {
            return false;
        }
        
        // Recursive verification
        return verifyRecursive(x_new, y_new, halfT, proof, proofIndex + 1);
    }
}
