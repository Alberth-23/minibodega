package com.minibodega;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.minibodega.vista.POSFrame;

public class App {
    public static void main(String[] args) {
        try {
            // Aplica el estilo visual nativo del sistema operativo (Windows, Linux, etc.)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializa y despliega la vista principal en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            POSFrame frame = new POSFrame();
            frame.setVisible(true);
        });
    }
}