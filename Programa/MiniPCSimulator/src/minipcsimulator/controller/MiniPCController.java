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
    private SystemClock systemClock;
    private JobList jobList;
    private ProcessList processList;
    private Scheduler scheduler;
    private InterruptHandler interruptHandler;
    private FileSystem fileSystem;
    
    //hora de inicio
    private LocalDateTime startTimeSimulation;

    //tiempos de input
    private LocalDateTime startTimeInput;

    private boolean autoExecute = false;

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
        this.scheduler = new Scheduler(this.jobList, this.processList, this.memory, this.disk, this.systemClock);
        this.fileSystem = new FileSystem(this.disk);
        this.interruptHandler = new InterruptHandler(this.memory, this.processList, this.scheduler, this.fileSystem);
        this.cpu = new CPU(this.memory, interruptHandler);

        agregarListeners();

        // Muestra GUI
        this.vista.setVisible(true);
        abrirConfiguracion();
    }

    private void tick() {
        systemClock.tick();
        vista.setTicks(systemClock.getTicks());
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
                aplicarConfiguraciones();
                if (vista.getDialogConfig() != null) {
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

        // Botón para cargar el programa en memoria RAM
        vista.getBtnCargar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarPrograma();
            }
        });

        // Botón para ejecutar un paso del programa (modo paso a paso)
        vista.getBtnPasoAPaso().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarPasoAPaso();
            }
        });

        // Botón para ejecutar todo el programa de una vez
        vista.getBtnEjecutar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
    }

    /**
     * Esto procesa un archivo .asm cargado, verificando su sintaxis y generando un Job y un proceso en estado NEW.
     * @param fileData Un Map.Entry que contiene la ruta y nombre del archivo, y las líneas de código del archivo.
     * El primer elemento (key) es un ArrayList con la ruta y el nombre del archivo, y el segundo elemento (value) es un ArrayList con las líneas de código.
     */
    private void procesarArchivo(Map.Entry<ArrayList<String>, ArrayList<String>> fileData) {
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
        
        ArrayList<ArrayList<String>> asmArray = new ArrayList<>();
        
        try {
            asmArray = AsmParser.verifySyntax(lines);
        } catch (Exception exc) {
            vista.mostrarError(exc.getMessage());
            return;
        }

        if (asmArray.isEmpty()) {
            vista.mostrarError("El archivo + " + fileName + " está vacío o no contiene instrucciones válidas.");
            return;
        }

        if (asmArray.size() > SystemConfig.getUserMemorySize()) {
            vista.mostrarError("El programa " + fileName + " ingresado es demasiado grande para la memoria disponible. Tamaño máximo permitido: " + SystemConfig.getUserMemorySize() + " instrucciones.");
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

        Job job = new Job(filePath, fileName, lines, asmArray);
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

        for (Map.Entry<ArrayList<String>, ArrayList<String>> fileData : filesData) {
            procesarArchivo(fileData);
        }
    }

    /**
     * Esto carga el programa en la memoria principal (RAM) y actualiza la vista de la memoria.
     * Estado del proceso READY
     */
    private void cargarPrograma() {
        if (!this.scheduler.hasPendingJobs()) {
            vista.mostrarError("No hay un programa cargado. Seleccione un archivo .asm primero.");
            //finalizarSimulacion(); // PENDIENTE de ver si es aqui
            return;
        }

        this.startTimeSimulation = LocalDateTime.now();
        
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
        vista.setEstadoBCP("READY");
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
        if (this.cpu.getPCB() == null && !this.processList.hasProcesses()) {
            //vista.mostrarError("No hay un programa cargado. Seleccione un archivo .asm primero.");
            actualizarVistaCPU();
            finalizarSimulacion(); // PENDIENTE de ver si es aqui
            return false;
        }
        if (this.cpu.getPCB() != null) {
            if (this.cpu.getPCB().getState() == ProcessState.EXIT && !this.processList.hasProcesses()) {
                vista.setEstadoBCP("EXIT");
                vista.mostrarError("El proceso ya ha terminado. Seleccione un nuevo archivo .asm para cargar otro programa.");
                return false;
            }
            else if (this.cpu.getPCB().getState() == ProcessState.NEW) {
                vista.mostrarError("El proceso aún no ha sido cargado en memoria. Cargue el programa primero.");
                return false;
            }
            else if (this.cpu.getPCB().getState() == ProcessState.BLOCKED && this.interruptHandler.hasPendingInput()) {
                vista.mostrarError("El proceso está bloqueado. No se puede ejecutar hasta que se desbloquee.");
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

        tick();

        // Si el proceso es válido, vamos a ejecutarlo.

        Process currentProcess = this.processList.getFirstProcess();


        if (currentProcess == null) {
            //vista.mostrarError("No hay un programa cargado. Seleccione un archivo .asm primero.");
            actualizarVistaCPU();
            finalizarSimulacion();
            return;
        }

        // Para ejecutar primer paso se debe llamar al dispatcher
        if (currentProcess.getState() == ProcessState.READY) {
            Dispatcher.dispatch(currentProcess, this.cpu);
            vista.setEstadoBCP("RUNNING");
            actualizarVistaListaProcesos();
        }

        // Ejecutar la instrucción actual
        try {
            this.cpu.executeInstruction();
        } catch (RuntimeException e) {
            // Manejar la excepción y terminar el proceso
            System.out.println("Excepción durante la ejecución: " + e.getMessage());
            vista.getTxtPantalla().append("[PID " + currentProcess.getPCB().getPID() + "]: " + e.getMessage() + "\n");
            terminateCurrentProcess(e.getMessage());
            actualizarVistaListaProcesos();
            actualizarVistaCPU();
            ArrayList<List<Object[]>> diskLists = this.disk.getAllDiskRows();
            vista.actualizarTablaMemoria(this.memory.getAllMemoryRows(), -1);
            vista.actualizarTablaDisco(diskLists.get(0));
            vista.actualizarTablaMemoriaVirtual(diskLists.get(1));
            return;
        }

        // Input
        /* 
        if (currentProcess.getState() == ProcessState.BLOCKED && this.interruptHandler.hasPendingInput()) {
            this.startTimeInput = LocalDateTime.now();
            actualizarVistaListaProcesos();
            actualizarVistaCPU();
            //vista.getTxtTeclado().setEnabled(true);
            //vista.getTxtTeclado().requestFocusInWindow();
            return;
        }
        */

        // Output
        if (this.interruptHandler.hasPendingOutput()) {
            vista.getTxtPantalla().append(this.interruptHandler.clearBufferOutput());
        }

        
        if (this.cpu.getPCB() == null || this.cpu.getPCB().getState() == ProcessState.EXIT) {

            boolean haySuspendidosPendientes = this.scheduler.hasPendingSuspendedProcesses();

            if (!haySuspendidosPendientes) {
                this.scheduler.checkAdmitJob(); // Admitir automáticamente el siguiente Job en espera si cabe en la RAM liberada
            } else {
                System.out.println("No se admiten nuevos Jobs porque hay procesos en READY_SUSPENDED esperando memoria RAM.");
            } 

            actualizarVistaListaProcesos();

            // Actualizar vistas
            ArrayList<List<Object[]>> diskLists = this.disk.getAllDiskRows();
            actualizarVistaListaProcesos();
            vista.actualizarTablaMemoria(this.memory.getAllMemoryRows(), -1);
            vista.actualizarTablaDisco(diskLists.get(0));
            vista.actualizarTablaMemoriaVirtual(diskLists.get(1));
            actualizarVistaCPU();

            validarParaEjecutar();
        }
        else {
            Dispatcher.saveContext(currentProcess, this.cpu, this.memory); // PENDIENTE, NO DEBERIA PERO PREGUNTAR A PROFE
            actualizarVistaCPU();
            List<Object[]> memoryRows = this.memory.getAllMemoryRows();

            int actualAddress = this.cpu.getCurrentInstructionAddress();
            int rowsKernel = contarFilasKernel(memoryRows);

            int selectedRow = rowsKernel + (actualAddress - SystemConfig.getUserMemoryStart());
            
            vista.actualizarTablaMemoria(memoryRows, selectedRow);

            vista.actualizarTablaDisco(this.disk.getAllDiskRows().get(0));
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

        new Thread(() -> {
            boolean running = true;

            while (running) {
                Process processCheck = this.processList.getFirstProcess();
                if (processCheck != null && processCheck.getState() == ProcessState.BLOCKED && this.interruptHandler.hasPendingInput()) {
                    System.out.println("Ejecución automática pausada: Esperando entrada de teclado.");
                    break; //sale del hilo a esperar input
                }

                javax.swing.SwingUtilities.invokeLater(() -> ejecutarPasoAPaso()); //pendiebte de antes o despues de sleep

                try {
                    Thread.sleep(1000); // 1 segundo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                Process current = this.processList.getFirstProcess();
                if (current != null) {
                    // pausar si cae en bloqueado y hay input pendiente
                    if (current.getState() == ProcessState.BLOCKED && this.interruptHandler.hasPendingInput()) {
                        System.out.println("Proceso bloqueado por input. Hilo automático pausado.");
                        running = false;
                    }
                } else if (!this.scheduler.hasPendingJobs()) {
                    running = false;
                }
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                Process activeProc = this.processList.getFirstProcess();
                if (activeProc != null && activeProc.getState() == ProcessState.EXIT) {
                    vista.setEstadoBCP("EXIT");
                }
            });
        }).start(); 
    }

    /**
     * Actualiza la vista del CPU con la información del proceso actual.
     */
    private void actualizarVistaCPU() {
        if (this.cpu.getPCB() != null) {
            vista.setProcessID(this.cpu.getPCB().getPID());
            vista.setEstadoBCP(this.cpu.getPCB().getState().toString());
            vista.setPC(this.cpu.getPC());
            vista.setIR(this.cpu.getIR());
            vista.setAC(this.cpu.getAC());
            vista.setAX(this.cpu.getAX());
            vista.setBX(this.cpu.getBX());
            vista.setCX(this.cpu.getCX());
            vista.setDX(this.cpu.getDX());
        }
    }


    // Utils

    /**
     * Aplica las configuraciones seleccionadas por el usuario.
     */
    private void aplicarConfiguraciones() {
        System.out.println("CONFIGURANDO...");
        int memorySize = vista.getTamanoMemoriaSeleccionado();
        int kernelSize = vista.getLimiteKernelSeleccionado();
        int diskSize = vista.getTamanoDiscoSeleccionado();
        int virtualSize = vista.getTamanoVirtualSeleccionado();

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
                || virtualSize > maxVirtualSize) {

            vista.mostrarError(
                "Rango de configuración inválido."
            );
            return;
        }

        SystemConfig.setMemorySize(memorySize);
        SystemConfig.setUserMemoryStart(kernelSize);
        SystemConfig.setDiskSize(diskSize);
        SystemConfig.setDiskMemoryVirtualSize(virtualSize);

        this.memory = null; //sacamos memoria vieja
        this.cpu = null; //sacamos cpu vieja
        this.disk = null; //sacamos disco viejo
        this.fileSystem = null; //sacamos file system viejo

        this.memory = new MainMemory(); // ponemos memoria nueva
        this.disk = new Disk(); // ponemos disco nuevo

        this.fileSystem = new FileSystem(this.disk);

        this.jobList = new JobList();
        this.processList = new ProcessList(this.memory);
        this.scheduler = new Scheduler(this.jobList, this.processList, this.memory, this.disk, this.systemClock);
        this.interruptHandler = new InterruptHandler(this.memory, this.processList, this.scheduler, this.fileSystem);

        this.cpu = new CPU(this.memory, this.interruptHandler); // ponemos cpu nueva

        System.out.println("Configuraciones aplicadas correctamente.");
        vista.mostrarInfo("Configuraciones aplicadas correctamente.");
    }

    /**
     * Reinicia el sistema, limpiando la memoria y el CPU, y actualizando la vista.
     */
    private void reiniciarSistema() {
        systemClock.reset();
        this.autoExecute = false;
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

        this.scheduler = new Scheduler(this.jobList, this.processList, this.memory, this.disk, this.systemClock);
        this.interruptHandler = new InterruptHandler(this.memory, this.processList, this.scheduler, this.fileSystem);

        this.cpu = new CPU(this.memory, this.interruptHandler); // ponemos cpu nueva

        Job.resetPIDs();
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

            LocalDateTime horaInicioDateT = this.startTimeSimulation.plusSeconds(ticksAcumulados);
            
            int ticksProceso = pcb.getTimeSpent();
            ticksAcumulados += ticksProceso;

            LocalDateTime horaFinDateT = this.startTimeSimulation.plusSeconds(ticksAcumulados);

            String horaInicio = horaInicioDateT.format(formatter);
            String horaFin = horaFinDateT.format(formatter);
            double duracionSegundos = (double) ticksProceso;

            datosEstadisticas.add(new Object[]{pid, horaInicio, horaFin, String.format("%.2f s", duracionSegundos)});
        }

        vista.mostrarVentanaEstadisticas(datosEstadisticas);
    }
}
