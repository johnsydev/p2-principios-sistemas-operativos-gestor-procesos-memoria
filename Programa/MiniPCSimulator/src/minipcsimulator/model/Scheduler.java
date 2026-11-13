package minipcsimulator.model;

import java.util.ArrayList;
import java.util.LinkedList;

import minipcsimulator.model.Process.ProcessState;
import minipcsimulator.utils.SystemConfig;

public class Scheduler {
    private JobList jobList;
    private ProcessList processList;
    private MainMemory memory;
    private Disk disk;
    private SystemClock systemClock;

    private ArrayList<ArrayList<Process>> readyLists = new ArrayList<>();

    public Scheduler(JobList jobList, ProcessList processList, MainMemory memory, Disk disk, SystemClock systemClock, int numCPUs) {
        this.jobList = jobList;
        this.processList = processList;
        this.memory = memory;
        this.disk = disk;
        this.systemClock = systemClock;

        // una lista de listos por CPU
        for (int i = 0; i < numCPUs; i++) {
            readyLists.add(new ArrayList<>());
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
        boolean onlyMemVirtual = false;
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
                        return; // Si ya se admitió un proceso en memoria virtual, no admitir más en RAM, FCFS
                    }
                    Process process = new Process(job.getAssignedPID(), memoryStartAddress, pcbStartAddress);
                    process.setAssignedCpuId(job.getAssignedCpuId());
                    process.setFilePathAndName(job.getPathFile(), job.getNameFile());

                    process.getPCB().setState(ProcessState.READY);
                    process.getPCB().setStartTime(systemClock.getTicks()); // tick de tiempo de inicio del proceso
                    process.getPCB().setDiskStartPosition(job.getDiskStartAddress());
                    process.getPCB().setDiskProgramSize(job.getSize());

                    Loader.loadToMemory(process, memory, disk);

                    processList.addProcess(process);
                    addToReadyList(process);

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

                    process.getPCB().setState(ProcessState.READY_SUSPENDED); 
                    process.getPCB().setStartTime(systemClock.getTicks());
                    process.getPCB().setDiskStartPosition(job.getDiskStartAddress());
                    process.getPCB().setDiskProgramSize(job.getSize());

                    Loader.loadToVirtualMemory(process, disk);

                    processList.addProcess(process);
                    addToReadyList(process);
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
            memory.setPosition(
                posBaseAddr, 
                new MemoryRegister("bcp_base_address = " + process.getPCB().getStartPosition(), process.getPCB().getStartPosition())
            );

            // size
            int posSizeScope = baseRAM + 15 + SystemConfig.STACK_SIZE;
            memory.setPosition(
                posSizeScope, 
                new MemoryRegister("bcp_size_scope = " + process.getPCB().getDiskProgramSize(), process.getPCB().getDiskProgramSize())
            );
        }
    }
}
