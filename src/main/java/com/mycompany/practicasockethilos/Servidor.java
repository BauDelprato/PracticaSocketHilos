package com.mycompany.practicasockethilos;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {

    static List<HiloCliente> clientes = new ArrayList<>();
    //static int contadorClientes = 1;

    static double mejorOferta = 0;
    static String mejorPostor = "Nadie";
    static String objeto = "";
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> timerSubasta;
    static ScheduledFuture<?> aviso5Segundos;

    public static void main(String[] args) throws IOException {

        ServerSocket servidor = new ServerSocket(5000);
        System.out.println("Servidor iniciado");

        //while para recibir clientes y crear un hilo
        while (true) {
            Socket sc = servidor.accept();

            DataInputStream in = new DataInputStream(sc.getInputStream());
            String nombre = in.readUTF();

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

    public static synchronized void reiniciarTimer() {

        // cancela timer anterior
        if (timerSubasta != null && !timerSubasta.isDone()) {
            timerSubasta.cancel(false);
        }
        
        if (aviso5Segundos != null && !aviso5Segundos.isDone()) {
            aviso5Segundos.cancel(false);
        }

        // aviso a los 5 segundos
        aviso5Segundos = scheduler.schedule(() -> {
            broadcast("Quedan 5 segundos para ofertar!!!!");
        }, 5, TimeUnit.SECONDS);

        // programa cierre en 10 segundos
        timerSubasta = scheduler.schedule(() -> {
            cerrarSubasta();
        }, 10, TimeUnit.SECONDS);

        

    }

    public static synchronized void cerrarSubasta() {

        if (!objeto.equals("")) {
            broadcast("SUBASTA TERMINADA");
            broadcast("Objeto: " + objeto);
            broadcast("Ganador: " + mejorPostor + " con $" + mejorOferta);

            // resetear todo
            objeto = "";
            mejorOferta = 0;
            mejorPostor = "Nadie";
        }
    }
}
