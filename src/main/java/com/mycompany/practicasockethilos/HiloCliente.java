package com.mycompany.practicasockethilos;

import java.io.*;
import java.net.*;
import java.util.*;

class HiloCliente extends Thread {

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String nombre;
    boolean activo = true;

    public HiloCliente(Socket socket, String nombre) throws IOException {
        this.socket = socket;
        this.nombre = nombre;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        try {
            enviar("Bienvenido " + nombre);
            enviar("Comandos: MENSAJE texto | OFERTAR monto | LISTAR | SALIR");

            while (activo) { 
                String mensaje = in.readUTF();
                System.out.println(nombre + ": " + mensaje);

                
                if (mensaje.equalsIgnoreCase("SALIR")) {
                    enviar("Te desconectaste del servidor");
                    desconectar();
                    break;
                }

                procesarMensaje(mensaje);
            }

        } catch (IOException e) {
            System.out.println(nombre + " desconectado inesperadamente");
            desconectar(); 
        }
    }

    public void procesarMensaje(String msg) {

        try {
            if (msg.startsWith("MENSAJE")) {

                Servidor.broadcast(nombre + ": " + msg.substring(8));

            } else if (msg.startsWith("OFERTAR")) {

                double oferta = Double.parseDouble(msg.split(" ")[1]);

                synchronized (Servidor.class) {
                    if (oferta > Servidor.mejorOferta) {
                        Servidor.mejorOferta = oferta;
                        Servidor.mejorPostor = nombre;

                        Servidor.broadcast("Nueva mejor oferta: " + oferta + " por " + nombre);
                    } else {
                        enviar("Tu oferta es menor a la actual (" + Servidor.mejorOferta + ")");
                    }
                }

            } else if (msg.equals("LISTAR")) {

                String lista = "Clientes conectados:\n";
                for (HiloCliente c : Servidor.clientes) {
                    lista += c.nombre + "\n";
                }
                enviar(lista);

            } else {
                enviar("Comando no reconocido");
            }

        } catch (Exception e) {
            enviar("Error procesando comando");
        }
    }

    public void enviar(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void desconectar() {
        try {
            activo = false;

            Servidor.clientes.remove(this);
            Servidor.broadcast(nombre + " se ha desconectado");

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}