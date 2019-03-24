import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;


import javax.net.ssl.*;

public class Server1 {
	private String rutaAlmacen;
	private String claveAlmacen;
	private String claveCertificado;
	private SSLServerSocket socketSSL=null;
	private Socket conexion;
	
	
	public Server1(String rutaAlmacen, String claveAlmacen, String claveCertificado) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		this.rutaAlmacen=rutaAlmacen;
		this.claveAlmacen=claveAlmacen;
		this.claveCertificado=claveCertificado;
		SSLServerSocket socketSSL=this.creaSSLSocket(9002);//Obtenemos un socket de nuestro método
	}
	
	
	//Implementamos un método que llevará a cabo las conexiones
	public void conecta() throws IOException {
		Conexiones.llenaAvion();//Ponemos a punto el esquema de nuestro avión
		while(true) {//El servidor acepta conexiones de forma continua
			conexion=socketSSL.accept();
			Thread hilo=new Conexiones(conexion);
			hilo.start();
		}
	}
	
	
	
	//Método para crear el socket SSL
		public SSLServerSocket creaSSLSocket(int puerto) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
			
			//SocketSSL que vamos a devolver
			
			//1->Creamos el contenido del fichero donde se almacenan las claves de un almacén de claes.
			FileInputStream ficheroAlmacen=new FileInputStream(this.rutaAlmacen);
			KeyStore almacen= KeyStore.getInstance(KeyStore.getDefaultType());
			almacen.load(ficheroAlmacen, claveAlmacen.toCharArray());
			System.out.println("El almacén de claves ha sido leído");
			//2->Creamos fábrica (KeyManagerFactory) que cree un gestor de claves (KeyManager)
			KeyManagerFactory fabricaClaves=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			fabricaClaves.init(almacen, claveCertificado.toCharArray());
			System.out.println("Leída clave del servidor");
			//3->Intentamos generar un contexto SSL
			SSLContext contextoSSL=SSLContext.getInstance("TLS");
			contextoSSL.init(fabricaClaves.getKeyManagers(), null, null);
			//Creamos la fábrica de sockets SSL y un serversocket
			SSLServerSocketFactory fabricaSockets=contextoSSL.getServerSocketFactory();
			socketSSL=(SSLServerSocket)fabricaSockets.createServerSocket(puerto);
			System.out.println("El socket ha sido creado");
			
			//Por último devolvemos el socket
			return socketSSL;
			
		}
	

	public static void main(String[] args) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		/*Creamos un objeto de nuestra clase y le pasamos la ruta (le paso la ruta entera porque de lo contrario da
		 * problemas, por la forma de trabajar que tiene eclipse con las rutas*/
		Server1 s=new Server1("/home/josete88/Documents/eclipse/ServidorSSL/almacenservidor", "servidor", "servidor");
		s.conecta();

	}

}
