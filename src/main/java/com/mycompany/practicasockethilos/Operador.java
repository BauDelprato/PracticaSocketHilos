package com.mycompany.practicasockethilos;

import java.util.concurrent.*;

public class Operador {

    private double mejorOferta = 0;
    private String mejorPostor = "Nadie";
    private String objeto = "";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timer;
    private ScheduledFuture<?> aviso;

    // comunicación con el servidor
    public interface Listener {
        void onMensaje(String msg);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void enviar(String msg) {
        if (listener != null) {
            listener.onMensaje(msg);
        }
    }

// lógicas de subasta
    public synchronized void iniciarSubasta(String obj, String usuario) {
        if (!objeto.equals("")) {
            enviar("Ya hay una subasta activa: " + objeto);
            return;
        }

        objeto = obj;
        mejorOferta = 0;
        mejorPostor = "Nadie";

        enviar(usuario + " está subastando: " + objeto);

        reiniciarTimer();
    }


    public synchronized void ofertar(String usuario, double monto) {

        if (objeto.equals("")) {
            enviar("No hay subasta activa");
            return;
        }

        if (monto > mejorOferta) {
            mejorOferta = monto;
            mejorPostor = usuario;

            enviar("Nueva mejor oferta: " + monto + " de " + usuario + " por " + objeto);

            reiniciarTimer();
        } else {
            enviar("Tu oferta es menor a la actual (" + mejorOferta + ")");
        }
    }

    private void reiniciarTimer() {

        if (timer != null && !timer.isDone()) timer.cancel(false);
        if (aviso != null && !aviso.isDone()) aviso.cancel(false);

        aviso = scheduler.schedule(() -> {
            enviar("Quedan 5 segundos para ofertar!!!!");
        }, 10, TimeUnit.SECONDS);

        timer = scheduler.schedule(() -> {
            cerrarSubasta();
        }, 15, TimeUnit.SECONDS);
    }

    private synchronized void cerrarSubasta() {

        if (!objeto.equals("")) {
            enviar("SUBASTA TERMINADA");
            enviar("Objeto: " + objeto);
            enviar("Ganador: " + mejorPostor + " con $" + mejorOferta);

            objeto = "";
            mejorOferta = 0;
            mejorPostor = "Nadie";
        }
    }

    public String getObjeto() {
        return objeto;
    }
}