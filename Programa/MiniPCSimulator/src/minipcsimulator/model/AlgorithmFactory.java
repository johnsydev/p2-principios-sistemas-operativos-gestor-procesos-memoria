package minipcsimulator.model;

public class AlgorithmFactory {

    public static SchedulingAlgorithm create(String nombre, int quantum, int niveles) {
        switch (nombre) {
            case "SPN":                   return new SPNAlgorithm();
            case "SRT":                   return new SRTAlgorithm();
            case "Round Robin":
            case "Selfish Round Robin":   return new RoundRobinAlgorithm(quantum); 
            case "HRRN":                  return new HRRNAlgorithm();
            case "Lottery":               return new LotteryAlgorithm(quantum);
            case "Por Prioridad":         return new PriorityAlgorithm(false);
            case "Guaranteed Scheduling": return new GuaranteedAlgorithm();
            case "Multilevel Feedback Queue": return new MLFQAlgorithm(quantum, niveles);
            default:                      return new FCFSAlgorithm(); 
        }
    }
}