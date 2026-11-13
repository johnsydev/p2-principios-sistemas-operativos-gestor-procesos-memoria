package minipcsimulator.model;

import java.util.ArrayList;
import java.util.Stack;
import minipcsimulator.utils.SystemConfig;

/**
 * Clase Dispatcher que se encarga de despachar un proceso al CPU, cambiando su estado a RUNNING y enviando su PCB al CPU.
 * @author johnsydev
 */
public class Dispatcher {

    /**
     * Despacha un proceso al CPU, cambiando su estado a RUNNING y enviando su PCB al CPU.
     * @param process El proceso a despachar.
     * @param cpu El CPU al que se enviará el proceso.
     */
    public static void dispatch(Process process, CPU cpu) {
        PCB pcb = process.getPCB();
        pcb.setState(Process.ProcessState.RUNNING);
        cpu.setPCB(pcb);
        pcb.setCpuID(cpu.getCpuID());
    }

    /**
     * Guarda el contexto del proceso en la memoria.
     * @param process El proceso del que se guardará el contexto.
     * @param cpu El CPU del que se obtendrán los registros.
     * @param memory La memoria en la que se guardarán los registros.
     */
    public static void saveContext(Process process, CPU cpu, MainMemory memory) {
        Dispatcher.saveRegistersIntoMemory(process, cpu, memory);
    }

    // elige el siguiente de la lista de un CPU
    public static Process selectNext(ArrayList<Process> readyList) {
        if (readyList.isEmpty()) return null;
        return readyList.remove(0);
    }

    /**
     * Esto guarda los registros del CPU en la memoria RAM y actualiza la vista de la memoria.
     * NOTA: Esto se hace para que se pueda visualizar el proceso en la GUI en la memoria principal tal como se solicitó, posteriormente se debe modificar.
     * Estado del proceso RUNNING
     */
    public static void saveRegistersIntoMemory(Process process, CPU cpu, MainMemory memory) {
        PCB pcb = cpu.getPCB();
        pcb.setPC(cpu.getPC());
        pcb.setAC(cpu.getAC());
        pcb.setAX(cpu.getAX());
        pcb.setBX(cpu.getBX());
        pcb.setCX(cpu.getCX());
        pcb.setDX(cpu.getDX());
        pcb.setPSW(cpu.getPSW());
        pcb.setStack(cpu.getStack());

        int pid = pcb.getPID();
        String state = pcb.getState().toString();
        int pc = pcb.getPC();
        int ac = pcb.getAC();
        String ax = pcb.getAX();
        int bx = pcb.getBX();
        int cx = pcb.getCX();
        String dx = pcb.getDX();

        int psw = pcb.getPSW();

        int cpu_id = pcb.getCpuID();
        int start_time = pcb.getStartTime();
        int spent_time = pcb.getTimeSpent();
        int base_address = pcb.getStartPosition();
        int size_scope = pcb.getSizeProcessScope();
        int priority = pcb.getPriority();

        String openedFiles = pcb.getStringOpenedFiles();

        Stack<Integer> stack = pcb.getStack();

        // pos memoria BCP
        int memoryPosition = pcb.getMemoryPositionPCB();
        memory.setPosition(memoryPosition, new MemoryRegister("bcp_pid = " + pid, pid));
        memory.setPosition(memoryPosition+1, new MemoryRegister("bcp_state = " + state, pcb.getState().ordinal()));
        memory.setPosition(memoryPosition+2, new MemoryRegister("bcp_pc = " + pc, pc));
        memory.setPosition(memoryPosition+3, new MemoryRegister("bcp_ac = " + ac, ac));
        memory.setPosition(memoryPosition+4, new MemoryRegister("bcp_ax = " + ax, ax));
        memory.setPosition(memoryPosition+5, new MemoryRegister("bcp_bx = " + bx, bx));
        memory.setPosition(memoryPosition+6, new MemoryRegister("bcp_cx = " + cx, cx));
        memory.setPosition(memoryPosition+7, new MemoryRegister("bcp_dx = " + dx, dx));
        memory.setPosition(memoryPosition+8, new MemoryRegister("bcp_psw = " + psw, psw));
        for (int i = 0; i < SystemConfig.STACK_SIZE; i++) {
            if (i < stack.size()) {
                memory.setPosition(memoryPosition + 9 + i, new MemoryRegister("bcp_stack[" + i + "] = " + stack.get(i), stack.get(i)));
            } else {
                memory.setPosition(memoryPosition + 9 + i, new MemoryRegister("bcp_stack[" + i + "] = 0", 0));
            }
        }
        memory.setPosition(memoryPosition + 9 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_cpu_id = " + cpu_id, cpu_id));
        memory.setPosition(memoryPosition + 10 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_start_time = " + start_time, start_time));
        memory.setPosition(memoryPosition + 11 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_spent_time = " + spent_time, spent_time));
        
        memory.setPosition(memoryPosition + 12 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_opened_files = " + openedFiles, 0)); //PENDIENTE
        memory.setPosition(memoryPosition + 13 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_next_bcp = " + "null", ""));
        memory.setPosition(memoryPosition + 14 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_base_address = " + base_address, base_address));
        memory.setPosition(memoryPosition + 15 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_size_scope = " + size_scope, size_scope));

        memory.setPosition(memoryPosition + 16 + SystemConfig.STACK_SIZE, new MemoryRegister("bcp_priority = " + priority, priority));
    }

}
