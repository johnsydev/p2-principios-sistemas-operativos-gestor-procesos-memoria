package minipcsimulator.model;

public class AlgorithmFactory {

    public static SchedulingAlgorithm create(String nombre, int quantum) {
        switch (nombre) {
            case "SPN":                   return new SPNAlgorithm();
            case "SRT":                   return new SRTAlgorithm();
            case "Round Robin":
            case "Selfish Round Robin":   return new RoundRobinAlgorithm(quantum); // selfish: pendiente
            case "HRRN":                  return new HRRNAlgorithm();
            case "Lottery":               return new LotteryAlgorithm(quantum);
            case "Por Prioridad":         return new PriorityAlgorithm(false);
            case "Guaranteed Scheduling": return null;
            default:                      return new FCFSAlgorithm(); // FCFS y MLFQ (pendiente)
        }
    }
}