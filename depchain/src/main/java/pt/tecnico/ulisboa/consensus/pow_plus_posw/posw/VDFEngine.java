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
        System.out.println("VDF Progress: " + 0 + "%");

        System.out.println("VDF Engine Final Output (y = x^(2^T)):");
        System.out.println("x: " + x.toString(16));
        System.out.println("y: " + y.toString(16));
        System.out.println("T: " + T);
        
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
            
            long halfTi = Ti % 2 == 0 ? Ti / 2 : (Ti + 1) / 2; 
            
            // Compute μᵢ = xᵢ^(2^(T/2^i))
            BigInteger mu_i = sequentialSquaring(xi, halfTi);
            System.out.println("VDF Progress: " + (i * 100 / t) + "%");

            if (!isInSignedQuadraticResidues(mu_i)) {  //debugging
                throw new IllegalStateException(
                    "Generated invalid μ at step " + i + 
                    ": Jacobi=" + jacobiSymbol(mu_i, params.getModulus()) +
                    ", μ=" + mu_i.toString(16).substring(0, 16) + "..."
                );
            }

            proofElements.add(mu_i);
            
            // Generate challenge rᵢ = hash((xᵢ, T/2^(i-1), yᵢ), μᵢ)
            String challengeInput = xi.toString() + ":" + Ti + ":" + yi.toString() + ":" + mu_i.toString();
            BigInteger ri = new BigInteger(1, hasher.digest(challengeInput.getBytes()))
                            .mod(BigInteger.valueOf(2).pow(params.getSecurityParameter()));
            
            // Update for next iteration according to equations (9)
            BigInteger xi_plus_1 = xi.modPow(ri, params.getModulus()).multiply(mu_i).mod(params.getModulus());
            BigInteger yi_plus_1 = mu_i.modPow(ri, params.getModulus()).multiply(yi).mod(params.getModulus());
            
            // Handle odd T (Section 3.1 of the paper)
            if (Ti % 2 != 0) {
                yi_plus_1 = yi_plus_1.multiply(yi_plus_1).mod(params.getModulus());
            }

            // Update for next iteration
            xi = xi_plus_1;
            yi = yi_plus_1;
            Ti = halfTi;
        }
        
        return new VDFResult(y, proofElements);
    }

    private boolean isInSignedQuadraticResidues(BigInteger x) {
        if (x.signum() < 0) return false;
        return jacobiSymbol(x, params.getModulus()) == 1;
    }

    private int jacobiSymbol(BigInteger a, BigInteger n) {
        if (n.compareTo(BigInteger.ONE) <= 0 || n.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("n must be odd and > 1");
        }
        
        a = a.remainder(n);
        int result = 1;
        
        while (!a.equals(BigInteger.ZERO)) {
            while (a.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                a = a.divide(BigInteger.valueOf(2));
                BigInteger nMod8 = n.remainder(BigInteger.valueOf(8));
                if (nMod8.equals(BigInteger.valueOf(3)) || nMod8.equals(BigInteger.valueOf(5))) {
                    result = -result;
                }
            }
            
            // Swap a and n
            BigInteger temp = a;
            a = n;
            n = temp;
            
            if (a.remainder(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3)) && 
                n.remainder(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                result = -result;
            }
            
            a = a.remainder(n);
        }
        
        if (n.equals(BigInteger.ONE)) {
            return result;
        } else {
            return 0;
        }
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
