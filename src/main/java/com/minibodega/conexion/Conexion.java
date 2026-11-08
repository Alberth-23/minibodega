package com.minibodega.conexion;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    private static final String URL = "jdbc:postgresql://ep-sweet-fog-bd6b1h5i-pooler.c-8.us-east-2.aws.neon.tech/neondb?user=neondb_owner&password=npg_r4oibaDYyZ7E&sslmode=require&channelBinding=require";

    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("¡Conexión exitosa a Neon PostgreSQL!");
            return conn;
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        conectar();
    }
}