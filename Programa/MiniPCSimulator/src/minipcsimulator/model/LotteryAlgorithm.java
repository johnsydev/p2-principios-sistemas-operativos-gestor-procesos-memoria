package minipcsimulator.model;

import java.util.List;
import java.util.Random;

public class LotteryAlgorithm implements SchedulingAlgorithm {
    private static final int TICKETS_PER_PRIORITY = 10;

    private final Random random = new Random();
    private final int quantum;

    public LotteryAlgorithm(int quantum) {
        this.quantum = (quantum > 0) ? quantum : 1;
    }

    // Tickets = prioridad * 10 
    private int tickets(Process p) {
        return p.getPCB().getPriority() * TICKETS_PER_PRIORITY;
    }

    @Override
    public Process selectNext(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        int totalTickets = 0;
        for (Process p : readyProcesses) {
            totalTickets += tickets(p);
        }

        int winner = random.nextInt(totalTickets); // 0 .. totalTickets-1
        int counter = 0;
        for (Process p : readyProcesses) {
            counter += tickets(p);
            if (winner < counter) {
                return p;
            }
        }
        return null; // no debería llegar aquí
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    @Override
    public int getQuantum() {
        return quantum;
    }
}