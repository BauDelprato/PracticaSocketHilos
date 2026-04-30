package com.mycompany.practicasockethilos;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {

    static List<HiloCliente> clientes = new ArrayList<>();
    static Operador operador = new Operador();
    
    public static void main(String[] args) throws IOException {

        ServerSocket servidor = new ServerSocket(5000);
        System.out.println("Servidor iniciado");

        operador.setListener(msg -> {
            broadcast(msg);
        });

        //while para recibir clientes y crear un hilo
        while (true) {
            Socket sc = servidor.accept();

            DataInputStream in = new DataInputStream(sc.getInputStream());
            String nombre = in.readUTF();

            HiloCliente hilo = new HiloCliente(sc, nombre);

            clientes.add(hilo);
            hilo.start();
            
            System.out.println(nombre + " conectado");
            broadcast(nombre + " conectado");
            
        }
    }

    // manda mensajes a todos los clientes
    public static void broadcast(String msg) {
        for (HiloCliente c : clientes) {
            c.enviar(msg);
        }
    }
    
}
