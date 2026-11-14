package minipcsimulator.model;

import java.util.ArrayList;
import java.util.List;
import minipcsimulator.utils.SystemConfig;

public class ProcessList {
    Process head;
    ArrayList<Process> processList = new ArrayList<>();
    ArrayList<Process> deletedProcesses = new ArrayList<>();
    private MainMemory memory;

    public ProcessList(MainMemory memory) {
        this.head = null;
        this.memory = memory;
    }

    public void addProcess(Process process) {
        if (head == null) {
            head = process;
        } else {
            PCB current = head.getPCB();
            while (current.getNextPCB() != null) { //recorro los PCBs
                current = current.getNextPCB();
            }
            current.setNextPCB(process.getPCB()); // anido el nuevo PCB al final de la lista
        }
        processList.add(process);
        PCB pcb = process.getPCB(); 
        int memoryPosition = pcb.getMemoryPositionPCB(); 

        Process.ProcessState state = pcb.getState();

        // Escribir BCP Inicial en la RAM 
        memory.setPosition(memoryPosition, new MemoryRegister("bcp_pid = " + pcb.getPID(), pcb.getPID()));
        memory.setPosition(memoryPosition + 1, new MemoryRegister("bcp_state = " + state.toString(), state.ordinal()));
        memory.setPosition(memoryPosition + 2, new MemoryRegister("bcp_pc = " + pcb.getStartPosition(), pcb.getStartPosition()));
        memory.setPosition(memoryPosition + 3, new MemoryRegister("bcp_ac = 0", 0));
        memory.setPosition(memoryPosition + 4, new MemoryRegister("bcp_ax = 0", 0));
        memory.setPosition(memoryPosition + 5, new MemoryRegister("bcp_bx = 0", 0));
        memory.setPosition(memoryPosition + 6, new MemoryRegister("bcp_cx = 0", 0));
        memory.setPosition(memoryPosition + 7, new MemoryRegister("bcp_dx = 0", 0));
        memory.setPosition(memoryPosition + 8, new MemoryRegister("bcp_psw = 0", 0));

        // pila
        for (int i = 0; i < SystemConfig.STACK_SIZE; i++) {
            memory.setPosition(memoryPosition + 9 + i, new MemoryRegister("bcp_stack[" + i + "] = 0", 0));
        }

        memory.setPosition(memoryPosition + 9 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_cpu_id = 0", 0));
        memory.setPosition(memoryPosition + 10 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_start_time = " + pcb.getStartTime(), pcb.getStartTime()));
        memory.setPosition(memoryPosition + 11 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_spent_time = 0", 0));
        memory.setPosition(memoryPosition + 12 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_opened_files = null", 0));

        // Si ya había un proceso antes, actualizar su campo bcp_next_bcp en RAM para que apunte a este nuevo
        if (!this.processList.isEmpty()) {
            Process ultimoProceso = this.processList.get(this.processList.size() - 1);
            int posNextBCP = ultimoProceso.getPCB().getMemoryPositionPCB() + 13 + SystemConfig.STACK_SIZE;
            memory.setPosition(posNextBCP, new MemoryRegister("bcp_next_bcp = " + memoryPosition, memoryPosition));
        }

        // El nuevo BCP se inicializa apuntando a -1 (Fin de lista)
        memory.setPosition(memoryPosition + 13 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_next_bcp = -1", -1));
        memory.setPosition(memoryPosition + 14 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_base_address = " + pcb.getStartPosition(), pcb.getStartPosition()));
        memory.setPosition(memoryPosition + 15 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_size_scope = " + pcb.getSizeProcessScope(), pcb.getSizeProcessScope()));
        memory.setPosition(memoryPosition + 16 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_priority = " + pcb.getPriority(), pcb.getPriority()));
    }

    public void removeProcess(int pid) {
        Process process = getProcessByPID(pid);
        if (process == null) {
            System.out.println("ERROR: Proceso " + pid + " ya no está en la lista.");
            return;
        }
        System.out.println("Proceso " + pid + " eliminado.");
        System.out.println("Estado del proceso antes de eliminarlo: " + process.getPCB());
        System.out.println("Lista de procesos: " + processList);
        deletedProcesses.add(process);
        processList.remove(process);
        head = processList.isEmpty() ? null : processList.get(0);

        PCB pcb = process.getPCB();
        int pos_end = pcb.getStartPosition() + pcb.getSizeProcessScope();
        for (int i = pcb.getStartPosition(); i < pos_end; i++) {
            memory.setPosition(i, null); // Limpia la memoria ocupada por el proceso
        }

        int pcb_end = pcb.getMemoryPositionPCB() + SystemConfig.PCB_SIZE;
        for (int i = pcb.getMemoryPositionPCB(); i < pcb_end; i++) {
            memory.setPosition(i, null); // Limpia la memoria ocupada por el PCB
        }
    }

    public Process getProcessByPID(int pid) {
        for (Process process : processList) {
            if (process.getPCB().getPID() == pid) {
                return process;
            }
        }
        return null; // No se encontró el proceso con el PID especificado
    }

    public Process getFirstProcess() {
        return head;
    }

    public boolean hasProcesses() {
        return !processList.isEmpty();
    }

    public int getProcessCount() {
        return processList.size();
    }

    public List<Object[]> getTableData() {
        List<Object[]> tableData = new ArrayList<>();
        for (Process dProcess : deletedProcesses) {
            Object[] rowData = new Object[]{
                dProcess.getPID(),
                dProcess.getNameFile(),
                dProcess.getState(),
            };
            tableData.add(rowData);
        }
        for (Process process : processList) {
            Object[] rowData = new Object[]{
                process.getPID(),
                process.getNameFile(),
                process.getState(),
            };
            tableData.add(rowData);
        }
        return tableData;
    }

    public List<Process> getDeletedProcesses() {
        return deletedProcesses;
    }

    public List<Process> getProcesses() {
        return processList;
    }
}
