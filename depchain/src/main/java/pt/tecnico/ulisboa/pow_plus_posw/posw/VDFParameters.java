package pt.tecnico.ulisboa.pow_plus_posw.posw;

import java.math.BigInteger;

public class VDFParameters {
    private final BigInteger modulus;
    private final long T; // Time parameter (number of sequential steps)
    private final int securityParameter;
    
    public VDFParameters() {
        // Default parameters for 128-bit security
        this.T = 1000000; // ~10 seconds on modern hardware
        this.securityParameter = 128;
        this.modulus = generateSafeModulus();
    }
    
    public VDFParameters(long T, int securityParameter) {
        this.T = T;
        this.securityParameter = securityParameter;
        this.modulus = generateSafeModulus();
    }
    
    private BigInteger generateSafeModulus() {
        // For production, use a trusted setup or well-known safe modulus
        // This is a simplified example - in practice you'd use RSA-2048 or similar
        
        // Example: Use a product of two large primes (simplified)
        // In production: use RSA challenges or trusted setup
        String modulusHex = "C7970CEEDCC3B0754490201A7AA613CD73911081C64B42A1B27EAAE7"
                          + "0CDF6D7C4A6E5F5D7C6A9B9B8A8A7D7D6C6C5B5B4A4A3D3D2C2C1B1B"
                          + "0A0A9D9D8C8C7B7B6A6A5959484837372626151504043333222211110000";
        
        return new BigInteger(modulusHex, 16);
    }
    
    public BigInteger getModulus() {
        return modulus;
    }
    
    public long getTimeParameter() {
        return T;
    }
    
    public int getSecurityParameter() {
        return securityParameter;
    }
    
    @Override
    public String toString() {
        return "VDFParameters{" +
                "T=" + T +
                ", securityParameter=" + securityParameter +
                ", modulus=" + modulus +
                '}';
    }
}
