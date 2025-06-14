package pt.tecnico.ulisboa.blockchain.blocks;

import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;

import pt.tecnico.ulisboa.Config;
import pt.tecnico.ulisboa.protocol.ClientReq;
import pt.tecnico.ulisboa.utils.CryptoUtils;
import pt.tecnico.ulisboa.utils.types.Consensable;
import pt.tecnico.ulisboa.utils.types.Logger;

public class Block implements Consensable, Serializable {

    protected static final long serialVersionUID = 1L;
    protected final int maxTxPerBlock = Config.TX_PER_BLOCK;

    protected Integer height;
    protected String prevHash;
    protected String blockHash;
    protected List<ClientReq> transactions;
    protected long timestamp;

    // constructor to load a already existing block
    public Block(String prevHash, Integer height, String blockHash, List<ClientReq> transactions, long timestamp) {
        this.height = height;
        this.prevHash = prevHash;
        this.blockHash = blockHash;
        this.transactions = transactions;
        this.timestamp = timestamp;
    }

    // constructor to load a already existing block
    public Block(String prevHash, Integer height, String blockHash, List<ClientReq> transactions) {
        this(prevHash, height, blockHash, transactions, System.currentTimeMillis());
    }


    // constructor for genesis block and for server to create an empty block.
    public Block() {
        this.height = 0;
        this.prevHash = null;
        this.transactions = new ArrayList<>();
        this.blockHash = computeBlockHash();
        this.timestamp = System.currentTimeMillis();
    }

    // constructor for genesis block and for server to create an empty block.
    public Block(String prevHash, int height, List<ClientReq> txs) {
        this(prevHash, height, null, txs, System.currentTimeMillis());
    }

    // constructor for genesis block and for server to create an empty block.
    public Block(String prevHash, int height, List<ClientReq> txs, long timestamp) {
        this.height = height;
        this.prevHash = prevHash;
        this.transactions = new ArrayList<>(txs);
        this.blockHash = computeBlockHash();
        this.timestamp = timestamp;
    }

    public String computeBlockHash() {
        StringBuilder blockData = new StringBuilder();
        blockData.append(prevHash != null ? prevHash : "");
        blockData.append(getTransactionsHash());
        return CryptoUtils.hashSHA256(blockData.toString().getBytes());
    }

    public String getTransactionsHash() {
        StringBuilder txHash = new StringBuilder();
        for (ClientReq tx : transactions) {
            txHash.append(tx.toString());
        }
        return CryptoUtils.hashSHA256(txHash.toString().getBytes());
    }

    public void appendTransaction(ClientReq transaction) {
        if (transactions.size() >= maxTxPerBlock) {
            throw new IllegalStateException("Block is full");
        }
        transactions.add(transaction);
    }

    public boolean isFull() {
        return transactions.size() >= maxTxPerBlock;
    }

    public boolean isValid(Map<Integer, PublicKey> publicKeys) {
        if (!this.computeBlockHash().equals(this.getHash())) {
            Logger.LOG("Invalid this hash: " + this.getHash() + ", expected: " + this.computeBlockHash());
            return false;
        }

        // Check if each transaction is valid
        for (ClientReq tx : this.getTransactions()) {
            PublicKey puKey = publicKeys.get(tx.getSenderId());
            if (!tx.isValid()) {
                Logger.LOG("Invalid transaction: " + tx);
            } else if (puKey == null) {
                Logger.LOG("Public key not found for transaction: " + tx);
            } else if (!tx.verifySignature(puKey)) {
                Logger.LOG("Incorrect transaction signature: " + tx);
            } else {
                continue;
            }
            return false;
        }

        return true;
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getHash() {
        return blockHash;
    }

    public void setHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            throw new IllegalArgumentException("Hash cannot be null or empty");
        }
        this.blockHash = hash;
    }

    public Integer getHeight() {
        return height;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<ClientReq> getTransactions() {
        return transactions;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void printBlock() {
        System.out.println("┌───────────────────────────────────────────────────────────────┐");
        System.out.println("│ Block ID: " + height + "                                    │");
        System.out.println("│ Previous Hash: " + prevHash + "                          │");
        System.out.println("│ Block Hash: " + blockHash + "                             │");
        System.out.println("│ Transactions:                                             │");
        for (ClientReq tx : transactions) {
            System.out.println("│ " + tx.toString() + " │");
        }
        System.out.println("└───────────────────────────────────────────────────────────────┘");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Block other = (Block) obj;
        return this.blockHash.equals(other.blockHash);
    }

    @Override
    public int hashCode() {
        return blockHash.hashCode();
    }
}
