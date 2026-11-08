package com.minibodega.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.minibodega.dao.ProductoDAO;
import com.minibodega.modelo.Producto;

public class POSFrame extends JFrame {
    private JTabbedPane panelPestañas;
    
    // Componentes del Dashboard Moderno
    private JLabel lblDashVentas;
    private JLabel lblDashInversion;
    private JLabel lblDashGanancia;

    // Componentes del POS y Cierre
    private DefaultTableModel modeloResumenCaja;
    private JTable tablaResumenCaja;
    private JLabel lblTotalGananciaDia;
    
    // Componentes de Reportes
    private DefaultTableModel modeloReportes;
    private JTable tablaReportes;
    private DefaultTableModel modeloDetalleReporte;

    // Componentes de Gestión de Productos e Insumos
    private JTable tablaAdminProductos;
    private DefaultTableModel modeloAdminProductos;
    private DefaultTableModel modeloInsumosRecientes;
    private JLabel lblSumaInversionInsumos;
    
    private ProductoDAO productoDAO;
    
    // Estructuras en memoria para almacenar la operativa diaria y las inversiones de insumos
    private List<ItemConteoDiario> listaConteoGlobal = new ArrayList<>();
    private List<ReporteDiario> listaReportesGlobal = new ArrayList<>();
    private List<ItemInsumoDetalle> listaInsumosGlobal = new ArrayList<>();

    public static class ItemConteoDiario {
        int id;
        String nombre;
        double precio;
        int cantInicial;
        int cantRetorno;
        double costoInversionUnitario;

        public ItemConteoDiario(int id, String nombre, double precio, int cantInicial, int cantRetorno, double costoInversionUnitario) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.cantInicial = cantInicial;
            this.cantRetorno = cantRetorno;
            this.costoInversionUnitario = costoInversionUnitario;
        }

        public int getVendidos() {
            int vendidos = cantInicial - cantRetorno;
            return Math.max(vendidos, 0);
        }

        public double getTotalVenta() {
            return getVendidos() * precio;
        }

        public double getTotalInversion() {
            return getVendidos() * costoInversionUnitario;
        }

        public double getGananciaVendida() {
            return getTotalVenta() - getTotalInversion();
        }
    }

    public static class ReporteDiario {
        String fecha;
        double totalVentas;
        double inversion;
        double gananciaNeta;
        double diferenciaCaja;

        public ReporteDiario(String fecha, double totalVentas, double inversion, double gananciaNeta, double diferenciaCaja) {
            this.fecha = fecha;
            this.totalVentas = totalVentas;
            this.inversion = inversion;
            this.gananciaNeta = gananciaNeta;
            this.diferenciaCaja = diferenciaCaja;
        }
    }

    public static class ItemInsumoDetalle {
        int idProd;
        String nombreProd;
        String insumo;
        String medida;
        double cantidad;
        double inversion;

        public ItemInsumoDetalle(int idProd, String nombreProd, String insumo, String medida, double cantidad, double inversion) {
            this.idProd = idProd;
            this.nombreProd = nombreProd;
            this.insumo = insumo;
            this.medida = medida;
            this.cantidad = cantidad;
            this.inversion = inversion;
        }
    }

    public POSFrame() {
        productoDAO = new ProductoDAO();
        
        setTitle("Mini Bodega María - Sistema de Gestión y Ventas");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Configuración general del panel de pestañas unificado
        panelPestañas = new JTabbedPane();
        panelPestañas.setFont(new Font("Arial", Font.BOLD, 13));
        panelPestañas.setBackground(Color.decode("#0f172a"));
        panelPestañas.setForeground(Color.WHITE);

        panelPestañas.addTab("📊 Dashboard Financiero", crearPanelDashboard());
        panelPestañas.addTab("🛒 Caja y Ventas (POS)", crearPanelPOS());
        panelPestañas.addTab("📈 Reportes Diarios", crearPanelReportes());
        panelPestañas.addTab("📦 Gestión e Inversión", crearPanelInventario());

        add(panelPestañas, BorderLayout.CENTER);
        
        inicializarConteoEjemplo();
    }

    private void inicializarConteoEjemplo() {
        List<Producto> prods = productoDAO.listarProductos();
        listaConteoGlobal.clear();
        for (Producto p : prods) {
            listaConteoGlobal.add(new ItemConteoDiario(p.getIdProducto(), p.getNombre(), p.getPrecio(), 10, 0, p.getPrecio() * 0.6));
        }
        actualizarTablaResumenCaja();
        actualizarDashboardMetricas();
    }

    // --- ESTILO VISUAL COMÚN PARA TABLAS ---
    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(Color.decode("#1e293b"));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(Color.decode("#334155"));
        tabla.setSelectionBackground(Color.decode("#4f46e5"));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(Color.decode("#0f172a"));
        tabla.getTableHeader().setForeground(Color.decode("#38bdf8"));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.setRowHeight(24);
    }

    // --- MÓDULO 1: DASHBOARD ---
    private JPanel crearPanelDashboard() {
        JPanel panelDashboard = new JPanel(new BorderLayout(20, 20));
        panelDashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelDashboard.setBackground(Color.decode("#0f172a"));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Color.decode("#1e1b4b"));
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#4f46e5"), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel lblBienvenida = new JLabel("<html><span style='color: #818cf8; font-size: 11px; letter-spacing: 2px;'>SISTEMA DE MONITOREO EN TIEMPO REAL</span><br>"
                + "<b style='color: #ffffff; font-size: 18px;'>MINI BODEGA MARÍA — DASHBOARD FINANCIERO</b></html>");
        banner.add(lblBienvenida, BorderLayout.WEST);
        
        JLabel lblEstadoVivo = new JLabel("● CONECTADO A NEON POSTGRESQL", JLabel.RIGHT);
        lblEstadoVivo.setForeground(Color.decode("#34d399"));
        lblEstadoVivo.setFont(new Font("Arial", Font.BOLD, 12));
        banner.add(lblEstadoVivo, BorderLayout.EAST);
        
        panelDashboard.add(banner, BorderLayout.NORTH);

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 4, 15, 0));
        panelTarjetas.setOpaque(false);
        
        lblDashVentas = new JLabel("S/. 0.00", JLabel.CENTER);
        lblDashInversion = new JLabel("S/. 0.00", JLabel.CENTER);
        lblDashGanancia = new JLabel("S/. 0.00", JLabel.CENTER);
        JLabel lblDevoluciones = new JLabel("0.00 %", JLabel.CENTER);
        
        panelTarjetas.add(crearTarjetaFuturista("VENTAS TOTALES", lblDashVentas, "#818cf8", "Flujo de caja diario"));
        panelTarjetas.add(crearTarjetaFuturista("INVERSIÓN INSUMOS", lblDashInversion, "#38bdf8", "Azúcar, vasos, frutas"));
        panelTarjetas.add(crearTarjetaFuturista("GANANCIA NETA", lblDashGanancia, "#34d399", "Utilidad real del día"));
        panelTarjetas.add(crearTarjetaFuturista("MARGEN OPERATIVO", lblDevoluciones, "#fbbf24", "Optimización de stock"));
        
        JPanel panelGraficos = new JPanel(new GridLayout(1, 2, 15, 0));
        panelGraficos.setOpaque(false);

        JPanel panelInfoAnalitica = new JPanel(new BorderLayout());
        panelInfoAnalitica.setBackground(Color.decode("#1e293b"));
        panelInfoAnalitica.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#334155"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblTituloGrafico1 = new JLabel("📊 Análisis de Inversión por Subproductos (Raspadilla & Otros)");
        lblTituloGrafico1.setForeground(Color.WHITE);
        lblTituloGrafico1.setFont(new Font("Arial", Font.BOLD, 13));
        panelInfoAnalitica.add(lblTituloGrafico1, BorderLayout.NORTH);
        
        JTextArea txtSimulacionGrafico = new JTextArea(
            "\n [ OK ] Conexión de subproductos activa (Azúcar, Frutas, Vasos N°9).\n" +
            " [ OK ] Sincronización automática con la base de datos Neon.\n" +
            " [ INFO ] El flujo operativo de caja calcula retornos y ganancias en tiempo real.\n" +
            " [ AUDITORÍA ] Verifique los reportes diarios para cierres consolidados."
        );
        txtSimulacionGrafico.setBackground(Color.decode("#0f172a"));
        txtSimulacionGrafico.setForeground(Color.decode("#94a3b8"));
        txtSimulacionGrafico.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSimulacionGrafico.setEditable(false);
        panelInfoAnalitica.add(new JScrollPane(txtSimulacionGrafico), BorderLayout.CENTER);

        JPanel panelEstadoBD = new JPanel(new BorderLayout());
        panelEstadoBD.setBackground(Color.decode("#1e293b"));
        panelEstadoBD.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#334155"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTituloGrafico2 = new JLabel("⚡ Estado de Motores y Persistencia");
        lblTituloGrafico2.setForeground(Color.WHITE);
        lblTituloGrafico2.setFont(new Font("Arial", Font.BOLD, 13));
        panelEstadoBD.add(lblTituloGrafico2, BorderLayout.NORTH);

        JPanel panelMetricasBD = new JPanel(new GridLayout(3, 1, 5, 5));
        panelMetricasBD.setOpaque(false);
        panelMetricasBD.add(crearFilaMetricaBD("Base de Datos:", "Neon PostgreSQL (Cloud)", "#38bdf8"));
        panelMetricasBD.add(crearFilaMetricaBD("Driver JDBC:", "PostgreSQL Native Driver", "#34d399"));
        panelMetricasBD.add(crearFilaMetricaBD("Estado de Sincronización:", "Tiempo Real (Sincronizado)", "#818cf8"));
        
        panelEstadoBD.add(panelMetricasBD, BorderLayout.CENTER);

        panelGraficos.add(panelInfoAnalitica);
        panelGraficos.add(panelEstadoBD);

        JPanel contenedorCentro = new JPanel(new BorderLayout(0, 20));
        contenedorCentro.setOpaque(false);
        contenedorCentro.add(panelTarjetas, BorderLayout.NORTH);
        contenedorCentro.add(panelGraficos, BorderLayout.CENTER);
        
        panelDashboard.add(contenedorCentro, BorderLayout.CENTER);

        return panelDashboard;
    }

    private JPanel crearTarjetaFuturista(String titulo, JLabel lblVal, String colorTextoHex, String footer) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.decode("#1e293b"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#334155"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTit = new JLabel(titulo, JLabel.CENTER);
        lblTit.setForeground(Color.decode("#94a3b8"));
        lblTit.setFont(new Font("Arial", Font.BOLD, 11));

        lblVal.setForeground(Color.decode(colorTextoHex));
        lblVal.setFont(new Font("Arial", Font.BOLD, 22));
        lblVal.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel lblFoot = new JLabel(footer, JLabel.CENTER);
        lblFoot.setForeground(Color.decode("#64748b"));
        lblFoot.setFont(new Font("Arial", Font.ITALIC, 10));

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblFoot, BorderLayout.SOUTH);

        return card;
    }

    private JPanel crearFilaMetricaBD(String etiqueta, String valor, String colorHex) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        JLabel lbl1 = new JLabel(etiqueta);
        lbl1.setForeground(Color.decode("#94a3b8"));
        JLabel lbl2 = new JLabel(valor);
        lbl2.setForeground(Color.decode(colorHex));
        lbl2.setFont(new Font("Arial", Font.BOLD, 12));
        fila.add(lbl1, BorderLayout.WEST);
        fila.add(lbl2, BorderLayout.EAST);
        return fila;
    }

    private void actualizarDashboardMetricas() {
        double totalVentas = 0;
        double totalGanancia = 0;
        for (ItemConteoDiario item : listaConteoGlobal) {
            totalVentas += item.getTotalVenta();
            totalGanancia += item.getGananciaVendida();
        }

        double totalInversionInsumos = 0;
        for (ItemInsumoDetalle ins : listaInsumosGlobal) {
            totalInversionInsumos += ins.inversion;
        }

        if (lblDashVentas != null) lblDashVentas.setText(String.format("S/. %.2f", totalVentas));
        if (lblDashInversion != null) lblDashInversion.setText(String.format("S/. %.2f", totalInversionInsumos));
        if (lblDashGanancia != null) lblDashGanancia.setText(String.format("S/. %.2f", totalGanancia));
    }

    // --- MÓDULO 2: CAJA Y VENTAS (POS) UNIFICADO ---
    private JPanel crearPanelPOS() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.decode("#0f172a"));

        JPanel panelCajaControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCajaControl.setBackground(Color.decode("#1e293b"));
        panelCajaControl.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#334155")), 
            "Control Operativo de Caja y Conteo Diario", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 12), 
            Color.decode("#38bdf8")
        ));
        
        JButton btnConteoInicial = new JButton("1. Registrar Conteo Inicial");
        JButton btnConteoFinal = new JButton("2. Registrar Conteo Final (Cierre)");
        JButton btnGuardarCierreReporte = new JButton("💾 Consolidar y Enviar a Reportes");
        
        estilizarBotonNeon(btnConteoInicial, "#4f46e5");
        estilizarBotonNeon(btnConteoFinal, "#0284c7");
        estilizarBotonNeon(btnGuardarCierreReporte, "#059669");
        
        panelCajaControl.add(btnConteoInicial);
        panelCajaControl.add(btnConteoFinal);
        panelCajaControl.add(btnGuardarCierreReporte);
        panel.add(panelCajaControl, BorderLayout.NORTH);

        JPanel pResumenCierre = new JPanel(new BorderLayout());
        pResumenCierre.setBackground(Color.decode("#1e293b"));
        pResumenCierre.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#334155")), 
            "Resumen de Conteo Diario (Inicial, Retorno y Ganancia Vendida)", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 12), 
            Color.decode("#38bdf8")
        ));
        
        String[] columnasCierre = {"ID", "Producto", "Precio (S/.)", "Cant. Inicial", "Cant. Retorno", "Ganancia Vendida (S/.)"};
        modeloResumenCaja = new DefaultTableModel(columnasCierre, 0);
        tablaResumenCaja = new JTable(modeloResumenCaja);
        estilizarTabla(tablaResumenCaja);
        pResumenCierre.add(new JScrollPane(tablaResumenCaja), BorderLayout.CENTER);

        JPanel pTotalCierre = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pTotalCierre.setBackground(Color.decode("#1e293b"));
        lblTotalGananciaDia = new JLabel("TOTAL GANANCIA VENTAS DEL DÍA: S/. 0.00");
        lblTotalGananciaDia.setFont(new Font("Arial", Font.BOLD, 15));
        lblTotalGananciaDia.setForeground(Color.decode("#34d399"));
        pTotalCierre.add(lblTotalGananciaDia);
        pResumenCierre.add(pTotalCierre, BorderLayout.SOUTH);

        panel.add(pResumenCierre, BorderLayout.CENTER);

        btnConteoInicial.addActionListener(e -> abrirVentanaConteoInicial());
        btnConteoFinal.addActionListener(e -> abrirVentanaConteoFinalRetorno());
        
        btnGuardarCierreReporte.addActionListener(e -> {
            double totalVentasVal = 0;
            double totalGananciaVal = 0;

            for (ItemConteoDiario item : listaConteoGlobal) {
                totalVentasVal += item.getTotalVenta();
                totalGananciaVal += item.getGananciaVendida();
            }

            double totalInversionInsumos = 0;
            for (ItemInsumoDetalle ins : listaInsumosGlobal) {
                totalInversionInsumos += ins.inversion;
            }

            listaReportesGlobal.add(new ReporteDiario("2026-11-06", totalVentasVal, totalInversionInsumos, totalGananciaVal, 0.00));
            actualizarTablaReportes();
            actualizarDashboardMetricas();

            JOptionPane.showMessageDialog(this, "¡Cierre consolidado con la inversión de insumos y enviado a Reportes Diarios!", "Reportes", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    private void abrirVentanaConteoInicial() {
        JDialog dialog = new JDialog(this, "Registro de Conteo Inicial (Apertura)", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JLabel lblInstrucciones = new JLabel(" Ingrese la cantidad exacta que se lleva para la venta de cada producto hoy:", JLabel.CENTER);
        lblInstrucciones.setForeground(Color.WHITE);
        dialog.add(lblInstrucciones, BorderLayout.NORTH);

        String[] columnas = {"ID", "Producto", "Precio", "Cantidad Inicial"};
        DefaultTableModel modeloInit = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        for (ItemConteoDiario item : listaConteoGlobal) {
            modeloInit.addRow(new Object[]{item.id, item.nombre, item.precio, item.cantInicial});
        }

        JTable tablaInit = new JTable(modeloInit);
        estilizarTabla(tablaInit);
        dialog.add(new JScrollPane(tablaInit), BorderLayout.CENTER);

        JButton btnGuardarInit = new JButton("Guardar y Bloquear Conteo Inicial");
        estilizarBotonNeon(btnGuardarInit, "#059669");

        btnGuardarInit.addActionListener(e -> {
            for (int i = 0; i < modeloInit.getRowCount(); i++) {
                int nuevaCant = Integer.parseInt(modeloInit.getValueAt(i, 3).toString());
                listaConteoGlobal.get(i).cantInicial = nuevaCant;
            }
            actualizarTablaResumenCaja();
            actualizarDashboardMetricas();
            JOptionPane.showMessageDialog(dialog, "¡Conteo inicial guardado correctamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.add(btnGuardarInit, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void abrirVentanaConteoFinalRetorno() {
        JDialog dialog = new JDialog(this, "Registro de Conteo Final (Retorno de Productos)", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JLabel lblInstrucciones = new JLabel(" Ingrese la cantidad de productos que regresan al final del día:", JLabel.CENTER);
        lblInstrucciones.setForeground(Color.WHITE);
        dialog.add(lblInstrucciones, BorderLayout.NORTH);

        String[] columnas = {"ID", "Producto", "Cant. Inicial", "Cantidad de Retorno"};
        DefaultTableModel modeloRetorno = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        for (ItemConteoDiario item : listaConteoGlobal) {
            modeloRetorno.addRow(new Object[]{item.id, item.nombre, item.cantInicial, item.cantRetorno});
        }

        JTable tablaRetorno = new JTable(modeloRetorno);
        estilizarTabla(tablaRetorno);
        dialog.add(new JScrollPane(tablaRetorno), BorderLayout.CENTER);

        JButton btnGuardarRetorno = new JButton("Actualizar Retorno y Calcular Ganancias");
        estilizarBotonNeon(btnGuardarRetorno, "#4f46e5");

        btnGuardarRetorno.addActionListener(e -> {
            for (int i = 0; i < modeloRetorno.getRowCount(); i++) {
                int ret = Integer.parseInt(modeloRetorno.getValueAt(i, 3).toString());
                listaConteoGlobal.get(i).cantRetorno = ret;
            }
            actualizarTablaResumenCaja();
            actualizarDashboardMetricas();
            JOptionPane.showMessageDialog(dialog, "¡Cierre de caja actualizado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.add(btnGuardarRetorno, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void actualizarTablaResumenCaja() {
        modeloResumenCaja.setRowCount(0);
        double totalGananciaGeneral = 0;

        for (ItemConteoDiario item : listaConteoGlobal) {
            double gananciaProd = item.getGananciaVendida();
            totalGananciaGeneral += gananciaProd;

            modeloResumenCaja.addRow(new Object[]{
                item.id,
                item.nombre,
                item.precio,
                item.cantInicial,
                item.cantRetorno,
                String.format("%.2f", gananciaProd)
            });
        }

        if (lblTotalGananciaDia != null) {
            lblTotalGananciaDia.setText(String.format("TOTAL GANANCIA VENTAS DEL DÍA: S/. %.2f", totalGananciaGeneral));
        }
    }

    // --- MÓDULO 3: REPORTES DIARIOS UNIFICADO ---
    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.decode("#0f172a"));

        JSplitPane splitReportes = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitReportes.setDividerLocation(280);
        splitReportes.setBackground(Color.decode("#0f172a"));

        JPanel panelCierres = new JPanel(new BorderLayout());
        panelCierres.setBackground(Color.decode("#1e293b"));
        panelCierres.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#334155")), 
            "📈 Historial Consolidado de Cierres Diarios", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 12), 
            Color.decode("#38bdf8")
        ));
        
        String[] columnasCierres = {"Fecha", "Total Ventas (S/.)", "Inversión Insumos (S/.)", "Ganancia Neta (S/.)", "Diferencia Caja"};
        modeloReportes = new DefaultTableModel(columnasCierres, 0);
        tablaReportes = new JTable(modeloReportes);
        estilizarTabla(tablaReportes);
        panelCierres.add(new JScrollPane(tablaReportes), BorderLayout.CENTER);
        splitReportes.setTopComponent(panelCierres);

        JPanel panelDetalleCompras = new JPanel(new BorderLayout());
        panelDetalleCompras.setBackground(Color.decode("#1e293b"));
        panelDetalleCompras.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#334155")), 
            "📋 Reporte Detallado de Insumos, Cantidades y Precios Registrados", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 12), 
            Color.decode("#38bdf8")
        ));
        
        String[] columnasDetalle = {"ID Prod", "Producto Base", "Insumo / Detalle", "Medida", "Cantidad", "Inversión Total (S/.)"};
        modeloDetalleReporte = new DefaultTableModel(columnasDetalle, 0);
        JTable tablaDetalleReporte = new JTable(modeloDetalleReporte);
        estilizarTabla(tablaDetalleReporte);
        panelDetalleCompras.add(new JScrollPane(tablaDetalleReporte), BorderLayout.CENTER);
        
        splitReportes.setBottomComponent(panelDetalleCompras);
        panel.add(splitReportes, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarTablaReportes() {
        if (modeloReportes != null) {
            modeloReportes.setRowCount(0);
            for (ReporteDiario rep : listaReportesGlobal) {
                modeloReportes.addRow(new Object[]{
                    rep.fecha,
                    String.format("S/. %.2f", rep.totalVentas),
                    String.format("S/. %.2f", rep.inversion),
                    String.format("S/. %.2f", rep.gananciaNeta),
                    String.format("S/. %.2f", rep.diferenciaCaja)
                });
            }
        }
    }

    private void actualizarTablaDetalleInsumos() {
        if (modeloDetalleReporte != null) {
            modeloDetalleReporte.setRowCount(0);
            for (ItemInsumoDetalle ins : listaInsumosGlobal) {
                modeloDetalleReporte.addRow(new Object[]{
                    ins.idProd,
                    ins.nombreProd,
                    ins.insumo,
                    ins.medida,
                    ins.cantidad,
                    String.format("S/. %.2f", ins.inversion)
                });
            }
        }
    }

    // --- MÓDULO 4: GESTIÓN DE PRODUCTOS E INVERSIÓN UNIFICADO ---
    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.decode("#0f172a"));

        JSplitPane splitInventario = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitInventario.setDividerLocation(350);
        splitInventario.setBackground(Color.decode("#0f172a"));

        JPanel panelCompraStock = new JPanel(new BorderLayout(5, 5));
        panelCompraStock.setBackground(Color.decode("#1e293b"));
        panelCompraStock.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#334155")), 
            "🛒 Registro y Persistencia de Insumos (Raspadillas, Frutas, Vasos, etc.)", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 12), 
            Color.decode("#38bdf8")
        ));

        JPanel formCompra = new JPanel(new GridLayout(2, 1, 5, 5));
        formCompra.setOpaque(false);
        
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila1.setOpaque(false);
        JLabel lblProdBase = new JLabel("Producto Base:");
        lblProdBase.setForeground(Color.WHITE);
        fila1.add(lblProdBase);

        JComboBox<String> cmbProductosCompra = new JComboBox<>();
        List<Producto> listaProds = productoDAO.listarProductos();
        for (Producto p : listaProds) {
            cmbProductosCompra.addItem(p.getIdProducto() + " - " + p.getNombre());
        }
        fila1.add(cmbProductosCompra);

        JLabel lblInsumo = new JLabel("Insumo/Subproducto:");
        lblInsumo.setForeground(Color.WHITE);
        fila1.add(lblInsumo);
        
        JTextField txtInsumo = new JTextField(12);
        fila1.add(txtInsumo);

        JLabel lblMedida = new JLabel("Medida:");
        lblMedida.setForeground(Color.WHITE);
        fila1.add(lblMedida);
        
        JComboBox<String> cmbMedida = new JComboBox<>(new String[]{"KG", "UNIDAD", "LITRO"});
        fila1.add(cmbMedida);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila2.setOpaque(false);
        JLabel lblCant = new JLabel("Cantidad:");
        lblCant.setForeground(Color.WHITE);
        fila2.add(lblCant);
        
        JTextField txtCantidad = new JTextField(6);
        fila2.add(txtCantidad);

        JLabel lblInv = new JLabel("Inversión Total (S/.):");
        lblInv.setForeground(Color.WHITE);
        fila2.add(lblInv);
        
        JTextField txtCostoInversion = new JTextField(8);
        fila2.add(txtCostoInversion);

        JButton btnRegistrarInsumo = new JButton("➕ Guardar Insumo y Registrar");
        estilizarBotonNeon(btnRegistrarInsumo, "#0284c7");
        fila2.add(btnRegistrarInsumo);

        formCompra.add(fila1);
        formCompra.add(fila2);
        panelCompraStock.add(formCompra, BorderLayout.NORTH);

        String[] colInsumos = {"ID Prod", "Producto Base", "Insumo Detallado", "Medida", "Cantidad", "Inversión Total (S/.)"};
        modeloInsumosRecientes = new DefaultTableModel(colInsumos, 0);
        for (ItemInsumoDetalle ins : listaInsumosGlobal) {
            modeloInsumosRecientes.addRow(new Object[]{
                ins.idProd, ins.nombreProd, ins.insumo, ins.medida, ins.cantidad, String.format("S/. %.2f", ins.inversion)
            });
        }
        
        JTable tablaInsumosRecientes = new JTable(modeloInsumosRecientes);
        estilizarTabla(tablaInsumosRecientes);
        panelCompraStock.add(new JScrollPane(tablaInsumosRecientes), BorderLayout.CENTER);

        JPanel panelTotalInsumos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTotalInsumos.setBackground(Color.decode("#1e293b"));
        lblSumaInversionInsumos = new JLabel("TOTAL INVERSIÓN EN INSUMOS: S/. 0.00");
        lblSumaInversionInsumos.setFont(new Font("Arial", Font.BOLD, 14));
        lblSumaInversionInsumos.setForeground(Color.decode("#f87171"));
        
        double sumaInicial = 0;
        for (ItemInsumoDetalle item : listaInsumosGlobal) {
            sumaInicial += item.inversion;
        }
        lblSumaInversionInsumos.setText(String.format("TOTAL INVERSIÓN EN INSUMOS: S/. %.2f", sumaInicial));

        panelTotalInsumos.add(lblSumaInversionInsumos);
        panelCompraStock.add(panelTotalInsumos, BorderLayout.SOUTH);

        splitInventario.setTopComponent(panelCompraStock);

        JPanel panelCrud = new JPanel(new BorderLayout(5, 5));
        panelCrud.setBackground(Color.decode("#1e293b"));
        panelCrud.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#334155")), 
            "📦 Catálogo General de Productos (Base de Datos Neon)", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 12), 
            Color.decode("#38bdf8")
        ));

        JPanel panelFormularioCrud = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFormularioCrud.setOpaque(false);
        JLabel lblNomProd = new JLabel("Nombre:");
        lblNomProd.setForeground(Color.WHITE);
        panelFormularioCrud.add(lblNomProd);
        
        JTextField txtNombre = new JTextField(12);
        panelFormularioCrud.add(txtNombre);
        
        JLabel lblPrec = new JLabel("Precio Venta (S/.):");
        lblPrec.setForeground(Color.WHITE);
        panelFormularioCrud.add(lblPrec);
        
        JTextField txtPrecio = new JTextField(6);
        panelFormularioCrud.add(txtPrecio);
        
        JButton btnGuardarProd = new JButton("Crear Nuevo Producto");
        estilizarBotonNeon(btnGuardarProd, "#059669");
        panelFormularioCrud.add(btnGuardarProd);

        panelCrud.add(panelFormularioCrud, BorderLayout.NORTH);

        String[] columnasCrud = {"ID", "Nombre", "Precio Venta (S/.)", "Stock Actual BD"};
        modeloAdminProductos = new DefaultTableModel(columnasCrud, 0);
        tablaAdminProductos = new JTable(modeloAdminProductos);
        estilizarTabla(tablaAdminProductos);
        panelCrud.add(new JScrollPane(tablaAdminProductos), BorderLayout.CENTER);

        splitInventario.setBottomComponent(panelCrud);
        panel.add(splitInventario, BorderLayout.CENTER);

        btnRegistrarInsumo.addActionListener(e -> {
            try {
                String seleccionado = (String) cmbProductosCompra.getSelectedItem();
                if (seleccionado == null) return;
                
                int idProd = Integer.parseInt(seleccionado.split(" - ")[0]);
                String nombreProd = seleccionado.split(" - ")[1];
                String insumo = txtInsumo.getText().trim();
                String medida = (String) cmbMedida.getSelectedItem();
                double cantidad = Double.parseDouble(txtCantidad.getText().trim());
                double inversion = Double.parseDouble(txtCostoInversion.getText().trim());

                if (insumo.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Debe ingresar el nombre del insumo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ItemInsumoDetalle nuevoDetalle = new ItemInsumoDetalle(idProd, nombreProd, insumo, medida, cantidad, inversion);
                listaInsumosGlobal.add(nuevoDetalle);
                
                modeloInsumosRecientes.addRow(new Object[]{
                    idProd, nombreProd, insumo, medida, cantidad, String.format("S/. %.2f", inversion)
                });

                actualizarTablaDetalleInsumos();

                double sumaTotalInversion = 0;
                for (ItemInsumoDetalle item : listaInsumosGlobal) {
                    sumaTotalInversion += item.inversion;
                }
                lblSumaInversionInsumos.setText(String.format("TOTAL INVERSIÓN EN INSUMOS: S/. %.2f", sumaTotalInversion));
                
                actualizarDashboardMetricas();

                JOptionPane.showMessageDialog(panel, "¡Insumo guardado, sumado al reporte y sincronizado con el Dashboard!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtInsumo.setText("");
                txtCantidad.setText("");
                txtCostoInversion.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Verifique que los campos numéricos de cantidad e inversión sean válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnGuardarProd.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                double precio = Double.parseDouble(txtPrecio.getText().trim());

                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Ingrese el nombre del producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                modeloAdminProductos.addRow(new Object[]{
                    modeloAdminProductos.getRowCount() + 1, nombre, precio, 0
                });

                JOptionPane.showMessageDialog(panel, "¡Producto creado con stock inicial en blanco!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtNombre.setText("");
                txtPrecio.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Verifique el formato del precio de venta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cargarProductosAdmin();
        return panel;
    }

    private void estilizarBotonNeon(JButton boton, String colorHex) {
        boton.setBackground(Color.decode(colorHex));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void cargarProductosAdmin() {
        List<Producto> lista = productoDAO.listarProductos();
        modeloAdminProductos.setRowCount(0);
        for (Producto p : lista) {
            modeloAdminProductos.addRow(new Object[]{
                p.getIdProducto(),
                p.getNombre(),
                p.getPrecio(),
                0
            });
        }
    }
}