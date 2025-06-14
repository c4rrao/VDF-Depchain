package pt.tecnico.ulisboa.consensus.pow_plus_posw.hybrid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsensusManager {
    private final AtomicBoolean networkReady = new AtomicBoolean(true);
    private final Map<String, Long> nodeLastSeen = new ConcurrentHashMap<>();
    private volatile String lastBlockHash = "0"; // Genesis block
    private volatile int currentBlockHeight = 0;
    
    public boolean isNetworkReady() {
        return networkReady.get();
    }
    
    public void setNetworkReady(boolean ready) {
        networkReady.set(ready);
    }
    
    public String getLastBlockHash() {
        return lastBlockHash;
    }
    
    public void updateLastBlock(String blockHash, int height) {
        this.lastBlockHash = blockHash;
        this.currentBlockHeight = height;
    }
    
    public int getCurrentBlockHeight() {
        return currentBlockHeight;
    }
    
    public void updateNodeStatus(String nodeId) {
        nodeLastSeen.put(nodeId, System.currentTimeMillis());
    }
    
    public boolean isNodeActive(String nodeId, long timeoutMs) {
        Long lastSeen = nodeLastSeen.get(nodeId);
        if (lastSeen == null) {
            return false;
        }
        return System.currentTimeMillis() - lastSeen < timeoutMs;
    }
    
    public int getActiveNodeCount(long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        return (int) nodeLastSeen.values().stream()
                .filter(lastSeen -> currentTime - lastSeen < timeoutMs)
                .count();
    }
}
