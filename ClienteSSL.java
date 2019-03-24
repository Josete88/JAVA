import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;

import javax.net.ssl.*;

public class ClienteSSL {
	private static String ip, puerto;
	private String rutaAlmacen,claveAlmacen,ipServidor;
	private int puertoServidor;
	private SSLSocket clienteSSL;
	private DataInputStream bufferDeEntrada = null;
    private DataOutputStream bufferDeSalida = null;
    Scanner teclado = new Scanner(System.in);
    final String COMANDO_TERMINACION = "salir()";
   
	
	public ClienteSSL(String rutaAlmacen, String claveAlmacen, String ipServidor, int puertoServidor) {
		this.rutaAlmacen=rutaAlmacen;
		this.claveAlmacen=claveAlmacen;
		this.ipServidor=ipServidor;
		this.puertoServidor=puertoServidor;
	}
	
	//Método que inicia la conexión con el servidor
	 public void levantarConexion(String ip, int puerto) {
	        try {
	        	this.clienteSSL=obtenerSocket(ip, puerto);
	            mostrarTexto("Conectado a :" + clienteSSL.getInetAddress().getHostName());
	        } catch (Exception e) {
	            mostrarTexto("Excepción al levantar conexión: " + e.getMessage());
	            System.exit(0);
	        }
	    }
	 
	 //Muestra mensajes internos para el cliente
	 public static void mostrarTexto(String s) {
	        System.out.println(s);
	    }
	 
	 
	 //Abrimos los flujos
	 public void abrirFlujos() {
	        try {
	            bufferDeEntrada = new DataInputStream(clienteSSL.getInputStream());
	            bufferDeSalida = new DataOutputStream(clienteSSL.getOutputStream());
	            bufferDeSalida.flush();
	        } catch (IOException e) {
	            mostrarTexto("Error en la apertura de flujos");
	        }
	    }
	 
	 
	 //Método para enviar datos al servidor
	 public void enviar(String s) {
		 	Thread h1=new Thread(new Runnable() {

				@Override
				public void run() {
					 try {
	            bufferDeSalida.writeUTF(s);
	            bufferDeSalida.flush();
	        } catch (IOException e) {
	            mostrarTexto("IOException on enviar");
	        }
					
				}
		 		
		 	});
		 	h1.start();
	       
	    }
	 
	 //Método para cerrar la conexión (no se usa)
	    public void cerrarConexion() {
	        try {
	            bufferDeEntrada.close();
	            bufferDeSalida.close();
	            clienteSSL.close();
	            mostrarTexto("Sesión finalizada");
	        } catch (IOException e) {
	            mostrarTexto("IOException on cerrarConexion()");
	        }finally{
	            System.exit(0);
	        }
	    }
	    
	    //Método que ejecuta todo lo necesario para establecer la conexión
	    public void ejecutarConexion(String ip, int puerto) {
	        Thread hilo = new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    levantarConexion(ip, puerto);
	                    abrirFlujos();
	                    recibirDatos();
	                    escribirDatos();
	                } finally {
	                    //cerrarConexion();
	                }
	            }
	        });
	        hilo.start();
	    }

	    
	    //Método que se encarga de recibir los datos
	    public void recibirDatos() {
	        String st = "";
	        try {
	            do {
	                st = (String) bufferDeEntrada.readUTF();
	                mostrarTexto("\n[Servidor] => " + st);
	                System.out.print("\n[Usted] => ");
	            } while (true);
	            //clienteSSL.close();
	        } catch (IOException e) {}
	    }
	    
	    //Método que envía datos al servidor
	    public void escribirDatos() {
	        String entrada = "";
	        while (true) {
	            System.out.print("[Usted] => ");
	            entrada = teclado.nextLine();
	            if(entrada.length() > 0)
	                enviar(entrada);
	        }
	    }
	    
	    

	
	public SSLSocket obtenerSocket(String ip, int puerto) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException { 
		
		FileInputStream ficheroAlmacen=new FileInputStream(this.rutaAlmacen);
		KeyStore almacenClaves=KeyStore.getInstance(KeyStore.getDefaultType());
		almacenClaves.load(ficheroAlmacen, claveAlmacen.toCharArray());//Cargamos las claves
		System.out.println("Cargadas las claves del almacén");
		//Creamos una fábrica de gestores de claves de confianza que use el almacén cargado previamente (con el certificado del servidor)
		TrustManagerFactory fabricaGestoresConfianza=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		fabricaGestoresConfianza.init(almacenClaves);
		System.out.println("Creada fábrica de gestores de confianza");
		//3-Creamos el contexto SSL.
		SSLContext contexto=SSLContext.getInstance("TLS");
		contexto.init(null, fabricaGestoresConfianza.getTrustManagers(), null);
		System.out.println("Creado contexto SSL");
		//Por último generamos el socket y lo devolvemos
		SSLSocketFactory fabricaSockets=contexto.getSocketFactory();
		clienteSSL=(SSLSocket) fabricaSockets.createSocket(ip, puerto);
		System.out.println("Socket creado");
		
				
		return clienteSSL;
	}
	
	
	
	


	public static void main(String[] args) throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		ClienteSSL cliente=new ClienteSSL("your route","cliente","localhost",5560);
		  Scanner escaner = new Scanner(System.in);
	        mostrarTexto("Ingresa la IP: [localhost por defecto] ");
	        ip = escaner.nextLine();
	        if (ip.length() <= 0) ip = "localhost";

	        mostrarTexto("Puerto: [9002 por defecto] ");
	         puerto = escaner.nextLine();
	        if (puerto.length() <= 0) puerto = "9002";
	        Thread hilo=new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					cliente.ejecutarConexion(ip, Integer.parseInt(puerto));
				}
	        	
	        });
	        hilo.start();
	        cliente.escribirDatos();

	}

}
