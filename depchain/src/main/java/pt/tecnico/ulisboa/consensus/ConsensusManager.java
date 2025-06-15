package pt.tecnico.ulisboa.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.tecnico.ulisboa.blockchain.blocks.Block;
import pt.tecnico.ulisboa.blockchain.blocks.HybridBlock;
import pt.tecnico.ulisboa.client.Client;
import pt.tecnico.ulisboa.consensus.pow_plus_posw.hybrid.HybridConsensus;
import pt.tecnico.ulisboa.protocol.ClientReq;
import pt.tecnico.ulisboa.server.Server;

public class ConsensusManager {
    private final AtomicBoolean networkReady = new AtomicBoolean(true);
    private final Map<String, Long> nodeLastSeen = new ConcurrentHashMap<>();
    private List<Block> recentBlocks;
    private List<Block> proposedBlocks = new ArrayList<>();
    private ConsensusInterface consensus = new HybridConsensus();
    private List<ClientReq> receivedTransactions;

    public ConsensusManager(List<Block> recentBlocks, List<ClientReq> rcvTxs) {
        this.receivedTransactions = rcvTxs;

        if (recentBlocks == null) {
            this.recentBlocks = new ArrayList<>();
            this.recentBlocks.add(new HybridBlock()); 
        } else {
            this.recentBlocks = recentBlocks;
        }
    }

    public void startConsensus() {
        while (networkReady.get()) {
            // // Shared state for thread competition
            // boolean raceFinished = false;
            // Block finalizedBlock = null;
            
            // // Create mining thread
            // Thread rcvProposedBlocksThread;
            // Thread miningThread = new Thread(() -> {
            //     try {
            //         // Wait for transactions
            //         while (receivedTransactions.isEmpty() && !Thread.currentThread().isInterrupted()) {
            //             Thread.sleep(100);
            //         }
                    
            //         if (Thread.currentThread().isInterrupted()) return;
                    
            //         // Mine new block
            //         Block previousBlock = recentBlocks.get(recentBlocks.size() - 1);
            //         Block newBlock = consensus.mineBlock(previousBlock, receivedTransactions);
                    
            //         // Check if we won the race
            //         synchronized (this) {
            //             if (!raceFinished) {
            //                 raceFinished = true;
            //                 finalizedBlock = newBlock;
            //                 System.out.println("Mining thread WON! Block mined: " + newBlock.getHash());
            //                 rcvProposedBlocksThread.interrupt();
            //             }
            //         }
                    
            //     } catch (InterruptedException e) {
            //         System.out.println("Mining thread interrupted by received block");
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //     }
            // });
            
            // // Create block receiving thread
            // rcvProposedBlocksThread = new Thread(() -> {
            //     try {
            //         while (!Thread.currentThread().isInterrupted()) {
            //             // Check for proposed blocks from network
            //             if (!proposedBlocks.isEmpty()) {
            //                 Block proposedBlock = proposedBlocks.remove(0);
                            
            //                 if (consensus.validateBlock(proposedBlock)) {
            //                     // Check if we won the race
            //                     synchronized (this) {
            //                         if (!raceFinished) {
            //                             raceFinished = true;
            //                             finalizedBlock = proposedBlock;
            //                             System.out.println("Receive thread WON! Block received: " + proposedBlock.getHash());
            //                             miningThread.interrupt();
            //                             return;
            //                         }
            //                     }
            //                 } else {
            //                     System.out.println("Invalid proposed block: " + proposedBlock.getHash());
            //                 }
            //             }
            //             Thread.sleep(100);
            //         }
                    
            //     } catch (InterruptedException e) {
            //         System.out.println("Receive thread interrupted by mining completion");
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //     }
            // });
            
            // // Start both threads
            // miningThread.start();
            // rcvProposedBlocksThread.start();
            
            // // Wait for one thread to win
            // try {
            //     miningThread.join();
            //     rcvProposedBlocksThread.join();
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
            
            // // Process the winning block
            // if (finalizedBlock != null) {
            //     // Update blockchain state
            //     recentBlocks.add(finalizedBlock);
            //     proposedBlocks.add(finalizedBlock);
                
            //     // Start broadcasting thread
            //     Thread broadcastThread = new Thread(() -> {
            //         try {
            //             System.out.println("Broadcasting block: " + finalizedBlock.getHash());
                        
            //         } catch (Exception e) {
            //             e.printStackTrace();
            //         }
            //     });
                
            //     broadcastThread.setName("Broadcast Thread");
            //     broadcastThread.start();
            // }
        }
    }

    public boolean isNetworkReady() {
        return networkReady.get();
    }
    
    public void setNetworkReady(boolean ready) {
        networkReady.set(ready);
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
