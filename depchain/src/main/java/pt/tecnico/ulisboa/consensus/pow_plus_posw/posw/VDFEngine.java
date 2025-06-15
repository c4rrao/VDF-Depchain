package pt.tecnico.ulisboa.consensus.pow_plus_posw.posw;

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
    
    private BigInteger hashToGroup(byte[] input) {
        byte[] hash = hasher.digest(input);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.mod(params.getModulus());
    }

    private BigInteger sequentialSquaring(BigInteger x, long T) {
        // Compute x^(2^T) mod N sequentially
        BigInteger result = x;
        
        for (long i = 0; i < T; i++) {
            result = result.multiply(result).mod(params.getModulus());
            
            // Progress indicator for long computations
            if (i % 1000000 == 0) {
                System.out.println("VDF Progress: " + (i * 100 / T) + "%");
            }
        }
        
        return result;
    }

    public SequentialProof computeVDF(byte[] input) {
        BigInteger x = hashToGroup(input);
        
        // Compute y = x^(2^T) and proof simultaneously
        VDFResult result = computeVDFWithProof(x, params.getTimeParameter());
        
        return new SequentialProof(result.y, result.proofElements, params.getTimeParameter());
    }

    private VDFResult computeVDFWithProof(BigInteger x, long T) {
        // Step 1: Compute y = x^(2^T) via sequential squaring
        BigInteger y = sequentialSquaring(x, T);
        
        // Step 2: Generate Pietrzak proof using Fiat-Shamir heuristic
        List<BigInteger> proofElements = new ArrayList<>();
        
        // Initialize for iterative proof generation
        BigInteger xi = x;
        BigInteger yi = y;
        long Ti = T;
        
        int t = (int)(Math.log(T) / Math.log(2)); // log₂(T)
        
        // Generate proof elements μᵢ and update (xᵢ, yᵢ) iteratively
        for (int i = 1; i <= t; i++) {
            if (Ti <= 1) break;
            
            long halfTi = Ti / 2;
            
            // Compute μᵢ = xᵢ^(2^(T/2^i))
            BigInteger mu_i = sequentialSquaring(xi, halfTi);
            proofElements.add(mu_i);
            
            // Generate challenge rᵢ = hash((xᵢ, T/2^(i-1), yᵢ), μᵢ)
            String challengeInput = xi.toString() + ":" + Ti + ":" + yi.toString() + ":" + mu_i.toString();
            BigInteger ri = new BigInteger(1, hasher.digest(challengeInput.getBytes()))
                            .mod(BigInteger.valueOf(2).pow(128));
            
            // Update for next iteration according to equations (9)
            BigInteger xi_plus_1 = xi.modPow(ri, params.getModulus()).multiply(mu_i).mod(params.getModulus());
            BigInteger yi_plus_1 = mu_i.modPow(ri, params.getModulus()).multiply(yi).mod(params.getModulus());
            
            // Update for next iteration
            xi = xi_plus_1;
            yi = yi_plus_1;
            Ti = halfTi;
        }
        
        return new VDFResult(y, proofElements);
    }

    private static class VDFResult {
        final BigInteger y;
        final List<BigInteger> proofElements;
        
        VDFResult(BigInteger y, List<BigInteger> proofElements) {
            this.y = y;
            this.proofElements = proofElements;
        }
    }


}
