
package com.mycompany.practicasockethilos;

import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {

    public static void main(String[] args) throws IOException {

        Socket sc = new Socket("127.0.0.1", 5000);

        DataInputStream in = new DataInputStream(sc.getInputStream());
        DataOutputStream out = new DataOutputStream(sc.getOutputStream());

        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        
        new Thread(() -> {
            try {
                while (true) {
                    System.out.println(in.readUTF());
                }
            } catch (IOException e) {
                System.out.println("Desconectado del servidor");
            }
        }).start();

        
        while (true) {
            String msg = teclado.readLine();
            out.writeUTF(msg);
        }
    }
}
