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
        long startTime = System.nanoTime();  // Start timer
        boolean result = verifyRecursive(x, y, T, proof, 0);
        long endTime = System.nanoTime();
        
        double elapsedMs = (endTime - startTime) / 1e6;
        System.out.printf("VDF Verification Time: %.3f ms (T=%d, proofSize=%d)%n", 
                        elapsedMs, T, proof.size());
        return result;
    }

    private boolean verifyRecursive(BigInteger x, BigInteger y, long T, 
                                List<BigInteger> proof, int proofIndex) {
        
        System.out.println("\n--- Verification Step " + proofIndex + " (T=" + T + ") ---");       //debugging
        System.out.println("x: " + x.toString(16).substring(0, 16) + "...");
        System.out.println("y: " + y.toString(16).substring(0, 16) + "...");
        
        if (T == 1) {
            // Base case: verify y = x² mod N (equation 12)
            BigInteger expected = x.multiply(x).mod(params.getModulus());

            if (expected.equals(y)) {
                System.out.println("VERIFICATION PASSED");
            }

            return expected.equals(y);
        }
        
        if (proofIndex >= proof.size()) {
            System.err.println("ERROR: Proof too short at step " + proofIndex);  //debugging
            return false;
        }
        
        long halfT = T % 2 == 0 ? T / 2 : (T + 1) / 2; 
        BigInteger mu = proof.get(proofIndex);
        System.out.println("μ: " + mu.toString(16).substring(0, 16) + "...");  //debugging
        
        // Step 1: Check μ ∈ QR⁺ₙ (REQUIRED by algorithm)
        if (!isInSignedQuadraticResidues(mu)) {
            System.err.println("FAIL: μ ∉ QRₙ⁺");     //debugging
            System.err.println("μ: " + mu);
            System.err.println("Jacobi: " + jacobiSymbol(mu, params.getModulus()));
            return false;
        }
        
        // Step 2: Generate challenge according to equation (10)
        String challengeInput = x.toString() + ":" + (T) + ":" + y.toString() + ":" + mu.toString();
        BigInteger r = new BigInteger(1, hasher.digest(challengeInput.getBytes()))
                        .mod(BigInteger.valueOf(2).pow(params.getSecurityParameter()));
        
        System.out.println("r: " + r.toString(16).substring(0, 16) + "..."); //debugging

        // Step 3: Compute new values (equations 10-11)
        BigInteger x_new = x.modPow(r, params.getModulus()).multiply(mu).mod(params.getModulus());
        BigInteger y_new = mu.modPow(r, params.getModulus()).multiply(y).mod(params.getModulus());
        
        // Handle odd T (Section 3.1 of the paper)
        if (T % 2 != 0) {
            System.out.println("Odd T adjustment: squaring y_new");  //debugging
            y_new = y_new.multiply(y_new).mod(params.getModulus());
        }

        return verifyRecursive(x_new, y_new, halfT, proof, proofIndex + 1);
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

}
