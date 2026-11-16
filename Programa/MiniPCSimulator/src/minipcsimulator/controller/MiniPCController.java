/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipcsimulator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import minipcsimulator.gui.VentanaPrincipal;
import minipcsimulator.model.CPU;
import minipcsimulator.model.Disk;
import minipcsimulator.model.Dispatcher;
import minipcsimulator.model.FileIndex;
import minipcsimulator.model.InterruptHandler;
import minipcsimulator.model.Job;
import minipcsimulator.model.JobList;
import minipcsimulator.model.Loader;
import minipcsimulator.model.MainMemory;
import minipcsimulator.model.MemoryRegister;
import minipcsimulator.model.Process;
import minipcsimulator.model.Process.ProcessState;
import minipcsimulator.model.ProcessList;
import minipcsimulator.model.Scheduler;
import minipcsimulator.model.FileSystem;
import minipcsimulator.model.SystemClock;
import minipcsimulator.model.PCB;
import minipcsimulator.model.ArrivalQueue;
import minipcsimulator.model.ArrivalQueue.PendingProgram;
import minipcsimulator.model.AlgorithmFactory;
import minipcsimulator.model.FCFSAlgorithm;
import minipcsimulator.model.SchedulingAlgorithm;
import minipcsimulator.services.AsmParser;
import minipcsimulator.services.FileManager;
import minipcsimulator.utils.SystemConfig;

/**
 * Clase MiniPCController que actúa como Kernel del sistema simulado.
 * @author johnsydev
 */
public class MiniPCController {
    //private MiniPCModel modelo;
    private VentanaPrincipal vista;
    private MainMemory memory;
    private Disk disk;
    private CPU cpu;
    private ArrayList<CPU> cpus;
    private SystemClock systemClock;
    private JobList jobList;
    private ProcessList processList;
    private Scheduler scheduler;
    private InterruptHandler interruptHandler;
    private FileSystem fileSystem;
    private ArrivalQueue arrivalQueue;
    private SchedulingAlgorithm algorithm = new FCFSAlgorithm();
    private boolean selfishActivo = false;
    private int selfishA = 2;
    private int selfishB = 1;
    
    //hora de inicio
    private LocalDateTime startTimeSimulation;

    //tiempos de input
    private LocalDateTime startTimeInput;

    private boolean autoExecute = false;

    private boolean simulationStarted = false;

    private int cpuIdTab = 1;

    /**
     * Constructor de la clase MiniPCController.
     * Inicializa la vista, la memoria principal y el CPU.
     */
    public MiniPCController() {
        this.vista = new VentanaPrincipal();
        this.memory = new MainMemory();
        this.disk = new Disk();
        this.systemClock = new SystemClock();
        this.jobList = new JobList();
        this.processList = new ProcessList(this.memory);
        this.scheduler = new Scheduler(this.jobList, this.processList, this.memory, this.disk, this.systemClock, 1);
        this.fileSystem = new FileSystem(this.disk);
        this.interruptHandler = new InterruptHandler(this.memory, this.processList, this.scheduler, this.fileSystem);
        this.cpu = new CPU(this.memory, interruptHandler);
        System.out.println("Iniciando CPU..." + this.cpu.getCpuID());
        this.cpus = new ArrayList<>();
        this.cpus.add(this.cpu);
        this.arrivalQueue = new ArrivalQueue();

        agregarListeners();

        // Muestra GUI
        this.vista.setVisible(true);
        abrirConfiguracion();
    }

    private void tick() {
        systemClock.tick();
        vista.setTicks(systemClock.getTicks() + 1);
    }

    private void abrirConfiguracion() {
        vista.mostrarPanelConfig(false);
        System.out.println("Abriendo panel de configuración...");

        JButton btnGuardar = vista.getBtnGuardarConfig();

        if (btnGuardar == null) {
            return;
        }

        for (ActionListener listener : btnGuardar.getActionListeners()) {
            btnGuardar.removeActionListener(listener);
        }

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Guardando configuraciones...");
                boolean valida = aplicarConfiguraciones();
                if (valida && vista.getDialogConfig() != null) {
                    vista.getDialogConfig().dispose();
                }
            }
        });

        vista.mostrarPanelConfig(true);
    }

    /**
     * Agrega los listeners a los botones de la GUI para manejar las acciones del usuario.
     * Cuando se presiona un botón en la UI, se ejecuta la acción que se defina en esta sección.
     */
    public void agregarListeners() {
        // Botón para seleccionar archivo .asm
        vista.getBtnSeleccionar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seleccionarArchivos();
            }
        });

        // Botón para pausar el programa
        vista.getBtnPausar().addActionListener(e -> {
            autoExecute = false; // el programa se detiene en la siguiente vuelta
            vista.setModoAutomatico(false);
        });

        // Botón para ejecutar un paso del programa (modo paso a paso)
        vista.getBtnPasoAPaso().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!simulationStarted) {
                    abrirTablaPrevia(() -> ejecutarPasoAPaso());
                    return;
                }
                ejecutarPasoAPaso();
            }
        });

        // Botón para ejecutar todo el programa de una vez
        vista.getBtnEjecutar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!simulationStarted) {
                    abrirTablaPrevia(() -> ejecutarTodoPrograma());
                    return;
                }
                ejecutarTodoPrograma();
            }
        });

        // Botón para aplicar las configuraciones de memoria principal y espacio de kernel
        vista.getBtnAbrirConfig().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirConfiguracion();
            }
        });

        // Botón para reiniciar el sistema, limpiando la memoria y los procesos
        vista.getBtnLimpiar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciarSistema();
            }
        });

        vista.getBtnEstadisticas().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarSimulacion();
            }
        });

        // Botón para asignar tiempos de llegada
        vista.getBtnTiemposLlegada().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asignarTiemposLlegada();
            }
        });

        vista.getTabsCPU().addChangeListener(e -> {
            int indice = vista.getTabsCPU().getSelectedIndex();
            if (indice == -1) return;
            this.cpuIdTab = indice+1;
        });
    }

    /**
     * Valida la sintaxis y el tamaño de un archivo .asm.
     * @return las instrucciones parseadas, o null si el archivo tiene errores
     */
    private ArrayList<ArrayList<String>> validarSintaxis(String fileName, ArrayList<String> lines) {
        ArrayList<ArrayList<String>> asmArray;

        try {
            asmArray = AsmParser.verifySyntax(lines);
        } catch (Exception exc) {
            vista.mostrarError(fileName + ": " + exc.getMessage());
            return null;
        }

        if (asmArray.isEmpty()) {
            vista.mostrarError("El archivo " + fileName + " está vacío o no contiene instrucciones válidas.");
            return null;
        }

        if (asmArray.size() > SystemConfig.getUserMemorySize()) {
            vista.mostrarError("El programa " + fileName + " ingresado es demasiado grande para la memoria disponible. Tamaño máximo permitido: " + SystemConfig.getUserMemorySize() + " instrucciones.");
            return null;
        }

        if (!Loader.fitsInEmptyRAM(asmArray.size())) {
            vista.mostrarError("El programa " + fileName + " (" + asmArray.size() + " instrucciones) no cabe en la memoria principal con la estrategia de asignación seleccionada.");
            return null;
        }

        return asmArray;
    }

    /**
     * Esto procesa un archivo .asm cargado, verificando su sintaxis y generando un Job y un proceso en estado NEW.
     * @param fileData Un Map.Entry que contiene la ruta y nombre del archivo, y las líneas de código del archivo.
     * El primer elemento (key) es un ArrayList con la ruta y el nombre del archivo, y el segundo elemento (value) es un ArrayList con las líneas de código.
     */
    private void procesarArchivo(PendingProgram pendingProgram) {
        Map.Entry<ArrayList<String>, ArrayList<String>> fileData = pendingProgram.getFileData();
        String filePath;
        String fileName;
        ArrayList<String> lines = new ArrayList<>();
        try {
            
            filePath = fileData.getKey().get(0);
            fileName = fileData.getKey().get(1);
            lines = fileData.getValue();
        } catch (Exception exc) {
            vista.mostrarError(exc.getMessage());
            return;
        }
        
        ArrayList<ArrayList<String>> asmArray = validarSintaxis(fileName, lines);
        if (asmArray == null) {
            return;
        }

        System.out.println("Archivo cargado y verificado correctamente.");
        System.out.println(asmArray);

        // Aquí se hace el trabajo y las instrucciones se cargan, sin RAM aún

        FileIndex fileIndex = new FileIndex(fileName, -1, asmArray.size());
        int startAddress = -1;
        try {
            startAddress = Loader.loadProgram(lines, asmArray, this.disk, fileIndex); // sin ponerlo en tabla
        } catch (Exception e) {
            vista.mostrarError("El programa " + fileName + " no se puede cargar al sistema. " + e.getMessage());
            return;
        }

        Job job = new Job(filePath, fileName, lines, asmArray, pendingProgram.getAssignedCpuId());
        job.setArrivalTime(pendingProgram.getArrivalTime());
        job.setPriority(pendingProgram.getPriority());
        job.setDiskStartAddress(startAddress);
        int jobAddressRAM = -1;
        try {
            jobAddressRAM = Loader.getFreeSpaceForJobInfo(this.memory);
        } catch (Exception e) {
            Job.revertPID();
            vista.mostrarError("El programa " + fileName + " no se puede cargar al sistema. No hay espacio suficiente en la RAM para almacenar los datos del Job.");
            return;
        }

        job.setStartAddressInRAM(jobAddressRAM);
        memory.setPosition(jobAddressRAM, new MemoryRegister("PID = " + job.getAssignedPID(), job.getAssignedPID()));
        memory.setPosition(jobAddressRAM + 1, new MemoryRegister("DiskIndexPosition = " + fileIndex.getDiskIndexPosition(), fileIndex.getDiskIndexPosition()));

        this.jobList.addJob(job);
        actualizarVistaListaProcesos();

        vista.deshabilitarConfiguraciones();
        ArrayList<List<Object[]>> diskLists = this.disk.getAllDiskRows();
        vista.actualizarTablaDisco(diskLists.get(0));
        vista.actualizarTablaMemoriaVirtual(diskLists.get(1));
        List<Object[]> memoryRows = this.memory.getAllMemoryRows();
        vista.actualizarTablaMemoria(memoryRows, -1);
    }

    /**
     * Esto abre el cuadro de diálogo para seleccionar un archivo .asm, lo carga y verifica su sintaxis.
     * Genera el proceso (estado NEW)
     * Genera la tabla de instrucciones en la GUI
     */
    private void seleccionarArchivos() {
        if (this.processList.hasProcesses()) {
            vista.mostrarError("Ya hay un programa cargado. Debe finalizarlo y limpiar el sistema antes de cargar otro.");
            return;
        }


        List<Map.Entry<ArrayList<String>, ArrayList<String>>> filesData;
        try {
            filesData = FileManager.loadFiles(this.jobList.getAllJobNames());
        } catch (Exception exc) {
            vista.mostrarError(exc.getMessage());
            return;
        }

        int numCPUs = SystemConfig.getNumCPUs();

        for (Map.Entry<ArrayList<String>, ArrayList<String>> fileData : filesData) {
            String fileName = fileData.getKey().get(1);
            ArrayList<ArrayList<String>> asmArray = validarSintaxis(fileName, fileData.getValue());

            if (asmArray == null) {
                continue; // tiene errores entonces no se agrega
            }

            PendingProgram program = new PendingProgram(fileData, fileName, asmArray.size());
            //program.setCpu(this.arrivalQueue.getNextCpuID(numCPUs));
            this.arrivalQueue.add(program);
        }

        if (this.arrivalQueue.hasPending()) {
            vista.deshabilitarConfiguraciones();
        }
        actualizarVistaListaProcesos();
    }

    /**
     * Abre el cuadro de diálogo para asignar tiempos de llegada y prioridades a los programas.
     */
    private void asignarTiemposLlegada() {
        ArrayList<PendingProgram> programas = arrivalQueue.getPrograms();

        if (programas.isEmpty()) {
            vista.mostrarError("No hay programas seleccionados. Seleccione archivos .asm primero.");
            return;
        }

        List<Object[]> filas = new ArrayList<>();
        for (PendingProgram p : programas) {
            filas.add(new Object[]{ p.getName(), p.getRafaga(), p.getArrivalTime(), p.getPriority() });
        }

        List<Object[]> resultado = vista.mostrarDialogoTiemposLlegada(filas);
        if (resultado == null) return; // canceló

        // aplicar cambios y dejar la cola en el mismo orden que la tabla
        ArrayList<PendingProgram> ordenados = new ArrayList<>();
        for (Object[] fila : resultado) {
            PendingProgram p = programas.get((int) fila[0]);
            p.setArrivalTime((int) fila[1]);
            p.setPriority((int) fila[2]);
            ordenados.add(p);
        }

        programas.clear();
        programas.addAll(ordenados);
    }

    private void asignarCPUs() {
        int numCPUs = SystemConfig.getNumCPUs();
        ArrayList<PendingProgram> programas = arrivalQueue.getPrograms();
        for (int i = 0; i < programas.size(); i++) {
            programas.get(i).setAssignedCpuId(i % numCPUs + 1);
        }
    }

    private void abrirTablaPrevia(Runnable accionDespues) {
        ArrayList<PendingProgram> programas = arrivalQueue.getPrograms();

        if (programas.isEmpty()) {
            vista.mostrarError("No hay programas seleccionados. Seleccione archivos .asm primero.");
            return;
        }

        while (true) {
            asignarCPUs(); // por si cambió el orden o la cantidad de CPUs

            List<Object[]> filas = new ArrayList<>();
            for (PendingProgram p : programas) {
                filas.add(new Object[]{ p.getName(), p.getRafaga(), p.getArrivalTime(), p.getPriority(), p.getAssignedCpuId() });
            }

            int opcion = vista.mostrarTablaPrevia(filas);

            if (opcion == VentanaPrincipal.PREVIA_INICIAR) {
                simulationStarted = true;
                this.startTimeSimulation = LocalDateTime.now();
                accionDespues.run();
                return;
            }

            if (opcion == VentanaPrincipal.PREVIA_CAMBIAR) {
                asignarTiemposLlegada();
                continue;              // vuelve a mostrar la previa con los datos nuevos
            }

            return; // cerró con la X
        }
    }

    /**
     * Esto carga el programa en la memoria principal (RAM) y actualiza la vista de la memoria.
     * Estado del proceso READY
     */
    private void cargarPrograma() {
        /* 
        if (!this.scheduler.hasPendingJobs()) {
            vista.mostrarError("No hay un programa cargado. Seleccione un archivo .asm primero.");
            //finalizarSimulacion(); // PENDIENTE de ver si es aqui
            return;
        }
        */
        //this.startTimeSimulation = LocalDateTime.now();
        
        /*
        if (this.process.getPCB().getState() != Process.ProcessState.NEW) {
            vista.mostrarError("El programa ya ha sido cargado en memoria. No se puede cargar nuevamente.");
            return;
        }
             */

        //Loader.loadToMemory(this.process, this.memory, this.disk);

        //this.process.getPCB().setState(Process.ProcessState.READY);
        this.scheduler.checkAdmitJob();
        //this.process = this.processList.getFirstProcess();
        //vista.setEstadoBCP("READY");
        actualizarVistaListaProcesos();

        List<Object[]> memoryRows = this.memory.getAllMemoryRows();
        vista.actualizarTablaMemoria(memoryRows, -1);

        ArrayList<List<Object[]>> diskLists = this.disk.getAllDiskRows();
        vista.actualizarTablaDisco(diskLists.get(0));
        vista.actualizarTablaMemoriaVirtual(diskLists.get(1));
    }

    /**
     * Cuenta cuántas filas en la tabla pertenecen al área del Kernel 
     * (desde la posición 0 hasta getUserMemoryStart() - 1).
     */
    private int contarFilasKernel(List<Object[]> memoryRows) {
        int filasKernel = 0;
        int limiteKernel = SystemConfig.getUserMemoryStart();

        for (Object[] row : memoryRows) {
            if (row != null && row.length > 0) {
                if (row[0] != null && row[0].toString().contains("...")) {
                    filasKernel++;
                }
                else {
                    int pos = Integer.parseInt(row[0].toString().split("[^0-9]+")[0]);

                    if (pos >= limiteKernel) {
                        break;
                    } else {
                        filasKernel++;
                    }
                }
                
            }
        }
        return filasKernel;
    }

    public void terminateCurrentProcess(String motivo) {
        Process currentProcess = processList.getFirstProcess();
        if (currentProcess == null) return;

        System.out.println("Proceso " + currentProcess.getPCB().getPID() + " terminado. Motivo: " + motivo);

        // 1. Cambiar estado
        currentProcess.getPCB().setState(Process.ProcessState.EXIT);

        // 2. Limpiar estructuras (CPU, Memoria, Listas)
        cpu.setPCB(null);
        vista.getTxtPantalla().append("[PID " + currentProcess.getPCB().getPID() + "]: " + "Proceso terminado con error." + "\n");
        processList.removeProcess(currentProcess.getPCB().getPID());
        // memoria.liberarEspacio(currentProcess...);

        boolean haySuspendidosPendientes = this.scheduler.hasPendingSuspendedProcesses();

        if (!haySuspendidosPendientes) {
            this.scheduler.checkAdmitJob(); // Admitir automáticamente el siguiente Job en espera si cabe en la RAM liberada
        } else {
            System.out.println("No se admiten nuevos Jobs porque hay procesos en READY_SUSPENDED esperando memoria RAM.");
        } 
    }

    /**
     * Esto valida que el proceso esté en un estado válido para ejecutar (READY o RUNNING).
     * Si el proceso está en estado EXIT, NEW o BLOCKED, se muestra un mensaje de error y no se permite la ejecución.
     * @return true si el proceso está en un estado válido para ejecutar, false de lo contrario.
     */
    private boolean validarParaEjecutar() {
        // no se ha cargado nada todavía
        if (!scheduler.hasPendingJobs() && !processList.hasProcesses() && todosLosCPUsLibres() && !arrivalQueue.hasPending()) {
            vista.mostrarError("No hay programas cargados. Seleccione archivos .asm primero.");
            return false;
        }

        // ya todo terminó
        if (simulacionTerminada()) {
            actualizarVistaCPU();
            finalizarSimulacion();
            return false;
        }

        return true;
    }

    /**
     * Verifica si la simulación ha terminado.
     * @return true si no hay trabajos pendientes y todos los procesos están en EXIT
     */
    private boolean simulacionTerminada() {
        if (scheduler.hasPendingJobs() || arrivalQueue.hasPending()) return false;

        for (Process p : processList.getProcesses()) {
            if (p.getState() != ProcessState.EXIT) {
                return false; // alguien sigue vivo (listo, bloqueado, suspendido o ejecutando)
            }
        }

        return todosLosCPUsLibres();
    }

    // DESPUES CAMBIAR CON DISTINTOS COLORES POR CPU
    private void actualizarVistas() {
        actualizarVistaListaProcesos();
        actualizarVistaCPU();

        List<Object[]> memoryRows = memory.getAllMemoryRows();
        int rowsKernel = contarFilasKernel(memoryRows);

        // resaltar la instrucción actual de cada CPU ocupado
        int[] filas = new int[cpus.size()];
        for (int i = 0; i < cpus.size(); i++) {
            CPU c = cpus.get(i);
            filas[i] = (c.getPCB() == null) ? -1 : rowsKernel + (c.getCurrentInstructionAddress() - SystemConfig.getUserMemoryStart());
        }
        vista.actualizarTablaMemoria(memoryRows, filas);

        ArrayList<List<Object[]>> diskLists = disk.getAllDiskRows();
        vista.actualizarTablaDisco(diskLists.get(0));
        vista.actualizarTablaMemoriaVirtual(diskLists.get(1));
    }

    /**
     * Termina el proceso que está en un CPU  y deja el CPU libre.
     * @param aCpu El CPU donde falló el proceso
     * @param motivo Mensaje del error
     */
    private void terminarProcesoDeCPU(CPU aCpu, String motivo) {
        PCB pcb = aCpu.getPCB();
        if (pcb == null) return;

        int pid = pcb.getPID();
        System.out.println("Proceso " + pid + " terminado. Motivo: " + motivo);

        pcb.setState(ProcessState.EXIT);

        vista.getTxtPantalla().append("[PID " + pid + "]: " + motivo + "\n");
        vista.getTxtPantalla().append("[PID " + pid + "]: Proceso terminado con error.\n");

        processList.removeProcess(pid);
        aCpu.setPCB(null);

        boolean haySuspendidosPendientes = scheduler.hasPendingSuspendedProcesses();
        if (!haySuspendidosPendientes) {
            scheduler.checkAdmitJob();
        } else {
            System.out.println("No se admiten nuevos Jobs porque hay procesos en READY_SUSPENDED esperando memoria RAM.");
        }
    }

    // Saca el proceso de la CPU y lo devuelve a la cola de listos
    private void expulsarDeCPU(CPU aCpu, Process p) {
        aCpu.getPCB().setState(ProcessState.READY); // antes del saveContext para que la RAM muestre READY
        Dispatcher.saveContext(p, aCpu, memory);
        aCpu.setPCB(null);
        scheduler.addToReadyList(p);
    }

    private void aplicarSelfishAlScheduler() {
        scheduler.setSelfish(selfishActivo, selfishA, selfishB);
    }

    public void aceptarProgramasNuevos() {
        List<PendingProgram> arrivedPrograms = arrivalQueue.getArrivedPrograms(systemClock.getTicks());
        for (PendingProgram p : arrivedPrograms) {
            procesarArchivo(p);
        }
    }

    /**
     * Verifica si todos los CPUs están libres
     * @return true si todos los CPUs están libres
     */
    private boolean todosLosCPUsLibres() {
        for (CPU c : cpus) {
            if (c.getPCB() != null && c.getPCB().getState() != ProcessState.EXIT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Esto ejecuta el programa paso a paso, actualizando la vista de la memoria y el estado del proceso.
     * Estado del proceso RUNNING
     */
    private void ejecutarPasoAPaso() {
        if (!validarParaEjecutar()) {
            return;
        }
        System.out.println("Ejecución automática iniciada.");

        for (Process p : processList.getProcesses()) {
            if (p.getState() == ProcessState.BLOCKED) {
                p.getPCB().setState(ProcessState.READY); // esto es porque no hay input entonces para evitar pegarse en blocked
                scheduler.addToReadyList(p);
            }
        }

        tick();
        aceptarProgramasNuevos();
        cargarPrograma();
        
        // despachar si está libre y ejecutar
        for (int i = 0; i < cpus.size(); i++) {
            CPU aCpu = cpus.get(i);

            // Expulsión: solo si el algoritmo es expulsivo, hay un proceso corriendo y alguien espera
            ArrayList<Process> ready = scheduler.getReadyList(i);
            if (aCpu.getPCB() != null) {
                int quantum = algorithm.getQuantum();
                if (quantum > 0 && aCpu.getQuantumCounter() >= quantum) {
                    Process enCPU = processList.getProcessByPID(aCpu.getPCB().getPID());
                    if (enCPU != null && !ready.isEmpty()) {
                        algorithm.onQuantumExpired(enCPU); // solo baja de cola si alguien más espera
                        expulsarDeCPU(aCpu, enCPU);
                    } else {
                        aCpu.resetQuantumCounter(); // está solo: sigue con un quantum nuevo
                    }
                }
            }

            if (aCpu.getPCB() != null && algorithm.isPreemptive() && !ready.isEmpty()) {
                Process running = processList.getProcessByPID(aCpu.getPCB().getPID());

                if (running != null) {
                    ArrayList<Process> pool = new ArrayList<>(ready);
                    pool.add(0, running); // el que corre también compite

                    Process best = algorithm.selectNext(pool, systemClock.getTicks());
                    if (best != running) {
                        expulsarDeCPU(aCpu, running); // la CPU queda libre
                    }
                }
            }

            if (aCpu.getPCB() == null) {
                scheduler.admitIfNoAccepted(i);
                Process nextProcess = Dispatcher.selectNext(
                        scheduler.getReadyList(i), algorithm, systemClock.getTicks());
                if (nextProcess == null) continue; // ocioso
                Dispatcher.dispatch(nextProcess, aCpu);
                aCpu.resetQuantumCounter(); // reinicia el contador de quantum al despachar un proceso
            }

            int pid = aCpu.getPCB().getPID();
            Process actual = processList.getProcessByPID(pid);
            String archivo = (actual != null) ? actual.getNameFile() : "";
            vista.registrarEjecucion(i + 1, pid, archivo, systemClock.getTicks() + 1);

            try {
                aCpu.executeInstruction();
            } catch (RuntimeException e) {
                terminarProcesoDeCPU(aCpu, e.getMessage());
                continue;
            }

            if (actual != null) {
                actual.decrementBurstTime();
                actual.addCpuTime(1);
            }

            if (aCpu.getPCB() == null) continue;

            ProcessState status = aCpu.getPCB().getState();
            Process corriendo = (status == ProcessState.EXIT || status == ProcessState.BLOCKED) ? null : actual;
            scheduler.updateSelfish(i, corriendo);
            if (status == ProcessState.EXIT) {
                processList.removeProcess(aCpu.getPCB().getPID());
                aCpu.setPCB(null);
            } else if (status == ProcessState.BLOCKED) {
                aCpu.setPCB(null);
            } else {
                aCpu.incrementQuantumCounter();
                Dispatcher.saveContext(null, aCpu, memory);
            }
        }

        // output
        if (interruptHandler.hasPendingOutput()) {
            vista.getTxtPantalla().append(interruptHandler.clearBufferOutput());
        }

        actualizarVistas();
        
        if (simulacionTerminada()) {
            finalizarSimulacion();
        }
    }

    /**
     * Esto ejecuta todo el programa de una vez, actualizando la vista de la memoria y el estado del proceso.
     * Utiliza un bucle llamando a ejecutarPasoAPaso() hasta que el proceso termine (estado EXIT).
     * Si el proceso ya está en estado EXIT, se muestra un mensaje de error y no se permite la continuar la ejecución.
     * Estado del proceso RUNNING
     */
    private void ejecutarTodoPrograma() {
        if (!validarParaEjecutar()) {
            return;
        }

        this.autoExecute = true;
        vista.setModoAutomatico(true);
        
        new Thread(() -> {
            while (autoExecute) {

                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (autoExecute) {
                        ejecutarPasoAPaso();
                    }
                });

                try {
                    Thread.sleep(1000); // 1 segundo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start(); 
    }

    /**
     * Actualiza la vista del CPU con la información del proceso actual.
     */
    private void actualizarVistaCPU() {
        for (int i = 0; i < cpus.size(); i++) {
            int numCPU = i + 1;
            CPU aCpu = cpus.get(i);

            if (aCpu.getPCB() == null) {
                vista.setEstadoBCP(numCPU, "OCIOSO");
                continue;
            }

            vista.setProcessID(numCPU, aCpu.getPCB().getPID());
            vista.setEstadoBCP(numCPU, aCpu.getPCB().getState().toString());
            vista.setPC(numCPU, aCpu.getPC());
            vista.setIR(numCPU, aCpu.getIR());
            vista.setAC(numCPU, aCpu.getAC());
            vista.setAX(numCPU, aCpu.getAX());
            vista.setBX(numCPU, aCpu.getBX());
            vista.setCX(numCPU, aCpu.getCX());
            vista.setDX(numCPU, aCpu.getDX());
        }
    }


    // Utils

    /**
     * Aplica las configuraciones seleccionadas por el usuario.
     */
    private boolean aplicarConfiguraciones() {
        System.out.println("CONFIGURANDO...");
        int memorySize = vista.getTamanoMemoriaSeleccionado();
        int kernelSize = vista.getLimiteKernelSeleccionado();
        int diskSize = vista.getTamanoDiscoSeleccionado();
        int virtualSize = vista.getTamanoVirtualSeleccionado();
        int numCPUs = vista.getCantidadCPUSeleccionada();

        // Calcular límites usando la nueva configuración
        int minKernelSize = (int) Math.ceil(
            memorySize * SystemConfig.MIN_KERNEL_MEMORY_PERCENTAGE
        );

        int maxKernelSize = (int) Math.floor(
            memorySize * SystemConfig.MAX_KERNEL_MEMORY_PERCENTAGE
        );

        int minVirtualSize = (int) Math.ceil(
            diskSize * SystemConfig.MIN_VIRTUAL_MEMORY_PERCENTAGE
        );

        int maxVirtualSize = (int) Math.floor(
            diskSize * SystemConfig.MAX_VIRTUAL_MEMORY_PERCENTAGE
        );

        if (memorySize < SystemConfig.MEMORY_SIZE_MIN
                || memorySize > SystemConfig.MEMORY_SIZE_MAX
                || kernelSize < minKernelSize
                || kernelSize > maxKernelSize
                || kernelSize >= memorySize
                || diskSize < SystemConfig.DISK_SIZE_MIN
                || diskSize > SystemConfig.DISK_SIZE_MAX
                || virtualSize < minVirtualSize
                || virtualSize > maxVirtualSize
                || numCPUs < SystemConfig.NUM_CPUS_MIN
                || numCPUs > SystemConfig.NUM_CPUS_MAX) {

            vista.mostrarError(
                "Rango de configuración inválido."
            );
            return false;
        }

        // validaciones de la asignación de memoria (antes de aplicar nada)
        String estrategiaElegida = vista.getEstrategiaMemoriaSeleccionada();
        int kernelPar = (kernelSize % 2 != 0) ? kernelSize + 1 : kernelSize; // SystemConfig redondea el kernel a par
        int userMemorySize = memorySize - kernelPar;

        if (estrategiaElegida.equals("Partición fija (iguales)")) {
            int tamanoParticion = vista.getTamanoParticionSeleccionado();
            if (tamanoParticion > userMemorySize) {
                vista.mostrarError("El tamaño de partición (" + tamanoParticion + ") no puede ser mayor que la memoria de usuario (" + userMemorySize + " celdas).");
                return false;
            }
        }

        if (estrategiaElegida.equals("Partición fija (diferentes)")) {
            int[] lista = vista.getParticionesSeleccionadas();
            if (lista == null) {
                vista.mostrarError("La lista de particiones no es válida. Use números enteros mayores a 0 separados por comas, por ejemplo 16,32,64.");
                return false;
            }
            int suma = 0;
            for (int t : lista) {
                suma += t;
            }
            if (suma > userMemorySize) {
                vista.mostrarError("Las particiones suman " + suma + ", más que la memoria de usuario (" + userMemorySize + " celdas).");
                return false;
            }
        }

        // Selfish Round Robin: a debe ser mayor o igual que b
        if ("Selfish Round Robin".equals(vista.getAlgoritmoCPUSeleccionado())) {
            int a = vista.getSelfishASeleccionado();
            int b = vista.getSelfishBSeleccionado();

            if (a < b) {
                vista.mostrarError("En Selfish Round Robin, a (" + a + ") no puede ser menor que b (" + b + ").");
                return false;
            }
        }
        

        SystemConfig.setMemorySize(memorySize);
        SystemConfig.setUserMemoryStart(kernelSize);
        SystemConfig.setDiskSize(diskSize);
        SystemConfig.setDiskMemoryVirtualSize(virtualSize);
        SystemConfig.setNumCPUs(numCPUs);

        SystemConfig.setMemoryStrategyAssignment(vista.getEstrategiaMemoriaSeleccionada());
        SystemConfig.setMemoryFit(vista.getAjusteMemoriaSeleccionado());
        SystemConfig.setPartitionFixedEqualSize(vista.getTamanoParticionSeleccionado());
        SystemConfig.setPagingSize(vista.getTamanoPaginaSeleccionado());

        if (estrategiaElegida.equals("Partición fija (diferentes)")) {
            SystemConfig.setPartitionFixedDifferentSize(vista.getParticionesSeleccionadas()); // solo si es válida
        }

        String estrategia = SystemConfig.getMemoryStrategyAssignment();
        if (estrategia.equals("fixed_equal")) {
            vista.actualizarColoresParticionIguales(vista.getTamanoParticionSeleccionado());
        } else if (estrategia.equals("paging")) {
            vista.actualizarColoresParticionIguales(vista.getTamanoPaginaSeleccionado());
        } else if (estrategia.equals("fixed_different")) {
            vista.actualizarColoresParticionDiferentes(vista.getParticionesSeleccionadas());
        } else {
            vista.actualizarColoresParticion(null, null); // dinámica o paginación: sin colores
        }

        this.memory = null; //sacamos memoria vieja
        this.cpu = null; //sacamos cpu vieja
        this.disk = null; //sacamos disco viejo
        this.fileSystem = null; //sacamos file system viejo

        this.memory = new MainMemory(); // ponemos memoria nueva
        this.disk = new Disk(); // ponemos disco nuevo

        this.fileSystem = new FileSystem(this.disk);

        this.jobList = new JobList();
        this.processList = new ProcessList(this.memory);
        this.scheduler = new Scheduler(this.jobList, this.processList, this.memory, this.disk, this.systemClock, SystemConfig.getNumCPUs());
        this.interruptHandler = new InterruptHandler(this.memory, this.processList, this.scheduler, this.fileSystem);

        this.cpus = new ArrayList<>(); // ponemos cpus nuevos
        for (int i = 0; i < SystemConfig.getNumCPUs(); i++) {
            this.cpus.add(new CPU(this.memory, this.interruptHandler));
        }
         
        int cantidad = vista.getCantidadCPUSeleccionada();
        vista.configurarCantidadCPU(cantidad);

        this.algorithm = AlgorithmFactory.create(
            vista.getAlgoritmoCPUSeleccionado(), vista.getQuantumSeleccionado(), vista.getColasMLFQSeleccionadas()
        );
        System.out.println("Algoritmo: " + vista.getAlgoritmoCPUSeleccionado()
            + " | quantum: " + algorithm.getQuantum());

        selfishActivo = "Selfish Round Robin".equals(vista.getAlgoritmoCPUSeleccionado());
        selfishA = vista.getSelfishASeleccionado();
        selfishB = vista.getSelfishBSeleccionado();
        aplicarSelfishAlScheduler();

        this.cpu = this.cpus.get(0);

        this.arrivalQueue = new ArrivalQueue();
        this.simulationStarted = false;

        Loader.resetNextFitIndex();

        System.out.println("Configuraciones aplicadas correctamente.");
        vista.mostrarInfo("Configuraciones aplicadas correctamente.");
        return true;
    }

    /**
     * Reinicia el sistema, limpiando la memoria y el CPU, y actualizando la vista.
     */
    private void reiniciarSistema() {
        systemClock.reset();
        this.autoExecute = false;
        this.simulationStarted = false;
        this.memory = null; //sacamos memoria vieja
        this.disk = null; //sacamos disco viejo
        this.cpu = null; //sacamos cpu vieja
        this.fileSystem = null; //sacamos file system viejo

        this.memory = new MainMemory(); // ponemos memoria nueva

        this.disk = new Disk(); // ponemos disco nuevo

        this.fileSystem = new FileSystem(this.disk); // ponemos file system nuevo
        // reescribimos las listas
        this.jobList = new JobList();
        this.processList = new ProcessList(this.memory);

        this.scheduler = new Scheduler(this.jobList, this.processList, this.memory, this.disk, this.systemClock, SystemConfig.getNumCPUs());
        aplicarSelfishAlScheduler();
        this.interruptHandler = new InterruptHandler(this.memory, this.processList, this.scheduler, this.fileSystem);

        this.cpus = new ArrayList<>(); // ponemos cpus nuevos
        for (int i = 0; i < SystemConfig.getNumCPUs(); i++) {
            this.cpus.add(new CPU(this.memory, this.interruptHandler));
        }

        this.cpu = this.cpus.get(0);

        this.arrivalQueue = new ArrivalQueue();

        CPU.resetIDs();
        Job.resetPIDs();
        Loader.resetNextFitIndex();
        vista.limpiarVista();
        vista.getDialogConfig().dispose();
        vista.habilitarConfiguraciones();

        System.out.println("Sistema reiniciado correctamente.");
        vista.mostrarInfo("Sistema reiniciado correctamente.");
    }

    /**
     * Actualiza la vista con la lista de procesos y trabajos.
     */
    private void actualizarVistaListaProcesos() {
        List<Object[]> tableDataJob = this.jobList.getTableData();
        List<Object[]> tableDataProcess = this.processList.getTableData();
        List<Object[]> tableDataFinal = new ArrayList<>();
        tableDataFinal.addAll(tableDataProcess);
        tableDataFinal.addAll(tableDataJob);

        vista.actualizarTablaTrabajos(tableDataFinal);
    }

    /**
     * Finaliza la simulación, deteniendo la ejecución y mostrando las estadísticas de los procesos ejecutados.
     */
    private void finalizarSimulacion() {
        this.autoExecute = false;
        List<Object[]> datosEstadisticas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        int ticksAcumulados = 0;

        for (Process p : this.processList.getDeletedProcesses()) {
            PCB pcb = p.getPCB();
            String pid = "PID " + pcb.getPID();

            int tickInicio = p.getArrivalTime();
            int tickFin = pcb.getTimeEnd() + 1;
            int ticksProceso = pcb.getTimeSpent();

            // cada tick es 1 segundo desde el inicio de la simulación
            LocalDateTime horaInicioDateT = this.startTimeSimulation.plusSeconds(tickInicio);
            LocalDateTime horaFinDateT = this.startTimeSimulation.plusSeconds(tickFin);

            String horaInicio = horaInicioDateT.format(formatter);
            String horaFin = horaFinDateT.format(formatter);

            int turnaround = tickFin - tickInicio;

            int tiempoOcioso = turnaround - ticksProceso;

            double trTs = (ticksProceso > 0) ? (double) turnaround / ticksProceso : 0;

            double duracionSegundos = (double) turnaround;

            datosEstadisticas.add(new Object[]{pid, horaInicio, horaFin, String.format("%.2f s", duracionSegundos), tickInicio, tickFin, tiempoOcioso, turnaround, String.format("%.2f", trTs)});
        }
        vista.mostrarVentanaEstadisticas(datosEstadisticas);
    }
}