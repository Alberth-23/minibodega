package com.minibodega.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.minibodega.conexion.Conexion; // Asumiendo que esta clase maneja tu conexión a Neon
import com.minibodega.vista.POSFrame.ItemInsumoDetalle;

public class InsumoDAO {

    public boolean registrarInsumo(int idProd, String nombreInsumo, String medida, double cantidad, double inversion) {
        String sql = "INSERT INTO insumos_inversion (id_producto, nombre_insumo, medida, cantidad, inversion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProd);
            pstmt.setString(2, nombreInsumo);
            pstmt.setString(3, medida);
            pstmt.setDouble(4, cantidad);
            pstmt.setDouble(5, inversion);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ItemInsumoDetalle> listarInsumos() {
        List<ItemInsumoDetalle> lista = new ArrayList<>();
        String sql = "SELECT i.id_producto, p.nombre, i.nombre_insumo, i.medida, i.cantidad, i.inversion " +
                     "FROM insumos_inversion i JOIN productos p ON i.id_producto = p.id_producto ORDER BY i.id_insumo DESC";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ItemInsumoDetalle(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("nombre_insumo"),
                    rs.getString("medida"),
                    rs.getDouble("cantidad"),
                    rs.getDouble("inversion")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}