package com.mycompany.practicasockethilos;

import java.io.*;
import java.net.*;

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
            enviar(""); //NO ELIMINAR ESTE ENVIAR (se rompe el primer cliente uwu)
            enviar("Bienvenido " + nombre);
            
            if (Servidor.operador.getObjeto().equals("")) {
                enviar("NO hay un objeto en subasta, podes subastar algo!!!");
            } else {
                enviar("Objeto subastandose: " + Servidor.operador.getObjeto());
            }

            

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
                Servidor.operador.ofertar(nombre, oferta);

            } else if (msg.equals("LISTAR")) {

                String lista = "Clientes conectados:\n";
                for (HiloCliente c : Servidor.clientes) {
                    lista += c.nombre + "\n";
                }
                enviar(lista);

            } else if (msg.equals("SUBASTAR")) {

                if (Servidor.operador.getObjeto().equals("")) {

                    enviar("Ingrese el objeto a subastar");
                    String objetoSubastado = in.readUTF();

                    Servidor.operador.iniciarSubasta(objetoSubastado, nombre);

                } else {
                    enviar("Ya hay una subasta activa: " + Servidor.operador.getObjeto());
                }

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
