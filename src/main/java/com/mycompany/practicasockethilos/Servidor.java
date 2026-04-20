
package com.mycompany.practicasockethilos;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Servidor {

    static List<HiloCliente> clientes = new ArrayList<>();
    static int contadorClientes = 1;


    static double mejorOferta = 0;
    static String mejorPostor = "Nadie";
    static String objeto = "objetoo";

    public static void main(String[] args) throws IOException {

        ServerSocket servidor = new ServerSocket(5000);
        System.out.println("Servidor iniciado");

        while (true) {
            Socket sc = servidor.accept();

            String nombre = "Cliente " + contadorClientes++;
            HiloCliente hilo = new HiloCliente(sc, nombre);

            clientes.add(hilo);
            hilo.start();

            System.out.println(nombre + " conectado");
        }
    }

    // manda mensajes a todos los clientes
    public static void broadcast(String msg) {
        for (HiloCliente c : clientes) {
            c.enviar(msg);
        }
    }
}
