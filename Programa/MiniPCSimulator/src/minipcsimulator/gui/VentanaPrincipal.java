package minipcsimulator.gui;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import minipcsimulator.utils.SystemConfig;

public class VentanaPrincipal extends JFrame {

    // Paleta de colores
    private static final Color BG_MAIN = new Color(18, 20, 24);
    private static final Color BG_PANEL = new Color(25, 28, 34);
    private static final Color BG_CARD = new Color(31, 35, 42);
    private static final Color BG_INPUT = new Color(22, 25, 30);

    private static final Color BORDER = new Color(55, 61, 70);
    private static final Color TEXT = new Color(235, 238, 242);
    private static final Color TEXT_SECONDARY = new Color(155, 163, 174);

    // Colores de botones oscurecidos
    private static final Color BLUE = new Color(30, 95, 165);
    private static final Color GREEN = new Color(30, 130, 80);
    private static final Color RED = new Color(160, 45, 55);
    private static final Color CYAN = new Color(35, 120, 135);
    private static final Color ORANGE = new Color(175, 105, 35);
    private static final Color GRAY = new Color(50, 55, 65);
    private static final Color PURPLE = new Color(110, 55, 150);

    // Colores para la tabla de ejecución
    private static final Color[] COLORES_PROCESOS = {BLUE, GREEN, ORANGE, PURPLE, CYAN, RED};

    public static final int MAX_CPUS = 5;

    public static final String[] ALGORITMOS_CPU = {
        "FCFS", "SRT", "SPN", "Round Robin", "HRRN", "Selfish Round Robin",
        "Lottery", "Por Prioridad", "Guaranteed Scheduling", "Multilevel Feedback Queue"
    };

    public static final String[] ESTRATEGIAS_MEMORIA = {
        "Paginación", "Partición fija (iguales)", "Partición fija (diferentes)", "Dinámica"
    };

    public static final String[] AJUSTES_DINAMICA = {"First Fit", "Next Fit", "Best Fit"};

    private static final Integer[] TAMANOS_PAGINA = {4, 8, 16, 32, 64};

    // Componentes de la interfaz
    private JButton btnSeleccionar, btnTiemposLlegada, btnCargar, btnPasoAPaso, btnEjecutar, btnPausar, btnLimpiar;
    private JButton btnEstadisticas, btnAbrirConfig, btnGuardarConfig;
    private JSpinner spTamanoMemoria, spLimiteKernel, spTamanoDisco, spTamanoVirtual;
    private JSpinner spCantidadCPU, spQuantum, spSelfishA, spSelfishB, spColasMLFQ, spTamanoParticion;
    private JComboBox<String> cbAlgoritmoCPU, cbEstrategiaMemoria, cbAjusteMemoria;
    private JComboBox<Integer> cbTamanoPagina;
    private JTextField txtParticiones;

    private JDialog dialogConfig;
    private JDialog dialogEstadisticas;

    // Tablas
    private JTable tablaMemoria, tablaDisco, tablaVirtual, tablaTrabajos, tablaEjecucion;
    private JTable tablaEjecucionFija;
    private DefaultTableModel modeloTablaMemoria, modeloTablaDisco, modeloTablaVirtual, modeloTablaTrabajos;
    private DefaultTableModel modeloTablaEjecucion;
    private ArrayList<String> filasEjecucion = new ArrayList<>(); // guarda "cpu|pid" por fila

    // Consola
    private JTextArea txtPantalla;

    // Registros CPU y BCP
    private JTabbedPane tabsCPU;
    private JPanel[] panelesCPU = new JPanel[MAX_CPUS];
    private JTextField[] txtPC = new JTextField[MAX_CPUS];
    private JTextField[] txtIR = new JTextField[MAX_CPUS];
    private JTextField[] txtAC = new JTextField[MAX_CPUS];
    private JTextField[] txtAX = new JTextField[MAX_CPUS];
    private JTextField[] txtBX = new JTextField[MAX_CPUS];
    private JTextField[] txtCX = new JTextField[MAX_CPUS];
    private JTextField[] txtDX = new JTextField[MAX_CPUS];
    private JLabel[] lblEstadoBCP = new JLabel[MAX_CPUS];
    private JLabel[] lblProcessID = new JLabel[MAX_CPUS];
    private JLabel lblTicks;

    // Estado del botón de configuración
    private boolean configuracionHabilitada = true;
    private boolean hayEstadisticas = false;
    private int ticksCount = 0;

    public VentanaPrincipal() {
        initComponents();
    }

    // Inicializa la ventana principal
    private void initComponents() {
        setTitle("Mini PC Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 780);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        JPanel contenedor = new JPanel(new BorderLayout(16, 16));
        contenedor.setBackground(BG_MAIN);
        contenedor.setBorder(new EmptyBorder(18, 18, 18, 18));

        contenedor.add(crearHeader(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(16, 0));
        contenido.setBackground(BG_MAIN);

        contenido.add(crearPanelControles(), BorderLayout.WEST);
        contenido.add(crearPanelMemoria(), BorderLayout.CENTER);
        contenido.add(crearPanelBCP(), BorderLayout.EAST);

        contenedor.add(contenido, BorderLayout.CENTER);
        add(contenedor, BorderLayout.CENTER);
    }

    // Crea el panel superior (Header)
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_MAIN);
        header.setPreferredSize(new Dimension(0, 58));

        JPanel tituloPanel = new JPanel();
        tituloPanel.setOpaque(false);
        tituloPanel.setLayout(new BoxLayout(tituloPanel, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("MINI PC SIMULATOR");
        titulo.setForeground(TEXT);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel subtitulo = new JLabel("Proyecto 2 - Principios de Sistemas Operativos");
        subtitulo.setForeground(TEXT_SECONDARY);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        tituloPanel.add(titulo);
        tituloPanel.add(Box.createVerticalStrut(2));
        tituloPanel.add(subtitulo);

        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        
        estadoPanel.setOpaque(false);

        lblTicks = new JLabel("TICKS: " + ticksCount);
        lblTicks.setForeground(TEXT_SECONDARY);
        lblTicks.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel estado = new JLabel("VERSIÓN 1.0");
        estado.setForeground(TEXT_SECONDARY);
        estado.setFont(new Font("SansSerif", Font.BOLD, 12));

        estadoPanel.add(lblTicks);
        estadoPanel.add(estado);

        header.add(tituloPanel, BorderLayout.WEST);
        header.add(estadoPanel, BorderLayout.EAST);

        return header;
    }

    // Crea el panel de controles izquierdo
    private JPanel crearPanelControles() {
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(BG_PANEL);
        panelPrincipal.setBorder(new EmptyBorder(16, 14, 16, 14));
        panelPrincipal.setPreferredSize(new Dimension(200, 0));

        JLabel titulo = crearTituloCentrado("CONTROLES");
        panelPrincipal.add(titulo);
        panelPrincipal.add(Box.createVerticalStrut(14));

        btnSeleccionar = crearBoton("Seleccionar .asm", BLUE);
        btnTiemposLlegada = crearBoton("Tiempos de llegada", BLUE);
        btnCargar = crearBoton("Cargar programas", ORANGE);
        btnPasoAPaso = crearBoton("Paso a paso", CYAN);
        btnEjecutar = crearBoton("Ejecutar todo", GREEN);
        btnPausar = crearBoton("Pausar", GRAY);
        btnPausar.setEnabled(false);
        btnLimpiar = crearBoton("Limpiar sistema", RED);

        panelPrincipal.add(btnSeleccionar);
        panelPrincipal.add(Box.createVerticalStrut(8));
        panelPrincipal.add(btnTiemposLlegada);
        panelPrincipal.add(Box.createVerticalStrut(8));
        panelPrincipal.add(btnCargar);
        panelPrincipal.add(Box.createVerticalStrut(8));
        panelPrincipal.add(btnPasoAPaso);
        panelPrincipal.add(Box.createVerticalStrut(8));
        panelPrincipal.add(btnEjecutar);
        panelPrincipal.add(Box.createVerticalStrut(8));
        panelPrincipal.add(btnPausar);
        panelPrincipal.add(Box.createVerticalStrut(8));
        panelPrincipal.add(btnLimpiar);

        panelPrincipal.add(Box.createVerticalStrut(18));

        JSeparator separador = new JSeparator();
        separador.setForeground(BORDER);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        panelPrincipal.add(separador);
        panelPrincipal.add(Box.createVerticalStrut(14));

        // Botón de Estadísticas (Deshabilitado por defecto hasta finalizar la simulación)
        btnEstadisticas = crearBoton("Estadísticas", PURPLE);
        btnEstadisticas.setEnabled(false);
        panelPrincipal.add(btnEstadisticas);

        panelPrincipal.add(Box.createVerticalStrut(8));

        // Botón para abrir el diálogo modal de configuración
        btnAbrirConfig = crearBoton("Configuración", GRAY);
        panelPrincipal.add(btnAbrirConfig);

        return panelPrincipal;
    }

    private void abrirDialogoConfiguracion() {
        dialogConfig = new JDialog(this, "Configuración de Sistema", true);
        dialogConfig.setSize(820, 500);
        dialogConfig.setResizable(false);
        dialogConfig.setLocationRelativeTo(this);
        dialogConfig.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panelModal = new JPanel(new BorderLayout(10, 10));
        panelModal.setBackground(BG_PANEL);
        panelModal.setBorder(new EmptyBorder(16, 16, 16, 16));

        spTamanoMemoria = new JSpinner(new SpinnerNumberModel(
            SystemConfig.getMemorySize(),
            SystemConfig.MEMORY_SIZE_MIN,
            SystemConfig.MEMORY_SIZE_MAX,
            16
        ));

        spLimiteKernel = new JSpinner(new SpinnerNumberModel(
            SystemConfig.getUserMemoryStart(),
            SystemConfig.getMinKernelSize(),
            SystemConfig.getMaxKernelSize(),
            8
        ));

        spTamanoDisco = new JSpinner(new SpinnerNumberModel(
            SystemConfig.getDiskSize(),
            SystemConfig.DISK_SIZE_MIN,
            SystemConfig.DISK_SIZE_MAX,
            32
        ));

        spTamanoVirtual = new JSpinner(new SpinnerNumberModel(
            SystemConfig.getDiskMemoryVirtualSize(),
            SystemConfig.getMinVirtualSize(),
            SystemConfig.getMaxVirtualSize(),
            16
        ));

        spTamanoMemoria.addChangeListener(e -> actualizarLimitesKernel());
        spTamanoDisco.addChangeListener(e -> actualizarLimitesMemoriaVirtual());

        spCantidadCPU = new JSpinner(new SpinnerNumberModel(1, 1, MAX_CPUS, 1));
        cbAlgoritmoCPU = new JComboBox<>(ALGORITMOS_CPU);
        spQuantum = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        spSelfishA = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        spSelfishB = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
        spColasMLFQ = new JSpinner(new SpinnerNumberModel(3, 2, 5, 1));

        cbAlgoritmoCPU.addActionListener(e -> actualizarParametrosCPU());

        cbEstrategiaMemoria = new JComboBox<>(ESTRATEGIAS_MEMORIA);
        cbEstrategiaMemoria.setSelectedItem("Dinámica");
        cbAjusteMemoria = new JComboBox<>(AJUSTES_DINAMICA);
        cbTamanoPagina = new JComboBox<>(TAMANOS_PAGINA);
        cbTamanoPagina.setSelectedItem(8);
        spTamanoParticion = new JSpinner(new SpinnerNumberModel(16, 4, 1024, 4));
        txtParticiones = new JTextField("8,16,32,64");

        cbEstrategiaMemoria.addActionListener(e -> actualizarParametrosMemoria());

        estilitarSpinner(spTamanoMemoria);
        estilitarSpinner(spLimiteKernel);
        estilitarSpinner(spTamanoDisco);
        estilitarSpinner(spTamanoVirtual);
        estilitarSpinner(spCantidadCPU);
        estilitarSpinner(cbAlgoritmoCPU);
        estilitarSpinner(spQuantum);
        estilitarSpinner(spSelfishA);
        estilitarSpinner(spSelfishB);
        estilitarSpinner(spColasMLFQ);
        estilitarSpinner(cbEstrategiaMemoria);
        estilitarSpinner(cbAjusteMemoria);
        estilitarSpinner(cbTamanoPagina);
        estilitarSpinner(spTamanoParticion);
        estilitarSpinner(txtParticiones);

        JPanel cardRAM = crearCardFormulario(
            new String[]{"Tamaño RAM", "Espacio Kernel", "Tamaño Disco", "Memoria Virtual"},
            new JComponent[]{spTamanoMemoria, spLimiteKernel, spTamanoDisco, spTamanoVirtual}
        );

        JPanel cardCPU = crearCardFormulario(
            new String[]{"Cantidad de CPU", "Algoritmo CPU", "Quantum", "Selfish RR (a)", "Selfish RR (b)", "Colas MLFQ"},
            new JComponent[]{spCantidadCPU, cbAlgoritmoCPU, spQuantum, spSelfishA, spSelfishB, spColasMLFQ}
        );

        JPanel cardAsignacion = crearCardFormulario(
            new String[]{"Asignación de memoria", "Ajuste", "Tamaño de página", "Tamaño de partición", "Particiones"},
            new JComponent[]{cbEstrategiaMemoria, cbAjusteMemoria, cbTamanoPagina, spTamanoParticion, txtParticiones}
        );

        JPanel panelCards = new JPanel(new GridLayout(1, 3, 10, 0));
        panelCards.setOpaque(false);
        panelCards.add(cardRAM);
        panelCards.add(cardCPU);
        panelCards.add(cardAsignacion);

        actualizarParametrosCPU();
        actualizarParametrosMemoria();

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        estilitarBotonSecundario(btnCancelar);
        btnCancelar.addActionListener(e -> dialogConfig.dispose());

        btnGuardarConfig = crearBotonPrincipal("Guardar");

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardarConfig);

        panelModal.add(panelCards, BorderLayout.CENTER);
        panelModal.add(panelBotones, BorderLayout.SOUTH);

        dialogConfig.setContentPane(panelModal);
    }

    private JPanel crearCardFormulario(String[] etiquetas, JComponent[] campos) {
        JPanel card = crearCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridx = 0;
        gbc.weightx = 1;

        int fila = 0;
        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridy = fila++;
            card.add(crearLabel(etiquetas[i]), gbc);
            gbc.gridy = fila++;
            card.add(campos[i], gbc);
        }

        gbc.gridy = fila;
        gbc.weighty = 1;
        card.add(Box.createVerticalGlue(), gbc);

        return card;
    }

    private void actualizarParametrosCPU() {
        String algoritmo = (String) cbAlgoritmoCPU.getSelectedItem();
        boolean esSelfish = "Selfish Round Robin".equals(algoritmo);

        spQuantum.setEnabled(algoritmoUsaQuantum(algoritmo));
        spSelfishA.setEnabled(esSelfish);
        spSelfishB.setEnabled(esSelfish);
        spColasMLFQ.setEnabled("Multilevel Feedback Queue".equals(algoritmo));
    }

    private void actualizarParametrosMemoria() {
        String estrategia = (String) cbEstrategiaMemoria.getSelectedItem();

        cbAjusteMemoria.setEnabled("Dinámica".equals(estrategia));
        cbTamanoPagina.setEnabled("Paginación".equals(estrategia));
        spTamanoParticion.setEnabled("Partición fija (iguales)".equals(estrategia));
        txtParticiones.setEnabled("Partición fija (diferentes)".equals(estrategia));
    }

    private boolean algoritmoUsaQuantum(String algoritmo) {
        return "Round Robin".equals(algoritmo)
                || "Selfish Round Robin".equals(algoritmo)
                || "Multilevel Feedback Queue".equals(algoritmo);
    }

    private void actualizarLimitesKernel() {

        int memorySize = (Integer) spTamanoMemoria.getValue();

        int minKernel = (int) Math.ceil(
            memorySize * SystemConfig.MIN_KERNEL_MEMORY_PERCENTAGE
        );

        int maxKernel = (int) Math.floor(
            memorySize * SystemConfig.MAX_KERNEL_MEMORY_PERCENTAGE
        );

        SpinnerNumberModel model =
            (SpinnerNumberModel) spLimiteKernel.getModel();

        model.setMinimum(minKernel);
        model.setMaximum(maxKernel);

        int kernelActual = (Integer) spLimiteKernel.getValue();

        if (kernelActual < minKernel) {
            spLimiteKernel.setValue(minKernel);
        } else if (kernelActual > maxKernel) {
            spLimiteKernel.setValue(maxKernel);
        }
    }

    private void actualizarLimitesMemoriaVirtual() {

        int diskSize = (Integer) spTamanoDisco.getValue();

        int minVirtual = (int) Math.ceil(
            diskSize * SystemConfig.MIN_VIRTUAL_MEMORY_PERCENTAGE
        );

        int maxVirtual = (int) Math.floor(
            diskSize * SystemConfig.MAX_VIRTUAL_MEMORY_PERCENTAGE
        );

        SpinnerNumberModel model =
            (SpinnerNumberModel) spTamanoVirtual.getModel();

        model.setMinimum(minVirtual);
        model.setMaximum(maxVirtual);

        int virtualActual = (Integer) spTamanoVirtual.getValue();

        if (virtualActual < minVirtual) {
            spTamanoVirtual.setValue(minVirtual);
        } else if (virtualActual > maxVirtual) {
            spTamanoVirtual.setValue(maxVirtual);
        }
    }

    /**
     * Habilita el botón de estadísticas y despliega la ventana emergente modal
     * con la tabla de tiempos por cada proceso ejecutado.
     * @param datosEstadisticas Lista de filas [Algoritmo, Proceso, Tiempo Inicio, Tiempo Final, Tiempo Ocioso, Turnaround, Tr/Ts]
     */
    public void mostrarVentanaEstadisticas(List<Object[]> datosEstadisticas) {
        hayEstadisticas = true;
        if (btnEstadisticas != null) {
            btnEstadisticas.setEnabled(true);
        }

        dialogEstadisticas = new JDialog(this, "Estadísticas de Ejecución de Procesos", true);
        dialogEstadisticas.setSize(760, 380);
        dialogEstadisticas.setResizable(false);
        dialogEstadisticas.setLocationRelativeTo(this);

        JPanel panelModal = new JPanel(new BorderLayout(12, 12));
        panelModal.setBackground(BG_PANEL);
        panelModal.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Encabezado
        JLabel lblTituloModal = new JLabel("RESUMEN DE TIEMPOS DE EJECUCIÓN");
        lblTituloModal.setForeground(TEXT);
        lblTituloModal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTituloModal.setHorizontalAlignment(SwingConstants.CENTER);

        // Tabla de Estadísticas
        String[] columnasEst = {"Algoritmo", "Proceso", "Tiempo Inicio", "Tiempo Final", "Tiempo Ocioso", "Turnaround", "Tr / Ts"};
        DefaultTableModel modeloEstadisticas = new DefaultTableModel(columnasEst, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        if (datosEstadisticas != null) {
            for (Object[] fila : datosEstadisticas) {
                modeloEstadisticas.addRow(fila);
            }
        }

        JTable tablaEstadisticas = crearTabla(modeloEstadisticas);
        JScrollPane scrollEstadisticas = new JScrollPane(tablaEstadisticas);
        scrollEstadisticas.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        // Botón Cerrar
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelInferior.setOpaque(false);

        JButton btnCerrar = new JButton("Cerrar");
        estilitarBotonSecundario(btnCerrar);
        btnCerrar.addActionListener(e -> dialogEstadisticas.dispose());
        panelInferior.add(btnCerrar);

        panelModal.add(lblTituloModal, BorderLayout.NORTH);
        panelModal.add(scrollEstadisticas, BorderLayout.CENTER);
        panelModal.add(panelInferior, BorderLayout.SOUTH);

        dialogEstadisticas.setContentPane(panelModal);
        dialogEstadisticas.setVisible(true);
    }

    /**
     * Ventana para asignar llegada y prioridad a cada programa.
     * Se reordena por tiempo de llegada cada vez que se cambia uno.
     * @param programas Lista de filas [Archivo, Ráfaga, Llegada, Prioridad]
     * @return Filas [indiceOriginal, Llegada, Prioridad] ordenadas por llegada, o null si se cancela
     */
    public List<Object[]> mostrarDialogoTiemposLlegada(List<Object[]> programas) {
        JDialog dialogLlegadas = new JDialog(this, "Tiempos de Llegada", true);
        dialogLlegadas.setSize(560, 380);
        dialogLlegadas.setResizable(false);
        dialogLlegadas.setLocationRelativeTo(this);

        JPanel panelModal = new JPanel(new BorderLayout(12, 12));
        panelModal.setBackground(BG_PANEL);
        panelModal.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTituloModal = new JLabel("TIEMPOS DE LLEGADA");
        lblTituloModal.setForeground(TEXT);
        lblTituloModal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTituloModal.setHorizontalAlignment(SwingConstants.CENTER);

        // la columna "idx" guarda la posición original y no se muestra
        String[] columnas = {"#", "Archivo", "Ráfaga", "Llegada", "Prioridad", "idx"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return c == 3 || c == 4; }
        };

        if (programas != null) {
            for (int i = 0; i < programas.size(); i++) {
                Object[] fila = programas.get(i);
                modelo.addRow(new Object[]{i + 1, fila[0], fila[1], fila[2], fila[3], i});
            }
        }

        JTable tabla = crearTabla(modelo);
        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tabla.removeColumn(tabla.getColumnModel().getColumn(5)); // ocultar idx

        // reordenar cuando cambia una llegada
        final boolean[] ordenando = {false};
        modelo.addTableModelListener(e -> {
            if (ordenando[0]) return;
            if (e.getType() != javax.swing.event.TableModelEvent.UPDATE) return;
            if (e.getColumn() != 3) return;
            SwingUtilities.invokeLater(() -> ordenarPorLlegada(modelo, ordenando));
        });

        ordenarPorLlegada(modelo, ordenando); // orden inicial

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelInferior.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        estilitarBotonSecundario(btnCancelar);
        btnCancelar.addActionListener(e -> dialogLlegadas.dispose());

        JButton btnGuardar = crearBotonPrincipal("Guardar");

        final List<Object[]> resultado = new ArrayList<>();
        final boolean[] guardado = {false};

        btnGuardar.addActionListener(e -> {
            if (tabla.isEditing()) {
                tabla.getCellEditor().stopCellEditing();
            }
            ordenarPorLlegada(modelo, ordenando); // por si el último cambio no se ordenó

            List<Object[]> filas = new ArrayList<>();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                int llegada = convertirEntero(modelo.getValueAt(i, 3));
                int prioridad = convertirEntero(modelo.getValueAt(i, 4));

                if (llegada < 0 || prioridad < 1) {
                    JOptionPane.showMessageDialog(dialogLlegadas,
                            "Valores inválidos en la fila #" + (i + 1) + ".",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                filas.add(new Object[]{ modelo.getValueAt(i, 5), llegada, prioridad });
            }

            resultado.addAll(filas);
            guardado[0] = true;
            dialogLlegadas.dispose();
        });

        panelInferior.add(btnCancelar);
        panelInferior.add(btnGuardar);

        panelModal.add(lblTituloModal, BorderLayout.NORTH);
        panelModal.add(scroll, BorderLayout.CENTER);
        panelModal.add(panelInferior, BorderLayout.SOUTH);

        dialogLlegadas.setContentPane(panelModal);
        dialogLlegadas.setVisible(true);

        return guardado[0] ? resultado : null;
    }

    // Ordena las filas por llegada (estable) y renumera la columna #
    private void ordenarPorLlegada(DefaultTableModel modelo, boolean[] ordenando) {
        ordenando[0] = true;

        List<Object[]> filas = new ArrayList<>();
        for (int r = 0; r < modelo.getRowCount(); r++) {
            Object[] fila = new Object[modelo.getColumnCount()];
            for (int c = 0; c < modelo.getColumnCount(); c++) {
                fila[c] = modelo.getValueAt(r, c);
            }
            filas.add(fila);
        }

        filas.sort((a, b) -> convertirEntero(a[3]) - convertirEntero(b[3]));

        modelo.setRowCount(0);
        for (int i = 0; i < filas.size(); i++) {
            filas.get(i)[0] = i + 1;
            modelo.addRow(filas.get(i));
        }

        ordenando[0] = false;
    }

    private int convertirEntero(Object valor) {
        try {
            return Integer.parseInt(String.valueOf(valor).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Crea el panel central con memorias y consola
    private JPanel crearPanelMemoria() {
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(BG_MAIN);

        String[] columnas = {"Posición", "Valor"};

        // 1. Memoria Principal (RAM)
        JPanel cardRAM = crearCard();
        cardRAM.setLayout(new BorderLayout(0, 10));
        cardRAM.add(crearEncabezadoSeccion("MEMORIA RAM", ""), BorderLayout.NORTH);

        modeloTablaMemoria = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMemoria = crearTabla(modeloTablaMemoria);
        JScrollPane scrollRAM = new JScrollPane(tablaMemoria);
        scrollRAM.setBorder(BorderFactory.createEmptyBorder());
        cardRAM.add(scrollRAM, BorderLayout.CENTER);

        // Subpanel Consola
        JPanel cardConsola = crearCard();
        cardConsola.setLayout(new BorderLayout(0, 8));
        cardConsola.add(crearEncabezadoSeccion("CONSOLA", ""), BorderLayout.NORTH);

        txtPantalla = new JTextArea();
        txtPantalla.setEditable(false);
        txtPantalla.setBackground(BG_INPUT);
        txtPantalla.setForeground(new Color(78, 201, 176));
        txtPantalla.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtPantalla.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scrollPantalla = new JScrollPane(txtPantalla);
        scrollPantalla.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        cardConsola.add(scrollPantalla, BorderLayout.CENTER);

        // 2. Almacenamiento Secundario (Disco y Memoria Virtual anidados)
        JPanel panelSecundarioArriba = new JPanel(new GridLayout(1, 2, 8, 0));
        panelSecundarioArriba.setOpaque(false);

        // Subpanel Disco para Archivos
        JPanel cardDisco = crearCard();
        cardDisco.setLayout(new BorderLayout(0, 10));
        cardDisco.add(crearEncabezadoSeccion("DISCO", ""), BorderLayout.NORTH);

        modeloTablaDisco = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDisco = crearTabla(modeloTablaDisco);
        JScrollPane scrollDisco = new JScrollPane(tablaDisco);
        scrollDisco.setBorder(BorderFactory.createEmptyBorder());
        cardDisco.add(scrollDisco, BorderLayout.CENTER);

        // Subpanel Memoria Virtual
        JPanel cardVirtual = crearCard();
        cardVirtual.setLayout(new BorderLayout(0, 10));
        cardVirtual.add(crearEncabezadoSeccion("MEMORIA VIRTUAL", ""), BorderLayout.NORTH);

        modeloTablaVirtual = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaVirtual = crearTabla(modeloTablaVirtual);
        JScrollPane scrollVirtual = new JScrollPane(tablaVirtual);
        scrollVirtual.setBorder(BorderFactory.createEmptyBorder());
        cardVirtual.add(scrollVirtual, BorderLayout.CENTER);

        panelSecundarioArriba.add(cardDisco);
        panelSecundarioArriba.add(cardVirtual);

        // Subpanel Ejecución
        JPanel cardEjecucion = crearCard();
        cardEjecucion.setLayout(new BorderLayout(0, 10));
        cardEjecucion.add(crearEncabezadoSeccion("EJECUCIÓN", ""), BorderLayout.NORTH);

        modeloTablaEjecucion = new DefaultTableModel(new String[]{"Núcleo", "Proceso"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEjecucion = crearTabla(modeloTablaEjecucion);
        tablaEjecucion.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaEjecucionFija = crearTabla(modeloTablaEjecucion);
        tablaEjecucionFija.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configurarColumnasEjecucion();
        JScrollPane scrollEjecucion = new JScrollPane(tablaEjecucion);
        scrollEjecucion.setBorder(BorderFactory.createEmptyBorder());
        scrollEjecucion.setRowHeaderView(tablaEjecucionFija);
        scrollEjecucion.setCorner(JScrollPane.UPPER_LEFT_CORNER, tablaEjecucionFija.getTableHeader());
        cardEjecucion.add(scrollEjecucion, BorderLayout.CENTER);

        JPanel panelIzquierdoCentral = new JPanel(new GridBagLayout());
        panelIzquierdoCentral.setOpaque(false);

        GridBagConstraints gbcIzquierdo = new GridBagConstraints();
        gbcIzquierdo.fill = GridBagConstraints.BOTH;
        gbcIzquierdo.gridx = 0;
        gbcIzquierdo.weightx = 1.0;

        gbcIzquierdo.gridy = 0;
        gbcIzquierdo.weighty = 0.70;
        gbcIzquierdo.insets = new Insets(0, 0, 6, 0);
        panelIzquierdoCentral.add(cardRAM, gbcIzquierdo);

        gbcIzquierdo.gridy = 1;
        gbcIzquierdo.weighty = 0.30;
        gbcIzquierdo.insets = new Insets(6, 0, 0, 0);
        panelIzquierdoCentral.add(cardConsola, gbcIzquierdo);

        JPanel panelDerechoCentral = new JPanel(new GridBagLayout());
        panelDerechoCentral.setOpaque(false);

        GridBagConstraints gbcDerecho = new GridBagConstraints();
        gbcDerecho.fill = GridBagConstraints.BOTH;
        gbcDerecho.gridx = 0;
        gbcDerecho.weightx = 1.0;

        gbcDerecho.gridy = 0;
        gbcDerecho.weighty = 0.60;
        gbcDerecho.insets = new Insets(0, 0, 6, 0);
        panelDerechoCentral.add(panelSecundarioArriba, gbcDerecho);

        gbcDerecho.gridy = 1;
        gbcDerecho.weighty = 0.40;
        gbcDerecho.insets = new Insets(6, 0, 0, 0);
        panelDerechoCentral.add(cardEjecucion, gbcDerecho);

        Dimension base = new Dimension(50, 50);
        cardRAM.setPreferredSize(base);
        cardConsola.setPreferredSize(base);
        panelSecundarioArriba.setPreferredSize(base);
        cardEjecucion.setPreferredSize(base);
        panelIzquierdoCentral.setPreferredSize(base);
        panelDerechoCentral.setPreferredSize(base);

        // Layout general del panel central con 45% RAM, 55% almacenamiento secundario
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.45;
        gbc.insets = new Insets(0, 0, 0, 6);
        panelCentral.add(panelIzquierdoCentral, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        gbc.insets = new Insets(0, 6, 0, 0);
        panelCentral.add(panelDerechoCentral, gbc);

        return panelCentral;
    }

    // Crea el panel derecho para BCP, registros del CPU y Lista de Trabajos
    private JPanel crearPanelBCP() {
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(BG_PANEL);
        panelPrincipal.setBorder(new EmptyBorder(16, 14, 16, 14));
        panelPrincipal.setPreferredSize(new Dimension(320, 0));

        JLabel titulo = crearTituloCentrado("PROCESO EN EJECUCIÓN");
        panelPrincipal.add(titulo);
        panelPrincipal.add(Box.createVerticalStrut(10));

        // Pestañas de los CPU
        tabsCPU = new JTabbedPane();
        for (int i = 0; i < MAX_CPUS; i++) {
            panelesCPU[i] = crearPanelCPU(i);
        }
        configurarCantidadCPU(1);
        tabsCPU.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        tabsCPU.setPreferredSize(new Dimension(290, 400));

        panelPrincipal.add(tabsCPU);
        panelPrincipal.add(Box.createVerticalStrut(14));

        JLabel tituloTrabajos = crearTituloCentrado("LISTA DE TRABAJOS Y PROCESOS");
        panelPrincipal.add(tituloTrabajos);
        panelPrincipal.add(Box.createVerticalStrut(8));

        JPanel cardTrabajos = crearCard();
        cardTrabajos.setLayout(new BorderLayout(0, 8));

        String[] colsTrabajos = {"PID", "Archivo", "Estado"};
        modeloTablaTrabajos = new DefaultTableModel(colsTrabajos, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTrabajos = crearTabla(modeloTablaTrabajos);

        JScrollPane scrollTrabajos = new JScrollPane(tablaTrabajos);
        scrollTrabajos.setBorder(BorderFactory.createEmptyBorder());

        cardTrabajos.add(scrollTrabajos, BorderLayout.CENTER);
        panelPrincipal.add(cardTrabajos);

        return panelPrincipal;
    }

    private JPanel crearPanelCPU(int i) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel cardProceso = crearCard();
        cardProceso.setLayout(new BorderLayout(8, 8));
        cardProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        lblProcessID[i] = new JLabel("Sin proceso cargado");
        lblProcessID[i].setForeground(TEXT);
        lblProcessID[i].setFont(new Font("SansSerif", Font.BOLD, 15));

        lblEstadoBCP[i] = new JLabel("ESTADO: ESPERANDO ARCHIVO");
        lblEstadoBCP[i].setForeground(ORANGE);
        lblEstadoBCP[i].setFont(new Font("SansSerif", Font.BOLD, 11));

        cardProceso.add(lblProcessID[i], BorderLayout.NORTH);
        cardProceso.add(lblEstadoBCP[i], BorderLayout.SOUTH);

        panel.add(cardProceso);
        panel.add(Box.createVerticalStrut(12));

        JLabel tituloCPU = crearTituloCentrado("REGISTROS CPU");
        panel.add(tituloCPU);
        panel.add(Box.createVerticalStrut(8));

        JPanel registros = new JPanel(new GridLayout(3, 2, 6, 6));
        registros.setOpaque(false);
        registros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        txtPC[i] = crearCampoRegistro();
        txtAC[i] = crearCampoRegistro();
        txtAX[i] = crearCampoRegistro();
        txtBX[i] = crearCampoRegistro();
        txtCX[i] = crearCampoRegistro();
        txtDX[i] = crearCampoRegistro();

        registros.add(crearRegistroCard("PC", txtPC[i]));
        registros.add(crearRegistroCard("AC", txtAC[i]));
        registros.add(crearRegistroCard("AX", txtAX[i]));
        registros.add(crearRegistroCard("BX", txtBX[i]));
        registros.add(crearRegistroCard("CX", txtCX[i]));
        registros.add(crearRegistroCard("DX", txtDX[i]));

        // Registro IR de ancho completo
        txtIR[i] = crearCampoRegistro();
        JPanel cardIR = crearRegistroCard("IR (Instruction Register)", txtIR[i]);
        cardIR.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        panel.add(registros);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cardIR);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void configurarCantidadCPU(int cantidad) {
        if (cantidad < 1) cantidad = 1;
        if (cantidad > MAX_CPUS) cantidad = MAX_CPUS;

        tabsCPU.removeAll();
        for (int i = 0; i < cantidad; i++) {
            tabsCPU.addTab("CPU " + (i + 1), panelesCPU[i]);
        }
    }

    private JPanel crearCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1),
                        new EmptyBorder(10, 10, 10, 10)
                )
        );
        return card;
    }

    // Crea el encabezado para las secciones de las tablas
    private JPanel crearEncabezadoSeccion(String titulo, String descripcion) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setForeground(TEXT_SECONDARY);
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 10));

        panel.add(lblTitulo, BorderLayout.WEST);
        panel.add(lblDescripcion, BorderLayout.EAST);

        return panel;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(170, 38));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);

        boton.putClientProperty(
                FlatClientProperties.STYLE,
                "background: " + convertirColor(color) + "; " +
                "foreground: #FFFFFF; " +
                "font: bold;"
        );

        return boton;
    }

    private JButton crearBotonPrincipal(String texto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(100, 32));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.putClientProperty(
                FlatClientProperties.STYLE,
                "background: " + convertirColor(BLUE) + "; foreground: #FFFFFF; font: bold;"
        );
        return boton;
    }

    // Estiliza un botón secundario
    private void estilitarBotonSecundario(JButton boton) {
        boton.setPreferredSize(new Dimension(90, 32));
        boton.setFocusPainted(false);

        boton.putClientProperty(
                FlatClientProperties.STYLE,
                "background: #252B33; " +
                "foreground: #DDE2E8; " +
                "borderWidth: 1; " +
                "borderColor: #3B434E;"
        );
    }

    // Aplica estilos al JSpinner
    private void estilitarSpinner(JComponent spinner) {
        spinner.setPreferredSize(new Dimension(190, 30));
        spinner.putClientProperty(
                FlatClientProperties.STYLE,
                "background: #16191E; " +
                "foreground: #E8EBEF; " +
                "borderColor: #414954;"
        );
    }

    // Configura y crea una tabla estándar con filas alternas
    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setBackground(BG_CARD);
        tabla.setForeground(TEXT);
        tabla.setSelectionBackground(new Color(45, 90, 140));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));

        tabla.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tabla.getTableHeader().setBackground(new Color(37, 42, 50));
        tabla.getTableHeader().setForeground(TEXT_SECONDARY);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(BG_CARD);
                    } else {
                        c.setBackground(new Color(28, 32, 38));
                    }
                    c.setForeground(TEXT);
                }

                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        return tabla;
    }

    private final DefaultTableCellRenderer rendererEjecucion = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, false, false, row, column);

            if (table.convertColumnIndexToModel(column) >= 2 && value instanceof Integer) {
                int pid = (Integer) value;
                c.setBackground(COLORES_PROCESOS[pid % COLORES_PROCESOS.length]);
                setText("");
            } else if (row % 2 == 0) {
                c.setBackground(BG_CARD);
            } else {
                c.setBackground(new Color(28, 32, 38));
            }
            c.setForeground(TEXT);

            setBorder(new EmptyBorder(0, 8, 0, 8));
            return c;
        }
    };

    // Hay que llamarlo cada vez que se agregan columnas
    private void configurarColumnasEjecucion() {
        javax.swing.table.TableColumnModel fijas = tablaEjecucionFija.getColumnModel();
        while (fijas.getColumnCount() > 2) {
            fijas.removeColumn(fijas.getColumn(2));
        }
        fijas.getColumn(0).setPreferredWidth(70);
        fijas.getColumn(1).setPreferredWidth(130);
        fijas.getColumn(0).setCellRenderer(rendererEjecucion);
        fijas.getColumn(1).setCellRenderer(rendererEjecucion);
        tablaEjecucionFija.setPreferredScrollableViewportSize(new Dimension(200, 0));

        javax.swing.table.TableColumnModel segundos = tablaEjecucion.getColumnModel();
        while (segundos.getColumnCount() > 0 && segundos.getColumn(0).getModelIndex() < 2) {
            segundos.removeColumn(segundos.getColumn(0));
        }
        for (int i = 0; i < segundos.getColumnCount(); i++) {
            segundos.getColumn(i).setPreferredWidth(30);
            segundos.getColumn(i).setCellRenderer(rendererEjecucion);
        }
    }

    private JTextField crearCampoRegistro() {
        JTextField tf = new JTextField("0");
        tf.setEditable(false);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tf.setFont(new Font("Monospaced", Font.BOLD, 13));

        tf.putClientProperty(
                FlatClientProperties.STYLE,
                "background: #161A20; " +
                "foreground: #4EC9B0; " +
                "caretColor: #4EC9B0;"
        );

        return tf;
    }

    // Crea el contenedor visual para un registro de CPU
    private JPanel crearRegistroCard(String nombre, JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(BG_CARD);

        JLabel label = new JLabel(nombre);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));

        panel.add(label, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);

        return panel;
    }

    // Crea etiquetas de título centradas para los paneles principales
    private JLabel crearTituloCentrado(String texto) {
        JLabel label = new JLabel(texto, JLabel.CENTER);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return label;
    }

    // Convierte un objeto Color a su representación hexadecimal en String
    private String convertirColor(Color color) {
        return String.format(
                "#%02X%02X%02X",
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }

    // Muestra un diálogo de error
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Actualiza la tabla de memoria RAM, ingresa los datos y selecciona/resalta automáticamente la fila indicada.
     * @param listaMemoria Una lista de arreglos de objetos donde cada posición es: [Posición, Valor]
     * @param indiceResaltado El índice de la fila que se desea seleccionar (-1 para limpiar la selección)
     */
    public void actualizarTablaMemoria(List<Object[]> listaMemoria, int indiceResaltado) {
        actualizarTablaMemoria(listaMemoria, new int[]{indiceResaltado});
    }

    public void actualizarTablaMemoria(List<Object[]> listaMemoria, int[] indicesResaltados) {
        modeloTablaMemoria.setRowCount(0);
        for (Object[] fila : listaMemoria) {
            modeloTablaMemoria.addRow(fila);
        }

        tablaMemoria.clearSelection();
        if (indicesResaltados == null) return;

        for (int indice : indicesResaltados) {
            if (indice >= 0 && indice < tablaMemoria.getRowCount()) {
                tablaMemoria.addRowSelectionInterval(indice, indice);
                tablaMemoria.scrollRectToVisible(tablaMemoria.getCellRect(indice, 0, true));
            }
        }
    }

    /**
     * Actualiza la tabla del Disco secundario.
     * @param listaDisco Lista de arreglos de objetos: [Posición, Valor]
     */
    public void actualizarTablaDisco(List<Object[]> listaDisco) {
        modeloTablaDisco.setRowCount(0);
        for (Object[] fila : listaDisco) {
            modeloTablaDisco.addRow(fila);
        }
    }

    /**
     * Actualiza la tabla de Memoria Virtual.
     * @param listaVirtual Lista de arreglos de objetos: [Posición, Valor]
     */
    public void actualizarTablaMemoriaVirtual(List<Object[]> listaVirtual) {
        modeloTablaVirtual.setRowCount(0);
        for (Object[] fila : listaVirtual) {
            modeloTablaVirtual.addRow(fila);
        }
    }

    /**
     * Actualiza la tabla de la Lista de Trabajos.
     * @param listaTrabajos Lista de arreglos de objetos: [PID, Nombre Archivo, Estado (7 estados)]
     */
    public void actualizarTablaTrabajos(List<Object[]> listaTrabajos) {
        modeloTablaTrabajos.setRowCount(0);
        for (Object[] fila : listaTrabajos) {
            modeloTablaTrabajos.addRow(fila);
        }
    }

    /**
     * Agrega a la tabla de ejecución el proceso que corrió en ese segundo.
     * @param cpu Número de CPU (1 a 5)
     * @param segundo Empieza en 1
     */
    public void registrarEjecucion(int cpu, int pid, String archivo, int segundo) {
        if (segundo < 1) return;

        String clave = cpu + "|" + pid;
        int fila = filasEjecucion.indexOf(clave);

        // si no tiene fila se agrega después de las de su CPU
        if (fila == -1) {
            fila = 0;
            while (fila < filasEjecucion.size()
                    && Integer.parseInt(filasEjecucion.get(fila).split("\\|")[0]) <= cpu) {
                fila++;
            }
            Object[] nuevaFila = new Object[modeloTablaEjecucion.getColumnCount()];
            nuevaFila[0] = "CPU " + cpu;
            nuevaFila[1] = "PID " + pid + " - " + archivo;
            modeloTablaEjecucion.insertRow(fila, nuevaFila);
            filasEjecucion.add(fila, clave);
        }

        boolean nuevasColumnas = false;
        while (modeloTablaEjecucion.getColumnCount() - 2 < segundo) {
            modeloTablaEjecucion.addColumn(String.valueOf(modeloTablaEjecucion.getColumnCount() - 1));
            nuevasColumnas = true;
        }
        if (nuevasColumnas) {
            configurarColumnasEjecucion();
        }

        modeloTablaEjecucion.setValueAt(pid, fila, segundo + 1);
        int columnaVista = tablaEjecucion.convertColumnIndexToView(segundo + 1);
        tablaEjecucion.scrollRectToVisible(tablaEjecucion.getCellRect(fila, columnaVista, true));
    }

    public void limpiarTablaEjecucion() {
        modeloTablaEjecucion.setRowCount(0);
        modeloTablaEjecucion.setColumnCount(2);
        filasEjecucion.clear();
        configurarColumnasEjecucion();
    }

    public void setModoAutomatico(boolean activo) {
        btnSeleccionar.setEnabled(!activo);
        btnTiemposLlegada.setEnabled(!activo);
        btnCargar.setEnabled(!activo);
        btnPasoAPaso.setEnabled(!activo);
        btnEjecutar.setEnabled(!activo);
        btnLimpiar.setEnabled(!activo);
        btnAbrirConfig.setEnabled(!activo && configuracionHabilitada);
        btnPausar.setEnabled(activo);
    }

    /**
     * Deshabilita los controles de configuración de memoria y kernel para evitar cambios mientras un programa está cargado.
     */
    public void deshabilitarConfiguraciones() {
        configuracionHabilitada = false;
        if (btnAbrirConfig != null) btnAbrirConfig.setEnabled(false);
    }

    /**
     * Habilita los controles de configuración de memoria y kernel.
     */
    public void habilitarConfiguraciones() {
        configuracionHabilitada = true;
        if (btnAbrirConfig != null) btnAbrirConfig.setEnabled(true);
    }

    /**
     * Limpia la vista de la interfaz, reseteando las tablas, la consola y los registros del CPU a sus valores iniciales.
     */
    public void limpiarVista() {
        modeloTablaMemoria.setRowCount(0);
        modeloTablaDisco.setRowCount(0);
        modeloTablaVirtual.setRowCount(0);
        modeloTablaTrabajos.setRowCount(0);
        limpiarTablaEjecucion();

        tablaMemoria.clearSelection();

        if (txtPantalla != null) txtPantalla.setText("");

        for (int cpu = 1; cpu <= MAX_CPUS; cpu++) {
            setEstadoBCP(cpu, "ESPERANDO ARCHIVO");
            lblProcessID[cpu - 1].setText("Sin proceso cargado");
            setPC(cpu, 0);
            setIR(cpu, "0");
            setAC(cpu, 0);
            setAX(cpu, 0);
            setBX(cpu, 0);
            setCX(cpu, 0);
            setDX(cpu, 0);
        }

        ticksCount = 0;
        lblTicks.setText("TICKS: " + ticksCount);
        setModoAutomatico(false);

        if (btnEstadisticas != null) {
            btnEstadisticas.setEnabled(hayEstadisticas);
        }
    }

    public void mostrarPanelConfig(boolean mostrar) {
        if (dialogConfig == null) {
            abrirDialogoConfiguracion();
        }
        if (mostrar) {
            dialogConfig.setVisible(true);
        }
    }


    public JButton getBtnSeleccionar() { return btnSeleccionar; }
    public JButton getBtnTiemposLlegada() { return btnTiemposLlegada; }
    public JButton getBtnCargar() { return btnCargar; }
    public JButton getBtnPasoAPaso() { return btnPasoAPaso; }
    public JButton getBtnEjecutar() { return btnEjecutar; }
    public JButton getBtnPausar() { return btnPausar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnAbrirConfig() { return btnAbrirConfig; }
    public JButton getBtnGuardarConfig() { return btnGuardarConfig; }
    public JButton getBtnEstadisticas() { return btnEstadisticas; }
    public JTabbedPane getTabsCPU() { return tabsCPU; }

    public JDialog getDialogConfig() { return dialogConfig; }

    public int getTamanoMemoriaSeleccionado() { return spTamanoMemoria != null ? (int) spTamanoMemoria.getValue() : SystemConfig.getMemorySize(); }
    public int getLimiteKernelSeleccionado() { return spLimiteKernel != null ? (int) spLimiteKernel.getValue() : SystemConfig.getUserMemoryStart(); }
    public int getTamanoDiscoSeleccionado() { return spTamanoDisco != null ? (int) spTamanoDisco.getValue() : SystemConfig.getDiskSize(); }
    public int getTamanoVirtualSeleccionado() { return spTamanoVirtual != null ? (int) spTamanoVirtual.getValue() : SystemConfig.getDiskMemoryVirtualSize(); }

    public int getCantidadCPUSeleccionada() { return spCantidadCPU != null ? (int) spCantidadCPU.getValue() : 1; }
    public String getAlgoritmoCPUSeleccionado() { return cbAlgoritmoCPU != null ? (String) cbAlgoritmoCPU.getSelectedItem() : "FCFS"; }
    public int getQuantumSeleccionado() { return spQuantum != null ? (int) spQuantum.getValue() : 2; }
    public int getSelfishASeleccionado() { return spSelfishA != null ? (int) spSelfishA.getValue() : 2; }
    public int getSelfishBSeleccionado() { return spSelfishB != null ? (int) spSelfishB.getValue() : 1; }
    public int getColasMLFQSeleccionadas() { return spColasMLFQ != null ? (int) spColasMLFQ.getValue() : 3; }

    public String getEstrategiaMemoriaSeleccionada() { return cbEstrategiaMemoria != null ? (String) cbEstrategiaMemoria.getSelectedItem() : "Dinámica"; }
    public String getAjusteMemoriaSeleccionado() { return cbAjusteMemoria != null ? (String) cbAjusteMemoria.getSelectedItem() : "First Fit"; }
    public int getTamanoPaginaSeleccionado() { return cbTamanoPagina != null ? (Integer) cbTamanoPagina.getSelectedItem() : 8; }
    public int getTamanoParticionSeleccionado() { return spTamanoParticion != null ? (int) spTamanoParticion.getValue() : 16; }

    // retorna null si el texto no es válido
    public int[] getParticionesSeleccionadas() {
        String texto = txtParticiones != null ? txtParticiones.getText() : "8,16,32,64";
        String[] partes = texto.split(",");
        int[] tamanos = new int[partes.length];
        try {
            for (int i = 0; i < partes.length; i++) {
                tamanos[i] = Integer.parseInt(partes[i].trim());
                if (tamanos[i] <= 0) return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return tamanos;
    }

    public DefaultTableModel getModeloTablaMemoria() { return modeloTablaMemoria; }
    public DefaultTableModel getModeloTablaDisco() { return modeloTablaDisco; }
    public DefaultTableModel getModeloTablaVirtual() { return modeloTablaVirtual; }
    public DefaultTableModel getModeloTablaTrabajos() { return modeloTablaTrabajos; }
    public DefaultTableModel getModeloTablaEjecucion() { return modeloTablaEjecucion; }

    public JTable getTablaMemoria() { return tablaMemoria; }
    public JTable getTablaDisco() { return tablaDisco; }
    public JTable getTablaVirtual() { return tablaVirtual; }
    public JTable getTablaTrabajos() { return tablaTrabajos; }
    public JTable getTablaEjecucion() { return tablaEjecucion; }

    public JTextArea getTxtPantalla() { return txtPantalla; }

    // cpu va de 1 a 5
    private boolean cpuValido(int cpu) { return cpu >= 1 && cpu <= MAX_CPUS; }

    public void setEstadoBCP(int cpu, String estado) { if (cpuValido(cpu)) lblEstadoBCP[cpu - 1].setText("ESTADO: " + estado); }
    public void setProcessID(int cpu, int pid) { if (cpuValido(cpu)) lblProcessID[cpu - 1].setText("PID " + pid); }
    public void setPC(int cpu, int valor) { if (cpuValido(cpu)) txtPC[cpu - 1].setText(String.valueOf(valor)); }
    public void setIR(int cpu, String valor) { if (cpuValido(cpu)) txtIR[cpu - 1].setText(valor); }
    public void setAC(int cpu, int valor) { if (cpuValido(cpu)) txtAC[cpu - 1].setText(String.valueOf(valor)); }
    public void setAX(int cpu, int valor) { if (cpuValido(cpu)) txtAX[cpu - 1].setText(String.valueOf(valor)); }
    public void setAX(int cpu, String valor) { if (cpuValido(cpu)) txtAX[cpu - 1].setText(String.valueOf(valor)); }
    public void setBX(int cpu, int valor) { if (cpuValido(cpu)) txtBX[cpu - 1].setText(String.valueOf(valor)); }
    public void setCX(int cpu, int valor) { if (cpuValido(cpu)) txtCX[cpu - 1].setText(String.valueOf(valor)); }
    public void setDX(int cpu, int valor) { if (cpuValido(cpu)) txtDX[cpu - 1].setText(String.valueOf(valor)); }
    public void setDX(int cpu, String valor) { if (cpuValido(cpu)) txtDX[cpu - 1].setText(String.valueOf(valor)); }

    public void setEstadoBCP(String estado) { setEstadoBCP(1, estado); }
    public void setPC(int valor) { setPC(1, valor); }
    public void setProcessID(int pid) { setProcessID(1, pid); }
    public void setIR(String valor) { setIR(1, valor); }
    public void setAC(int valor) { setAC(1, valor); }
    public void setAX(int valor) { setAX(1, valor); }
    public void setAX(String valor) { setAX(1, valor); }
    public void setBX(int valor) { setBX(1, valor); }
    public void setCX(int valor) { setCX(1, valor); }
    public void setDX(int valor) { setDX(1, valor); }
    public void setDX(String valor) { setDX(1, valor); }
    public void setTicks(int valor) { ticksCount = valor; lblTicks.setText("TICKS: " + ticksCount); }
}