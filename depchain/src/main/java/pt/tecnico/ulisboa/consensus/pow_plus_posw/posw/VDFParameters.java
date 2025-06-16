// VDFParameters.java - Use proper RSA modulus
package pt.tecnico.ulisboa.consensus.pow_plus_posw.posw;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.math.BigInteger;

public class VDFParameters {
    private final BigInteger modulus;
    private final long T;
    private final int securityParameter;
    
    public VDFParameters() {
        this.T = (long) Math.pow(2,20);
        this.securityParameter = 128;
        this.modulus = generateRSAModulus(2048);
    }

    private BigInteger generateRSAModulus(int bitLength) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(bitLength);
            RSAPublicKey publicKey = (RSAPublicKey) keyGen.generateKeyPair().getPublic();
            return publicKey.getModulus();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA modulus", e);
        }
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
