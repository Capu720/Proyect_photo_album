/*
ALBUM DE FOTOS DIGITAL EN JAVA
2CM1 PROGRAMACION ORIENTADA A OBJETOS

Morales Hernández Carlos Jesús
Núñez González Angel Daniel
Sánchez Palacios Juan Manuel
*/


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.net.*;



public class Servidor_album{

    public static void main(String[] Args){
        server principal = new server();

    }

}

class server implements Runnable{

    public server(){
        //Creamos un hilo para el server Socket
        Thread escuchar = new Thread(this);
        escuchar.start();
        //Colocamos una alerta cuando comience a correr el servidor
        Alerta corriendo = new Alerta("Servidor Activo!");
    }


    public void run(){
        
        while(true){
            
            try {
                //Servidor de parte del cliente para rexibir el texto de regreso
                ServerSocket Servidor_cliente = new ServerSocket(8080);
                Socket Cliente = new Socket();
                paquete recibido = new paquete();
    
                while(true){
                    //Recibimos los datos de cualquiera de los dos clientes
                    Cliente = Servidor_cliente.accept();
                    ObjectInputStream Entrada = new ObjectInputStream(Cliente.getInputStream());
                    recibido = (paquete) Entrada.readObject();
                    Cliente.close();
                    Alerta ok = new Alerta("Imagen Recibida");
                   
                    try {
                        //Mandamos datos a los clientes que configuremos con sus respectivas ip
                        Socket miSocket = new Socket("192.168.0.7", 5700);
                        ObjectOutputStream informacion = new ObjectOutputStream(miSocket.getOutputStream());
                        informacion.writeObject(recibido);
                        miSocket.close();

                        Socket miSocket2 = new Socket("192.168.0.9", 8070);
                        ObjectOutputStream informacion2 = new ObjectOutputStream(miSocket2.getOutputStream());
                        informacion2.writeObject(recibido);
                        miSocket2.close();

                        //Ponemos un cuadro diciendo que la imagen se ha enviado
                        Alerta enviado = new Alerta("Imagen Enviada a los clientes");


                       //En caso de errores igual aparece un cuadro de texto 
                    } catch (UnknownHostException e1) {
                        Alerta noEncontrado = new Alerta("Cliente no disponible");
                    } catch (IOException e1){
                        Alerta error = new Alerta("Error!");
                    } 


                }
                //Manejo de excepciones
            } catch (IOException e ) {
                System.out.println(e.getMessage());
            } catch (ClassNotFoundException f){
                System.out.println(f.getMessage());
            }

            
        }	

    }
}
