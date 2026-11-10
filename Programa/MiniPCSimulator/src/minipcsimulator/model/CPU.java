package minipcsimulator.model;

import java.util.ArrayList;
import java.util.Stack;
import minipcsimulator.utils.SystemConfig;

/**
 * Clase CPU que se encarga de ejecutar las instrucciones de un proceso.
 * @author johnsydev
 */
public class CPU {

    // Codigos de operación
    public enum OpCode {
        // Instrucciones
        LOAD(1, "INSTRUCTION", 1), 
        STORE(2, "INSTRUCTION", 1), 
        MOV(3, "INSTRUCTION", 1),
        ADD(4, "INSTRUCTION", 1), 
        SUB(5, "INSTRUCTION", 1), 
        INC(6, "INSTRUCTION", 1),
        DEC(7, "INSTRUCTION", 1),
        SWAP(8, "INSTRUCTION", 1),
        INT_20H(9, "INSTRUCTION", 1),
        INT_10H(10, "INSTRUCTION", 1),
        INT_21H(12, "INSTRUCTION", 1),
        JMP(13, "INSTRUCTION", 1),
        CMP(14, "INSTRUCTION", 1),
        JE(15, "INSTRUCTION", 1),
        JNE(16, "INSTRUCTION", 1),
        PARAM(17, "INSTRUCTION", 1),
        PUSH(18, "INSTRUCTION", 1),
        POP(19, "INSTRUCTION", 1),

        // Registros
        AX(5, "REGISTER", 0),
        BX(6, "REGISTER", 0),
        CX(7, "REGISTER", 0),
        DX(8, "REGISTER", 0),

        //PARTES DE AX
        AH(9, "PART_REGISTER", 0),
        AL(10, "PART_REGISTER", 0);

        private final int id;
        private final String type;
        private final int weight;

        // Constructor
        OpCode(int id, String type, int weight) {
            this.id = id;
            this.type = type;
            this.weight = weight;
        }

        public String getCode() {
            return this.name();
        }

        public String getType() {
            return type;
        }

        /**
         * Obtiene un OpCode por su código.
         * @param code El código del OpCode a buscar.
         * @return El OpCode correspondiente al código, o null si no se encuentra.
         */
        public static OpCode getByCode(String code) {
            for (OpCode op : values()) {
                if (op.getCode().equals(code.trim().toUpperCase())) {
                    return op;
                }
            }
            return null;
        }
    }

    private static int cpu_count = 0;

    private int cpu_id;

    private MainMemory memory;
    private InterruptHandler interruptHandler;
    private PCB pcb;

    private int PC = 0;
    private int AC = 0;
    private Instruction IR;
 
    private String AX = "0";
    private int BX = 0;
    private int CX = 0;
    private String DX = "0";

    private Stack<Integer> stack;

    private int PSW = 0; // program status word, para banderas aritmeticas y de control, para este proyecto es solo Zero Flag (ZF)

    // Controles internos

    private String outputMessages = "";
    private boolean hasPendingOutput = false;
    private int ticksRemaining = 0;
    private int currentInstructionAddress = 0;

    /**
     * Constructor de la clase CPU.
     * @param memory La memoria principal del sistema.
     * @param interruptHandler El manejador de interrupciones del sistema.
     */
    public CPU(MainMemory memory, InterruptHandler interruptHandler) {
        this.cpu_id = ++cpu_count;
        this.memory = memory;
        this.interruptHandler = interruptHandler;
    }

    /**
     * Asigna un PCB al CPU, actualizando los registros del CPU con los valores del PCB.
     * Esta información es enviada por el Dispatcher cuando se despacha un proceso al CPU.
     * @param pcb El PCB del proceso a ejecutar.
     */    
    public void setPCB(PCB pcb) {
        if (pcb == null) {
            clearPCB();
            return;
        }
        this.pcb = pcb;

        this.PC = pcb.getPC();
        this.AC = pcb.getAC();
        this.AX = pcb.getAX();
        this.BX = pcb.getBX();
        this.CX = pcb.getCX();
        this.DX = pcb.getDX();

        this.stack = pcb.getStack();

        this.PSW = pcb.getPSW();
    }

    public void clearPCB() {
        this.pcb = null;
        this.PC = 0;
        this.AC = 0;
        this.AX = "0";
        this.BX = 0;
        this.CX = 0;
        this.DX = "0";
        this.stack = null;
        this.PSW = 0;
    }

    private int hexStringToInt(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return -1;
        }

        String limpio = hexStr.trim();

        if (limpio.endsWith("h")) {
            limpio = limpio.substring(0, limpio.length() - 1);
        } else if (limpio.startsWith("0x")) {
            limpio = limpio.substring(2);
        } else {
            return Integer.parseInt(limpio);
        }

        try {
            return Integer.parseInt(limpio, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Obtiene la instrucción que se debe ejecutar del proceso actualmente en ejecución en el CPU, mediante el PC.
     * Si el PC está fuera del rango de memoria del proceso, se asume que el proceso ha terminado y se cambia su estado a EXIT.
     */
    public void fetch() {
        if (PC >= pcb.getStartPosition() && PC < pcb.getEndPosition()) {
            this.IR = memory.getInstruction(PC);
            this.currentInstructionAddress = this.PC;
            this.PC++;
        } else {
            pcb.setState(Process.ProcessState.EXIT);
            throw new RuntimeException("Error: PC fuera del rango de memoria del proceso. (Segmentation Fault)");
        }
    }

    /**
     * Ejecuta la instrucción actual del proceso que está actualmente en ejecución en el CPU.
     * Realiza el decode de las instrucciones y ejecuta la operación correspondiente según el opcode y los registros involucrados.
     */
    public void execute() {
        // se obtienen los datos de la instrucción actual
        String instructionType = IR.getInstructionType();
        if (instructionType.equals("INT")) {
            String op = IR.getOperand(0).toUpperCase().trim();
            instructionType = "INT_" + op;
        }
        System.out.println("CPU: Ejecutando instrucción " + instructionType + " con operandos: " + IR.getOperands());
        OpCode opcode = OpCode.getByCode(instructionType);

        


        if (opcode == null) {
            System.out.println("Error: Instrucción o registro no reconocido.");
            return;
        }

        switch (opcode) {
            case LOAD:
                executeLOAD(OpCode.getByCode(IR.getOperand(0)));
                break;
            case STORE:
                executeSTORE(OpCode.getByCode(IR.getOperand(0)));
                break;
            case ADD:
                executeADD(OpCode.getByCode(IR.getOperand(0)));
                break;
            case SUB:
                executeSUB(OpCode.getByCode(IR.getOperand(0)));
                break;
            case MOV:
                if (IR.getOperand(1).contains("\"")) {
                    executeMOV(OpCode.getByCode(IR.getOperand(0)), IR.getOperand(1).replace("\"", ""));    
                } else if (!IR.isOperandRegister(1)) { // si el segundo operando no es un registro, se asume que es un valor numérico
                    if (IR.getOperand(1) != null && !IR.getOperand(1).isEmpty()) {
                        try {
                            executeMOV(OpCode.getByCode(IR.getOperand(0)), hexStringToInt(IR.getOperand(1)));
                        } catch (NumberFormatException e) {
                            //despues ver
                        }
                    }
                }
                else { // se trata como registro
                    executeMOV(OpCode.getByCode(IR.getOperand(0)), OpCode.getByCode(IR.getOperand(1)));
                }
                break;
            case INC:
                if (IR.getOperandsCount() == 0) {
                    executeINC();
                } else {
                    executeINC(OpCode.getByCode(IR.getOperand(0)));
                }
                break;
            case DEC:
                if (IR.getOperandsCount() == 0) {
                    executeDEC();
                } else {
                    executeDEC(OpCode.getByCode(IR.getOperand(0)));
                }
                break;
            case SWAP:
                executeSWAP(OpCode.getByCode(IR.getOperand(0)), OpCode.getByCode(IR.getOperand(1)));
                break;
            case JMP:
                try {
                    executeJMP(Integer.parseInt(IR.getOperand(0)));
                } catch (NumberFormatException e) {
                    //despues ver
                }
                break;
            case JE:
                try {
                    executeJE(Integer.parseInt(IR.getOperand(0)));
                } catch (NumberFormatException e) {
                    //despues ver
                }
                break;
            case JNE:
                try {
                    executeJNE(Integer.parseInt(IR.getOperand(0)));
                } catch (NumberFormatException e) {
                    //despues ver
                }
                break;
            case CMP:
                executeCMP(OpCode.getByCode(IR.getOperand(0)), OpCode.getByCode(IR.getOperand(1)));
                break;
            case PARAM:
                ArrayList<Integer> paramsPARAM = new ArrayList<>();
                for (int i = 0; i < IR.getOperandsCount(); i++) {
                    try {
                        paramsPARAM.add(Integer.parseInt(IR.getOperand(i)));
                    } catch (NumberFormatException e) {
                        //despues ver
                    }
                }
                executePARAM(paramsPARAM);
                break;
            case PUSH:
                executePUSH(OpCode.getByCode(IR.getOperand(0)));
                break;
            case POP:
                executePOP(OpCode.getByCode(IR.getOperand(0)));
                break;
            case INT_20H:
                executeINT_20H();
                break;
            case INT_10H:
                executeINT_10H();               
                break;
            case INT_21H:
                executeINT_21H();
                break;
            default:
                System.out.println("Error: Operación no reconocida.");
        }
    }

    // Ejecución de instrucciones específicas

    /**
     * Ejecuta la instrucción LOAD, cargando el valor del registro especificado en el acumulador (AC).
     * Ejemplo: LOAD AX cargará el valor del registro AX en el AC.
     * @param register El registro desde el cual se cargará el valor al AC.
     */
    public void executeLOAD(OpCode register) {
        switch (register) {
            case AX:
                AC = getIntAX();
                break;
            case BX:
                AC = BX;
                break;
            case CX:
                AC = CX;
                break;
            case DX:
                AC = getIntDX();
                break;
            case AH:
                AC = getAH();
                break;
            case AL:
                AC = getAL();
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción STORE, guardando el valor del acumulador (AC) en el registro especificado.
     * Ejemplo: STORE AX guardará el valor del AC en el registro AX.
     * @param register El registro en el cual se guardará el valor del AC.
     */
    public void executeSTORE(OpCode register) {
        switch (register) {
            case AX:
                setAX(AC);
                break;
            case BX:
                BX = AC;
                break;
            case CX:
                CX = AC;
                break;
            case DX:
                setDX(AC);
                break;
            case AH:
                setAH(AC);
                break;
            case AL:
                setAL(AC);
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción ADD, sumando el valor del registro especificado al acumulador (AC).
     * Ejemplo: ADD AX sumará el valor del registro AX al AC.
     * @param register El registro desde el cual se sumarán los valores al AC.
     */
    public void executeADD(OpCode register) {
        switch (register) {
            case AX:
                AC += getIntAX();
                break;
            case BX:
                AC += BX;
                break;
            case CX:
                AC += CX;
                break;
            case DX:
                AC += getIntDX();
                break;
            case AH:
                AC += getAH();
                break;
            case AL:
                AC += getAL();
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción SUB, restando el valor del registro especificado al acumulador (AC).
     * Ejemplo: SUB AX restará el valor del registro AX al AC.
     * @param register El registro desde el cual se restarán los valores al AC.
     */
    public void executeSUB(OpCode register) {
        switch (register) {
            case AX:
                AC -= getIntAX();
                break;
            case BX:
                AC -= BX;
                break;
            case CX:
                AC -= CX;
                break;
            case DX:
                AC -= getIntDX();
                break;
            case AH:
                AC -= getAH();
                break;
            case AL:
                AC -= getAL();
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción MOV, moviendo el valor especificado al registro indicado.
     * @param register El registro en el cual se moverá el valor.
     * @param value El valor a establecer en el registro (mover).
     */
    public void executeMOV(OpCode register, int value) {
        switch (register) {
            case AX:
                setAX(value);
                break;
            case BX:
                BX = value;
                break;
            case CX:
                CX = value;
                break;
            case DX:
                setDX(value);
                break;
            case AH:
                setAH(value);
                break;
            case AL:
                setAL(value);
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    public void executeMOV(OpCode register, String value) {
        switch (register) {
            case DX:
                setDX(value);
                break;
            default:
                System.out.println("Error: Registro no reconocido para mover cadena.");
        }
    }

    /**
     * Ejecuta la instrucción MOV, moviendo el valor de un registro a otro.
     * @param destRegister El registro destino donde se moverá el valor.
     * @param srcRegister El registro fuente desde donde se obtendrá el valor.
     */
    public void executeMOV(OpCode destRegister, OpCode srcRegister) { //método sobrecargado
        switch (srcRegister) {
            case AX:
                executeMOV(destRegister, getIntAX());
                break;
            case BX:
                executeMOV(destRegister, BX);
                break;
            case CX:
                executeMOV(destRegister, CX);
                break;
            case DX:
                executeMOV(destRegister, getIntDX());
                break;
            case AH: //PENDIENTE POSIBLE OVERFLOW
                executeMOV(destRegister, getAH());
                break;
            case AL:
                executeMOV(destRegister, getAL());
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción INC, incrementando en 1 el valor del acumulador (AC).
     */
    public void executeINC() {
        AC++;
    }

    /**
     * Ejecuta la instrucción INC, incrementando en 1 el valor del registro especificado.
     * @param register El registro que se incrementará en 1.
     */
    public void executeINC(OpCode register) {
        switch (register) {
            case AX:
                setAX(getIntAX() + 1);
                break;
            case BX:
                BX++;
                break;
            case CX:
                CX++;
                break;
            case DX:
                setDX(getIntDX() + 1);
                break;
            case AH:
                setAH(getAH() + 1);
                break;
            case AL:
                setAL(getAL() + 1);
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción DEC, decrementando en 1 el valor del acumulador (AC).
     */
    public void executeDEC() {
        AC--;
    }

    /**
     * Ejecuta la instrucción DEC, decrementando en 1 el valor del registro especificado.
     * @param register El registro que se decrementará en 1.
     */
    public void executeDEC(OpCode register) {
        switch (register) {
            case AX:
                setAX(getIntAX() - 1);
                break;
            case BX:
                BX--;
                break;
            case CX:
                CX--;
                break;
            case DX:
                setDX(getIntDX() - 1);
                break;
            case AH:
                setAH(getAH() - 1);
                break;
            case AL:
                setAL(getAL() - 1);
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    // Para swap:
    /**
     * Obtiene el valor de un registro.
     * @param register El registro del cual se obtiene el valor.
     * @return El valor del registro.
     */
    private int getRegisterValue(OpCode register) {
        switch (register) {
            case AX:
                return getIntAX();
            case BX:
                return BX;
            case CX:
                return CX;
            case DX:
                return getIntDX();
            case AH:
                return getAH();
            case AL:
                return getAL();
            default:
                System.out.println("Error: Registro no reconocido.");
                return 0;
        }
    }

    /**
     * Establece el valor de un registro.
     * @param register El registro al cual se le asignará el valor.
     * @param value El valor a asignar al registro.
     */
    private void setRegisterValue(OpCode register, int value) {
        switch (register) {
            case AX:
                setAX(value);
                break;
            case BX:
                BX = value;
                break;
            case CX:
                CX = value;
                break;
            case DX:
                setDX(value);
                break;
            case AH:
                setAH(value);
                break;
            case AL:
                setAL(value);
                break;
            default:
                System.out.println("Error: Registro no reconocido.");
        }
    }

    /**
     * Ejecuta la instrucción SWAP, intercambiando los valores de dos registros especificados.
     * @param reg1 El primer registro a intercambiar.
     * @param reg2 El segundo registro a intercambiar.
     */
    public void executeSWAP(OpCode reg1, OpCode reg2) {
        if (reg1 == null || reg2 == null) {
            System.out.println("Error: Registro no reconocido.");
            return;
        } 
        int valueR1 = getRegisterValue(reg1);
        int valueR2 = getRegisterValue(reg2);

        setRegisterValue(reg1, valueR2);
        setRegisterValue(reg2, valueR1);
    }

    /**
     * Ejecuta la instrucción JMP, desplazando el valor del contador de programa (PC) la cantidad de posiciones especificada.
     * @param countPositions Desplazamiento de posiciones a realizar en el PC (positivo o negativo).
     */
    public void executeJMP(int countPositions) {
        int newPC = this.PC + countPositions;
        if (newPC >= pcb.getStartPosition() && newPC < pcb.getEndPosition()) {
            this.PC = newPC;
        } else {
            pcb.setState(Process.ProcessState.EXIT); // PENDIENTE SEGMENTATION FAULT
            throw new RuntimeException("Error: Dirección de salto fuera del rango del proceso (Segmentation Fault).");
        }
    }

    /**
     * Ejecuta la instrucción JE (Jump if Equal), desplazando el valor del contador de programa (PC) la cantidad de posiciones especificada si el Zero Flag (ZF) está activado.
     * @param countPositions Desplazamiento de posiciones a realizar en el PC (positivo o negativo).
     */
    public void executeJE(int countPositions) {
        if (PSW == 1) { // Verifica zero flag
            executeJMP(countPositions);
        }
    }

    /**
     * Ejecuta la instrucción JNE (Jump if Not Equal), desplazando el valor del contador de programa (PC) la cantidad de posiciones especificada si el Zero Flag (ZF) está desactivado.
     * @param countPositions Desplazamiento de posiciones a realizar en el PC (positivo o negativo).
     */
    public void executeJNE(int countPositions) {
        if (PSW == 0) { // Verifica zero flag
            executeJMP(countPositions);
        }
    }

    /**
     * Ejecuta la instrucción CMP (Compare), comparando los valores de dos registros especificados y actualizando el Zero Flag (ZF) según el resultado de la comparación.
     * Si los valores de los registros son iguales, el bit del ZF se activa (1), de lo contrario, se desactiva (0).
     * @param reg1 El primer registro a comparar.
     * @param reg2 El segundo registro a comparar.
     */
    public void executeCMP(OpCode reg1, OpCode reg2) {
        int valueR1 = getRegisterValue(reg1);
        int valueR2 = getRegisterValue(reg2);

        if (valueR1 == valueR2) {
            PSW = 1; // se enciende bit de zero flag
        } else {
            PSW = 0; // se apaga bit de zero flag
        }
    }

    /**
     * Ejecuta la instrucción PARAM, agregando los parámetros especificados a la pila (stack) del proceso.
     * Si la pila está llena, se muestra un mensaje de error y se cambia el estado del proceso a EXIT.
     * @param params La lista de parámetros a agregar a la pila.
     */
    public void executePARAM(ArrayList<Integer> params) {
        for (Integer param : params) {
            if (stack.size() >= SystemConfig.STACK_SIZE) {
                pcb.setState(Process.ProcessState.EXIT); // PENDIENTE SEGMENTATION FAULT
                throw new RuntimeException("Error: Stack Overflow. No se puede hacer PARAM, la pila está llena.");
                //return;
            }
            stack.push(param);
        }
    }

    /**
     * Ejecuta la instrucción PUSH, agregando el valor del registro especificado a la pila (stack) del proceso.
     * Si la pila está llena, se muestra un mensaje de error y se cambia el estado del proceso a EXIT.
     * @param register El registro cuyo valor se agregará a la pila.
     */
    public void executePUSH(OpCode register) {
        int value = getRegisterValue(register);
        if (stack.size() >= SystemConfig.STACK_SIZE) {
            pcb.setState(Process.ProcessState.EXIT); // PENDIENTE SEGMENTATION FAULT
            throw new RuntimeException("Error: Stack Overflow. No se puede hacer PUSH, la pila está llena.");
            //return;
        }
        stack.push(value);
    }

    /**
     * Ejecuta la instrucción POP, eliminando el valor de la pila (stack) del proceso y almacenándolo en el registro especificado.
     * Si la pila está vacía, se muestra un mensaje de error y se cambia el estado del proceso a EXIT.
     * @param register El registro donde se almacenará el valor extraído de la pila.
     */
    public void executePOP(OpCode register) {
        if (stack.isEmpty()) {
            pcb.setState(Process.ProcessState.EXIT); // PENDIENTE SEGMENTATION FAULT
            throw new RuntimeException("Error: Stack Underflow. No se puede hacer POP, la pila está vacía.");
            //return;
        }
        int value = stack.pop();
        setRegisterValue(register, value);
    }

    /**
     * Ejecuta la instrucción INT 20H, finalizando el proceso actual y cambiando su estado a EXIT.
     * Esta instrucción se utiliza para indicar que el proceso ha terminado su ejecución.
     */
    public void executeINT_20H() {
        System.out.println("Finalizando proceso " + pcb.getPID());
        interruptHandler.handleInterruptEXIT(this);
    }

    /**
     * Ejecuta la instrucción INT 10H.
     * Esta instrucción se utiliza para realizar una operación específica.
     */
    public void executeINT_10H() {
        interruptHandler.handleInterruptIO(this, "OUTPUT");
    }

    // file manager
    public void executeINT_21H() {
        int op = getAH();
        System.out.println("INT 21H: Operación " + op + " solicitada por el proceso " + pcb.getPID());
        switch (op) {
            case 60: // 3CH
                interruptHandler.handleInterruptFileManager(this, "CREATE_FILE", getDX());
                break;
            case 61: // 3DH
                interruptHandler.handleInterruptFileManager(this, "OPEN_FILE", getDX());
                break;
            case 64: // 40H
                interruptHandler.handleInterruptFileManager(this, "WRITE_FILE", getDX());
                break;
            case 65: // 41H
                interruptHandler.handleInterruptFileManager(this, "DELETE_FILE", getDX());
                break;
            case 77: // 4DH
                interruptHandler.handleInterruptFileManager(this, "READ_FILE", getDX());
                break;
            default:
                System.out.println("Error: Operación de INT 21H no reconocida.");
        }
    }

    /**
     * Método principal que ejecuta una instrucción completa mediante el fecth - execute.
     * El fecth se encarga de obtener la instrucción y el execute se encarga de ejecutarla.
     * Si no hay ticks restantes, se realiza el fecth de la instrucción y se determina el tipo de instrucción para establecer los ticks necesarios para su ejecución.
     * Luego, se decrementa el contador de ticks restantes y se incrementa el tiempo total que el proceso ha estado en ejecución.
     * Si los ticks restantes llegan a cero, se ejecuta la instrucción.
     */
    public void executeInstruction() {
        if (ticksRemaining == 0) {
            fetch();

            String instructionType = IR.getInstructionType();
            if (instructionType.equals("INT")) {
                String op = IR.getOperand(0).toUpperCase().trim();
                instructionType = "INT_" + op;
            }
            OpCode opcode = OpCode.getByCode(instructionType);

            if (opcode != null) {
                ticksRemaining = opcode.weight;

                if (opcode == OpCode.INT_20H || opcode == OpCode.INT_10H || opcode == OpCode.INT_21H) { //PENDIENTE
                    pcb.setState(Process.ProcessState.BLOCKED);
                }
            } else {
                System.out.println("Error: Instrucción o registro no reconocido.");
            }
        }

        ticksRemaining--;
        pcb.incrementTimeSpent();

        if (ticksRemaining == 0) {
            execute();
        }
    }

    // SETTERS
    public void setAX(int value) {
        if (value < 0 || value > 65535) {
            System.out.println("Error: Valor fuera del rango permitido para AX (0-65535).");
            return;
        }
        this.AX = String.valueOf(value);
    }

    public void setAX(String value) {
        this.AX = value;
        if (value.contains("|")) {
            String[] parts = value.split("\\|");
            if (parts.length == 2) {
                setAH(Integer.parseInt(parts[0]));
                setAL(Integer.parseInt(parts[1]));
            }
        }
    }

    public void setAH(int value) {
        if (value < 0 || value > 255) {
            System.out.println("Error: Valor fuera del rango permitido para AH (0-255).");
            return;
        }
        int al = getAL();
        this.AX = value + "|" + al;
    }

    public void setAL(int value) {
        if (value < 0 || value > 255) {
            System.out.println("Error: Valor fuera del rango permitido para AL (0-255).");
            return;
        }
        int ah = getAH();
        this.AX = ah + "|" + value;
    }

    // GETTERS
    public int getPC() {
        return PC;
    }

    public String getIR() {
        return IR.getOriginalInstructionText();
    }

    public int getAC() {
        return AC;
    }

    private String decToHex(int value) {
        String hex = Integer.toHexString(value);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    public String getAX() {
        if (AX.contains("|")) {
            return getAH() + "|" + getAL();
        }
        return AX;
    }

    public int getAH() {
        if (AX.contains("|")) {
            return Integer.parseInt(AX.split("\\|")[0]);
        }
        return 0;
    }

    public int getAL() {
        if (AX.contains("|")) {
            return Integer.parseInt(AX.split("\\|")[1]);
        }
        return Integer.parseInt(AX);
    }

    public int getIntAX() {
        if (AX.contains("|")) {
            return getAH() * 256 + getAL();
        }
        return Integer.parseInt(AX);
    }

    public void setDX(String DX) {
        this.DX = DX;
    }

    public void setDX(int DX) {
        this.DX = String.valueOf(DX);
    }

    public int getBX() {
        return BX;
    }

    public int getCX() {
        return CX;
    }

    public String getDX() {
        return DX;
    }

    public int getIntDX() {
        try {
            return Integer.parseInt(DX);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getPSW() {
        return PSW;
    }

    public Stack<Integer> getStack() {
        return stack;
    }

    public int getCpuID() {
        return cpu_id;
    }

    public boolean hasTicksRemaining() {
        return ticksRemaining > 0;
    }

    public int getCurrentInstructionAddress() {
        return currentInstructionAddress;
    }

    public PCB getPCB() {
        return pcb;
    }

    public static void resetIDs() {
        cpu_count = 0;
    }
}
