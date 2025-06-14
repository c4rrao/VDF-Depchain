package pt.tecnico.ulisboa.consensus.bft;

public class AbortedSignal extends Exception {
    public AbortedSignal() {
        super("Aborted signal");
    }

    public AbortedSignal(String message) {
        super("Aborted signal: " + message);
    }
}
