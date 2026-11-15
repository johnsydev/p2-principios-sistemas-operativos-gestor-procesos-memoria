package minipcsimulator.model;

import java.util.ArrayList;
import minipcsimulator.model.Process.ProcessState;
import minipcsimulator.utils.SystemConfig;
import java.util.Iterator;

public class Scheduler {
    private JobList jobList;
    private ProcessList processList;
    private MainMemory memory;
    private Disk disk;
    private SystemClock systemClock;

    private ArrayList<ArrayList<Process>> readyLists = new ArrayList<>();
    
    //Atributos para Selfish RR
    private ArrayList<ArrayList<Process>> newLists = new ArrayList<>();
    private boolean selfish = false;
    private double selfishA = 2;
    private double selfishB = 1;

    public Scheduler(JobList jobList, ProcessList processList, MainMemory memory, Disk disk, SystemClock systemClock, int numCPUs) {
        this.jobList = jobList;
        this.processList = processList;
        this.memory = memory;
        this.disk = disk;
        this.systemClock = systemClock;

        // una lista de listos por CPU
        for (int i = 0; i < numCPUs; i++) {
            readyLists.add(new ArrayList<>());
            newLists.add(new ArrayList<>());
        }

        
    }

    public void addToReadyList(Process process) {
        //process.getPCB().setState(ProcessState.READY);
        readyLists.get(process.getAssignedCpuId() - 1).add(process);
    }

    public ArrayList<Process> getReadyList(int cpuIndex) {
        return readyLists.get(cpuIndex);
    }

    public boolean hasPendingJobs() {
        return jobList.hasPendingJobs();
    }

    public void checkAdmitJob() {
        boolean onlyMemVirtual = hayProcesosSuspendidos(); // antes: false
        while (jobList.hasPendingJobs()) {
            if (processList.getProcessCount() >= SystemConfig.MAX_PROCESSES_IN_MEMORY) {
                System.out.println("No se pueden admitir más procesos en memoria. Se alcanzó el límite máximo de procesos.");
                return;
            }

            Job job = jobList.getNextJob();

            if (job != null) {

                int pcbStartAddress = -1;
                int memoryStartAddress = -1;
                boolean haveSpaceInRAM = true;

                try {
                    pcbStartAddress = Loader.getFreeSpaceInMemoryForPCB(memory);
                } catch (Exception e) {
                    System.out.println("No hay espacio en el Kernel para el PCB. El trabajo espera en disco.");
                    return; // No se admite aún
                }

                try {
                    memoryStartAddress = Loader.getFreeSpaceInMemory(job.getSize(), memory);
                } catch (Exception e) {
                    // intentar agregar a mem. virtual
                    haveSpaceInRAM = false;
                }

                if (haveSpaceInRAM) {
                    if (onlyMemVirtual) {
                        return; // Si ya hay un proceso esperando en memoria virtual, no admitir otros en RAM antes que él (admisión en orden de llegada), FCFS
                    }
                    Process process = new Process(job.getAssignedPID(), memoryStartAddress, pcbStartAddress);
                    process.setAssignedCpuId(job.getAssignedCpuId());
                    process.setFilePathAndName(job.getPathFile(), job.getNameFile());
                    process.getPCB().setPriority(job.getPriority());
                    process.setArrivalTime(job.getArrivalTime());
                    process.setBurstTime(job.getSize());


                    process.getPCB().setState(ProcessState.READY);
                    process.getPCB().setStartTime(systemClock.getTicks()); // tick de tiempo de inicio del proceso
                    process.getPCB().setDiskStartPosition(job.getDiskStartAddress());
                    process.getPCB().setDiskProgramSize(job.getSize());


                    Loader.loadToMemory(process, memory, disk);

                    processList.addProcess(process);
                    admit(process);

                } else {
                    int virtualMemoryAddress = -1;
                    try {
                        virtualMemoryAddress = Loader.getFreeSpaceInVirtualMemory(job.getSize(), disk);
                    } catch (Exception e) {
                        System.out.println("No hay espacio en la memoria virtual del disco para alojar el código suspendido.");
                        return;
                    }

                    onlyMemVirtual = true;

                    Process process = new Process(job.getAssignedPID(), virtualMemoryAddress, pcbStartAddress);
                    process.setAssignedCpuId(job.getAssignedCpuId());
                    process.setFilePathAndName(job.getPathFile(), job.getNameFile());
                    process.getPCB().setPriority(job.getPriority());
                    process.setArrivalTime(job.getArrivalTime());
                    process.setBurstTime(job.getSize());
                    
                    process.getPCB().setState(ProcessState.READY_SUSPENDED); 
                    process.getPCB().setStartTime(systemClock.getTicks());
                    process.getPCB().setDiskStartPosition(job.getDiskStartAddress());
                    process.getPCB().setDiskProgramSize(job.getSize());

                    Loader.loadToVirtualMemory(process, disk);

                    processList.addProcess(process);
                    //admit(process);
                    System.out.println("Proceso PID " + process.getPCB().getPID() + " admitido en estado READY/SUSPEND (Cargado en Memoria Virtual).");
                }

                jobList.removeNextJob();
                int addrInRAM = job.getStartAddressInRAM();
                if (addrInRAM != -1) {
                    memory.setPosition(addrInRAM, null);
                    memory.setPosition(addrInRAM + 1, null);
                }
                
            }
        }
    }

    // revisa si hay algún proceso esperando en memoria virtual
    private boolean hayProcesosSuspendidos() {
        for (Process p : processList.getProcesses()) {
            if (p.getState() == ProcessState.READY_SUSPENDED) {
                return true;
            }
        }
        return false;
    }

    public void setSelfish(boolean selfish, double a, double b) {
        this.selfish = selfish;
        this.selfishA = a;
        this.selfishB = b;
    }

    // En selfish, los procesos recién admitidos esperan en la cola de nuevos
    private void admit(Process process) {
        if (selfish) {
            newLists.get(process.getAssignedCpuId() - 1).add(process);
        } else {
            addToReadyList(process);
        }
    }

    // Si no hay ningún aceptado, entra el nuevo de mayor prioridad (empate: el primero de la lista)
    public void admitIfNoAccepted(int cpuIndex) {
        if (!selfish) return;
        ArrayList<Process> ready = readyLists.get(cpuIndex);
        ArrayList<Process> nuevos = newLists.get(cpuIndex);
        if (!ready.isEmpty() || nuevos.isEmpty()) return;

        Process best = nuevos.get(0);
        for (Process p : nuevos) {
            if (p.getSelfishPriority() > best.getSelfishPriority()) best = p;
        }
        nuevos.remove(best);
        ready.add(best);
    }

    // Una vez por tick y por CPU: sube prioridades y promueve a los nuevos que alcanzan a los aceptados
    public void updateSelfish(int cpuIndex, Process running) {
        if (!selfish) return;
        ArrayList<Process> ready = readyLists.get(cpuIndex);
        ArrayList<Process> nuevos = newLists.get(cpuIndex);

        for (Process p : nuevos) p.addSelfishPriority(selfishA);
        for (Process p : ready) p.addSelfishPriority(selfishB);
        if (running != null) running.addSelfishPriority(selfishB);

        boolean hayAceptados = running != null || !ready.isEmpty();
        double max = (running != null) ? running.getSelfishPriority() : -1;
        for (Process p : ready) max = Math.max(max, p.getSelfishPriority());

        Iterator<Process> it = nuevos.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (!hayAceptados || p.getSelfishPriority() >= max) {
                it.remove();
                ready.add(p); // entra al final de la cola
                hayAceptados = true;
                max = Math.max(max, p.getSelfishPriority());
            }
        }
    }

    /**
     * Intenta activar procesos en READY_SUSPENDED pasándolos a READY si caben en RAM.
     * @return true si AÚN QUEDAN procesos en READY_SUSPENDED que no cupieron en RAM.
     */
    public boolean hasPendingSuspendedProcesses() {
        boolean haySuspendidosPendientes = false;
        boolean seguirComprobando = true;
        for (Process process : processList.getProcesses()) {
            if (process.getState() == ProcessState.READY_SUSPENDED) {
                try {
                    // Intenta trasladar de memoria virtual a RAM.
                    // Si no hay espacio en RAM, getFreeSpaceInMemory lanzará una excepción dentro de este método
                    Loader.loadFromVirtualToMemory(process, memory, disk);

                    // Si se ejecutó con éxito, cambiamos el estado a READY
                    process.getPCB().setState(ProcessState.READY);
                    updateStateAndInfo(process); // Actualiza el estado en la RAM
                    admit(process);
                    System.out.println("Proceso PID " + process.getPCB().getPID() + " activado con éxito: READY_SUSPENDED -> READY.");

                } catch (Exception e) {
                    // Si falló por falta de RAM, se mantiene en READY_SUSPENDED
                    haySuspendidosPendientes = true;
                    System.out.println(
                        "No se pudo activar PID " + process.getPCB().getPID() +
                        ": " + e.getMessage()
                    );
                    return haySuspendidosPendientes; // para evitar procesar otros
                }
            }
        }
        return haySuspendidosPendientes;
    }

    /**
     * Sincroniza el estado y la información de direcciones/ejecución del PCB 
     * con las celdas correspondientes en la memoria principal (RAM).
     * 
     * @param newState El nuevo estado del proceso.
     * @param memory Referencia a la memoria RAM del sistema.
     */
    public void updateStateAndInfo(Process process) {
        
        if (process.getPCB() != null && memory != null) {
            int baseRAM = process.getPCB().getMemoryPositionPCB();

            ProcessState newState = process.getPCB().getState();

            // State
            memory.setPosition(
                baseRAM + 1, 
                new MemoryRegister("bcp_state = " + newState.toString(), newState.ordinal())
            );

            // PC
            memory.setPosition(
                baseRAM + 2, 
                new MemoryRegister("bcp_pc = " + process.getPCB().getPC(), process.getPCB().getPC())
            );

            // base address
            int posBaseAddr = baseRAM + 14 + SystemConfig.STACK_SIZE;
            if (SystemConfig.getMemoryStrategyAssignment().equals("paging")) {
                memory.setPosition(
                    posBaseAddr, 
                    new MemoryRegister("bcp_pages_table = " + process.getPCB().getPagesTableString(), process.getPCB().getPagesTableString())
                );
            } else {
                memory.setPosition(
                    posBaseAddr, 
                    new MemoryRegister("bcp_base_address = " + process.getPCB().getStartPosition(), process.getPCB().getStartPosition())
                );
            }

            // size
            int posSizeScope = baseRAM + 15 + SystemConfig.STACK_SIZE;
            memory.setPosition(
                posSizeScope, 
                new MemoryRegister("bcp_size_scope = " + process.getPCB().getDiskProgramSize(), process.getPCB().getDiskProgramSize())
            );
        }
    }
}
