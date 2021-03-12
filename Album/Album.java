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
import java.awt.image.*;
import javax.swing.event.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;


public class Album{
    public static void main(String[] Args){
        ContA principal = new ContA();
    }
}


class ContA extends JFrame{

    public ContA(){
        ImageIcon[] imagenes = new ImageIcon[50];
        JButton b = new JButton();
        //Obtiene todas las imagenes dentro de la carpeta
        Leer contenido = new Leer();
        imagenes = contenido.getImagenes();

        setLayout(new GridLayout(1,2));
        setBounds(300,0,900,600);
        setTitle("ALBUM");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Añadir lamina derecha e izquierda, mandarle a ambas las dos variables
        Lamina_izq izq = new Lamina_izq(imagenes, b);
        Lamina_der der = new Lamina_der(imagenes, b);
        add(izq);
        add(der);
    }
}


class Lamina_izq extends JPanel implements ActionListener{
    public ImageIcon[] imagenes = new ImageIcon[50];
    public JButton b = new JButton();
    public JButton[] botones = new JButton[11];
    private int i = 0;
    private int posicion, aux = 1;



    public Lamina_izq(ImageIcon[] imagenes, JButton b){
        setLayout(new GridLayout(5,2));
        this.imagenes = imagenes;
        this.b = b;

        //Añadimos los 8 botones a la lamina
        for(i = 0; i<8; i++){
            botones[i] = new JButton();
            botones[i].setIcon(imagenes[i]);
            add(botones[i]);
            botones[i].addActionListener(this);
        }
        //Creamos los botones de atras y adelante
        botones[9] = new JButton("Atras");
        botones[10] = new JButton("Adelante");

        add(botones[9]);
        add(botones[10]);

        botones[9].addActionListener(this);
        botones[10].addActionListener(this);

        //Imagen para inicializar el modo preview
        b.setIcon(new Escalar().getEscala(imagenes[0],300,300));

    }

    public void actionPerformed(ActionEvent e){
        JButton pulsado = (JButton) e.getSource();
        int total = 0;
        posicion = aux;

        while(imagenes[total] != null){
            total++;
        }

        //Si se pulsa adelante
        if(pulsado == botones[10]){
            posicion = aux;
                //cambiamos de imagenes
            if((aux + 7) < total){
                for(i = 0; i<8; i++){
                    botones[i].setIcon(imagenes[posicion]);
                    posicion++;
                }
                aux++;
            }   
            posicion = 0;
        }
        //Si se pulsa atras
        else if(pulsado == botones[9]){
            //cambiamos de imagenes
            if(aux > 1){
                posicion = aux-2;
                for(i = 0; i<8; i++){
                    botones[i].setIcon(imagenes[posicion]);
                    posicion++;
                }
                aux--;
                posicion = 0;
            }   
        }
        else{
            b.setIcon(new Escalar().getEscala((ImageIcon)pulsado.getIcon(),300,300));
        }
    }
}


//Lamina que contiene dos laminas mas, Preview y Servidor
class Lamina_der extends JPanel{
    public ImageIcon[] imagenes = new ImageIcon[50];
    public JButton b = new JButton();
    
    public Lamina_der(ImageIcon[] imagenes, JButton b){
        setLayout(new GridLayout(2,1));
        this.imagenes = imagenes;
        this.b = b;
        //Meter aqui las dos laminas, mandarles las imagenes y el boton que se ha pulsado a la de preview
        Preview previa = new Preview(imagenes, b);
        Servidor serv = new Servidor(imagenes);
        add(previa);
        add(serv);
    }

}


class Preview extends JPanel implements ActionListener{
    public ImageIcon[] imagenes = new ImageIcon[50];
    public JButton b = new JButton();
    public JButton M_dia = new JButton("Modo Diapositiva");

    public Preview(ImageIcon[] imagenes, JButton b){
        setLayout(new BorderLayout());
        this.imagenes = imagenes;
        this.b = b;
        //b.setIcon(new Escalar().getEscala(imagenes[0], 300,300));
        add(b, BorderLayout.CENTER);
        add(M_dia, BorderLayout.SOUTH);
        M_dia.addActionListener(this);
    }


    public void actionPerformed(ActionEvent e){
        //Abrir un nuevo frame con el modo de diapositiva
        if(e.getSource() == M_dia)
            new Diapositiva(imagenes);
    }
}

//GUARDAR ESTO PARA EL FINAL
class Servidor extends JPanel implements ActionListener, Runnable{
    public ImageIcon[] imagenes = new ImageIcon[50];
    public JButton subir = new JButton("Subir imagen");
    public String direccion = new String("");
    public JTextArea texto = new JTextArea();
    public int total = 0;


    public Servidor(ImageIcon[] imagenes){
        setLayout(new BorderLayout());
        this.imagenes = imagenes;
        subir.addActionListener(this);
        add(texto, BorderLayout.CENTER);
        add(subir, BorderLayout.SOUTH);
        //Insertar el hilo aqui como escucha
        Thread escucha = new Thread(this);
        escucha.start();
    }


    public void actionPerformed(ActionEvent e){
        //Guardamos la imagen de la direccion proporcionada dentro del arreglo de imagenes
        direccion = texto.getText();
        direccion = direccion.replace("\\", "/");

        total = 0;
        while(imagenes[total] != null){
            total++;
        }

        imagenes[total] = new ImageIcon(direccion);
        texto.setText(""); //Limpiamos la casilla
        //Creamos un objeto que contenga la imagen y que podamos enviar y recibir
        paquete enviar = new paquete();
        enviar.setImagen(imagenes[total]);

        //Abrimos socket para enviar al servidor
        try {
            Socket mandar = new Socket("192.168.0.9", 8080);
            ObjectOutputStream salida = new ObjectOutputStream(mandar.getOutputStream());
            salida.writeObject(enviar);
            mandar.close();
            Alerta completado = new Alerta("Imagen mandada con exito!");
            
        } catch (UnknownHostException e1) {
            Alerta completado = new Alerta("Servidor no encontrado!");
        } catch (IOException e1){
            Alerta completado = new Alerta("Error al enviar");
            e1.printStackTrace();
        } 
        
    }

    public void run(){
		
		try {
			ServerSocket Servidor_cliente = new ServerSocket(25856);
			Socket Cliente = new Socket();
			paquete respuesta = new paquete();

			while(true){
				Cliente = Servidor_cliente.accept();
				ObjectInputStream Entrada = new ObjectInputStream(Cliente.getInputStream());
				respuesta = (paquete) Entrada.readObject();
                Cliente.close();
                
                total = 0;
                while(imagenes[total] != null){
                    total++;
                }
                imagenes[total] = respuesta.getImagen();
				
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}	

}
//************************************************************************
//Frame extra para el modo de diapositivas

class Diapositiva extends JFrame{
    public JButton[] acciones = new JButton[6];
    public static String[] Comentarios = new String[50];
    public static JTextArea Casilla = new JTextArea(10,10);
    public static JTextField Historial = new JTextField(30);

    public Diapositiva(ImageIcon[] imagenes){
        setLayout(new BorderLayout());
        setBounds(100,100,1200,600);
        setTitle("MODO DIAPOSITIVAS");
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        for(int i = 0; i<6; i++){
            acciones[i] = new JButton();
        }
        //Añadir lamina de la foto en grande
        //Añadir lamina de los botones
        //Nota: A ambas pasar por referencia todas las variables e imagenes
        Imagen_D arriba = new Imagen_D(acciones, Comentarios, Casilla, Historial, imagenes);
        Interfaz_D abajo = new Interfaz_D(acciones, Comentarios, Casilla, Historial);
        add(arriba, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

    }
}

class Imagen_D extends JPanel implements ActionListener, Runnable, ChangeListener{
    public JButton Imagen = new JButton();
    public JButton[] acciones = new JButton[6];
    public static String[] Comentarios = new String[50];
    public String texto = new String("");
    public static JTextArea Casilla = new JTextArea(5,5);
    public static JTextField Historial = new JTextField(30);
    public JSlider tam = new JSlider(100,2000,500);
    public ImageIcon[] imagenes = new ImageIcon[50]; 
    public int posicion, total = 0, descarga = 30;
    public boolean corriendo = false, iniciar = false;
    public Thread mover;

    public Imagen_D(JButton[] acciones, String[] Comentarios, JTextArea Casilla, JTextField Historial,ImageIcon[] imagenes){
        setLayout(new BorderLayout());
        this.imagenes = imagenes;
        this.acciones = acciones;
        this.Comentarios = Comentarios;
        this.Casilla = Casilla;
        this.Historial = Historial;
       
        //Registramos en el escucha los botones
        for(int i = 0; i<6; i++){
            acciones[i].addActionListener(this);
        }

        //Asignamos memoria donde guardaremos los comentarios
        for(int i = 0; i<50; i++){
            Comentarios[i] = new String("");
        }

        //Añadimos boton y JSlider
        Imagen.setIcon(new Escalar().getEscala(imagenes[0],500,500));
        add(Imagen, BorderLayout.CENTER);
        add(tam, BorderLayout.SOUTH);
        tam.addChangeListener(this);
    }

    public void actionPerformed(ActionEvent e){
        JButton pulsado = (JButton)e.getSource();
        mover = new Thread(this);
        total = 0;

        while(imagenes[total] != null){
            total++;
        }
        

        if(pulsado == acciones[0] & posicion > 0){
            posicion--;
            Imagen.setIcon(new Escalar().getEscala(imagenes[posicion],500,500));
            Casilla.setText("");
            Casilla.append(Comentarios[posicion]);
            //Meter comentarios
        }
        else if(pulsado == acciones[1] & posicion < total-1 ){
            posicion++;
            Imagen.setIcon(new Escalar().getEscala(imagenes[posicion],500,500));
            Casilla.setText("");
            Casilla.append(Comentarios[posicion]);
            //Meter el cambio de comentarios
        }else if(pulsado == acciones[2]){
            corriendo = true;
            mover.start();
        }
        else if(pulsado == acciones[3]){
            corriendo = false;
        }
        else if(pulsado == acciones[4]){
            Comentarios[posicion] += Historial.getText() + "\n";
            Casilla.append(Historial.getText() + "\n");
            Historial.setText("");
            Alerta completado = new Alerta("Comentario Subido!");
        }
        else if(pulsado == acciones[5]){
            //Creamos una BufferedImage a partir de nuestro icono para despues descargarla
            try{
            //String para especificar el directorio de salida
            String salida = new String("");
            salida = System.getProperty("user.dir");
            salida += "/Descargas/Imagen" + descarga;
            salida = salida.replace("\\", "/");

            BufferedImage bi = new BufferedImage(
            imagenes[posicion].getIconWidth(),
            imagenes[posicion].getIconHeight(),
            BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.createGraphics();
            imagenes[posicion].paintIcon(null, g, 0,0);
            g.dispose();
            File file = new File(salida +".jpg");
            ImageIO.write(bi, "JPG", file);
            descarga++;
            Alerta completado = new Alerta("Descarga Realizada!");
            }
            catch (IOException f) {
            }
        }
    }

    public void run(){
        while((posicion < total) && corriendo == true){
                try{
                    Imagen.setIcon(new Escalar().getEscala(imagenes[posicion],500,500));
                    Casilla.setText("");
                    Casilla.append(Comentarios[posicion]);
                    Thread.sleep(700);
                    posicion++;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
       }
       mover.interrupt();
    }
    

    public void stateChanged(ChangeEvent e){
        Imagen.setIcon(new Escalar().getEscala(imagenes[posicion], tam.getValue(), tam.getValue()));
    }
    

}

class Interfaz_D extends JPanel{

    public Interfaz_D(JButton[] acciones, String[] Comentarios, JTextArea Casilla, JTextField Historial){
        //Solo nos sirve como interfaz y agragamos cada boton (Contenedor)
        setLayout(new GridLayout(1,8));
        acciones[0].setText("Atras");
        acciones[1].setText("Adelante");
        acciones[2].setText("Iniciar");
        acciones[3].setText("Detener");
        acciones[4].setText("Subir Comentario");
        acciones[5].setText("Descargar");

        for(int i = 0; i<4; i++){
            add(acciones[i]);
        }        

        add(Historial);
        add(Casilla);
        add(acciones[4]);
        add(acciones[5]);
    
    }

}




//************************************************************************
//Clases para reutilizar

//Lee todas las imagenes dentro de la carpeta
class Leer{
    private ImageIcon[] imagenes = new ImageIcon[50];
    
    public Leer(){
        String dir_actual, direccion = new String();
        int i = 0;


        dir_actual = System.getProperty("user.dir");
        dir_actual += "/Imagenes/";
        dir_actual = dir_actual.replace("\\", "/");
    
        File folder = new File(dir_actual);
        File[] listOfFiles = folder.listFiles();

        //Guardamos todas las imagenes que se encuentren en el folder imagenes
        //Dentro del arreglo de imagenes
        for (File file : listOfFiles) {
            if (file.isFile()) {
                direccion += dir_actual + file.getName();
                imagenes[i] = new ImageIcon(direccion);   
                //Solo los primeros 8 botones los agregamos al panel de vista previa
                direccion = "";
                i++;
            }	
        }
    }

    public ImageIcon[] getImagenes(){
        return imagenes;
    }
}


class Escalar{
    ImageIcon salida;

    public ImageIcon getEscala(ImageIcon a, int x, int y){
        salida = a;
        Image dab = salida.getImage();
        Image escalada = dab.getScaledInstance(x,y,Image.SCALE_SMOOTH);
        salida = new ImageIcon(escalada);
        return salida;
    }
}

//Clase para mandar una imagen empaquetada por la red
class paquete{
    ImageIcon imagen;
    public paquete(){
    }
    public void setImagen(ImageIcon a){
        imagen = a;
    }
    public ImageIcon getImagen(){
        return imagen;
    }
}

//Aparece cuando:
//Se sube una imagen
//Se descarga una imagen
//Hubo un error al subir la imagen
class Alerta extends JFrame{
    private JLabel texto;

    public Alerta(String a){
        setLayout(new BorderLayout());
        setBounds(300,300,300,100);
        setTitle("");
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        texto = new JLabel("     " + a);
        add(texto, BorderLayout.NORTH);
    }
}


