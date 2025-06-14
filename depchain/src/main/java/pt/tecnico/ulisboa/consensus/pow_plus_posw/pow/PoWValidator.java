package pt.tecnico.ulisboa.consensus.pow_plus_posw.pow;

import java.math.BigInteger;
import java.security.MessageDigest;

import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;

public class PoWValidator {
    
    public boolean validatePoWProof(HybridBlock block, BigInteger difficulty) {
        try {
            // Recalculate block hash
            String calculatedHash = calculateBlockHash(block);
            
            // Check if calculated hash matches stored hash
            if (!calculatedHash.equals(block.getHash())) {
                return false;
            }
            
            // Check if hash meets difficulty requirement
            BigInteger hashValue = new BigInteger(calculatedHash, 16);
            BigInteger target = calculateTarget(difficulty);
            
            return hashValue.compareTo(target) < 0;
            
        } catch (Exception e) {
            System.err.println("Error validating PoW proof: " + e.getMessage());
            return false;
        }
    }
    
    private String calculateBlockHash(HybridBlock block) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = block.getPrevHash() + 
                          block.getTimestamp() + 
                          block.getNonce() + 
                          block.getTransactionsHash();
            
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }
    
    private BigInteger calculateTarget(BigInteger difficulty) {
        // Convert difficulty to target value
        // Target = MAX_TARGET / difficulty
        BigInteger maxTarget = new BigInteger("00000000FFFF0000000000000000000000000000000000000000000000000000", 16);
        return maxTarget.divide(difficulty);
    }
}
