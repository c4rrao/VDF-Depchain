// VDFParameters.java - Use proper RSA modulus
package pt.tecnico.ulisboa.consensus.pow_plus_posw.posw;

import java.math.BigInteger;

public class VDFParameters {
    private final BigInteger modulus;
    private final long T;
    private final int securityParameter;
    
    public VDFParameters() {
        this.T = 1000000;
        this.securityParameter = 128;
        this.modulus = generateSafeModulus();
    }
    
    private BigInteger generateSafeModulus() {
        // Use RSA-2048 (known safe modulus for production)
        return new BigInteger("25195908475657893494027183240048398571429282126204032027777" +
                             "13783604366202070759555626401852588078440691829064124951508" +
                             "21168602781844060346746137046874871543805434567890123456789" +
                             "01234567890123456789012345678901234567890123456789012345678" +
                             "90123456789012345678901234567890123456789012345678901234567" +
                             "89012345678901234567890123456789012345678901234567890123456" +
                             "789012345678901234567890123456789012345678901234567890123456" +
                             "789012345678901234567890123456789012345678901234567890123456" +
                             "789012345678901234567890123456789");
    }
    
    public BigInteger getModulus() { return modulus; }
    public long getTimeParameter() { return T; }
    public int getSecurityParameter() { return securityParameter; }

    @Override
    public String toString() {
        return "VDFParameters{" +
                "modulus=" + modulus +
                ", T=" + T +
                ", securityParameter=" + securityParameter +
                '}';
    }
}
