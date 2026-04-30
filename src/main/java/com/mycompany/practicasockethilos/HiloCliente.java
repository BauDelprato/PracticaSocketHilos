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
            if (Servidor.objeto.equals("")) {
                enviar("NO hay un objeto en subasta, podes subastar algo!!!");
            } else {
                enviar("Objeto esubastandose: " + Servidor.objeto);
            }
            enviar("Comandos: MENSAJE texto | OFERTAR monto | LISTAR | SUBASTAR | SALIR");

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

                if (Servidor.objeto.equals("")) {
                    enviar("No hay ninguna subasta activa :(");
                    return;
                } else {

                    double oferta = Double.parseDouble(msg.split(" ")[1]);

                    synchronized (Servidor.class) {
                        if (oferta > Servidor.mejorOferta) {

                            Servidor.reiniciarTimer(); //reinicia el countdown

                            Servidor.mejorOferta = oferta;
                            Servidor.mejorPostor = nombre;

                            Servidor.broadcast("Nueva mejor oferta: " + oferta + " de " + nombre + " por " + Servidor.objeto);
                            //tomar tiempo 10s
                        } else {
                            enviar("Tu oferta es menor a la actual (" + Servidor.mejorOferta + ")");
                        }
                    }

                }

            } else if (msg.equals("LISTAR")) {

                String lista = "Clientes conectados:\n";
                for (HiloCliente c : Servidor.clientes) {
                    lista += c.nombre + "\n";
                }
                enviar(lista);

            } else if (msg.equals("SUBASTAR")) {
                if (Servidor.objeto.equals("")) {

                    enviar("Ingrese el objeto a subastar");
                    String objetoSubastado = in.readUTF();
                    Servidor.objeto = objetoSubastado;
                    enviar(nombre + " está subastando: " + Servidor.objeto);

                } else {

                    enviar("No se puede subastar tu objeto, actualmente se está subastando: " + Servidor.objeto);

                }
            } else {
                enviar("Comando no reconocido");
            }

        } catch (Exception e) {
            enviar("Error procesando comando");
        }
    }

    //habla solo con el cliente del hilo
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
