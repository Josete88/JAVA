import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Conexiones extends Thread {
	private static String [][] avion=new String[7][3];//7 filas y 3 columnas (Servirá para representar nuestro avión)
	private Socket sc;
	private DataOutputStream salidaCliente;
	private DataInputStream entrada;
	private String mensaje="";
	private final String COMANDO_SALIR="salir";
	
	public Conexiones(Socket sc) {
		this.sc=sc;//Recibimos el socket para trabajar con el.
	}
	
	/*El siguiente método llenará nuestro "avión" en función de nuestras necesidades (he decidido que por 
	 * defecto todas las plazas van a estar disponibles*/
	public static void llenaAvion() {
		for (int i=0; i<avion.length; i++) {
			for (int j=0; j<avion[0].length; j++) {
				avion[i][j]=" ";//Llenamos nuestro avión con espacios en blanco
			}
		}
		avion[0][0]=" ";//Dejamos un espacio en blanco en la esquina superior izquierda (para respetar el esquema propuesto)
		avion[0][1]="I";//Añadimos la I de izquierda en la posición requerida
		avion[0][2]="D";//Hacemos lo propio con la D de derecha
		for (int i=1; i<avion.length; i++) {
			avion[i][0]=String.valueOf(i);//Añadimos el número de los asientos en la columna de la izquierda
		}
	}
	
	
	//Implementamos un método para enviar los mensajes al cliente
	public void enviar(String s) throws IOException {
		salidaCliente.writeUTF(s);
	}
	
	//Este método será el encargado de procesar las órdenes, realizar las tareas y devolver los datos correspondientes  
	public void devuelveDatos(String orden) throws IOException {
		  try {
			  String datos="";
			  String orden1;
			  int nAsiento=0,lado=0;
			  
			  switch (orden) {
			  case "RESERVA":
			  enviar("Por favor, indique el lado del avión donde se quiere sentar(izquierda=I, derecha=D");
			  System.out.println("El cliente está haciendo una reserva");
			  orden1=(String)entrada.readUTF();
			  
			  if(orden1.equalsIgnoreCase("D")) {//Si el cliente elige el lado derecho del avión
					lado=2;//Lado = 2 (recordemos que nuestra matriz tiene 3 columnas)
					enviar("Ha elegido el lado derecho del avión, ahora, indique el número del asiento que quiere reservar");
					nAsiento=Integer.parseInt(entrada.readUTF());//Ahora guardamos el número de asiento que ha elegido el cliente
					enviar("Ha elegido el asiento " + nAsiento);//Notificamos al cliente el número del asiento elegido
					//nAsiento++;	
				}else if(orden1.equalsIgnoreCase("I")){//Si el cliente elige el lado izquierdo
					lado=1;//Lado=1 (Una vez más recordemos la estructura de nuestra matriz)
					enviar("Ha elegido el lado izquierdo del avión, ahora, inidcque el número del asiento que quiere reservar");
					nAsiento=Integer.parseInt(entrada.readUTF());
					enviar("Ha elegido el asiento " + nAsiento);//Notificamos al cliente el número del asiento elegido
					//nAsiento++;
				}
				if(!avion[nAsiento][lado].equalsIgnoreCase("X")) {//Si el asiento está disponible
					avion[nAsiento][lado]="X";//Reservamos el asiento
					System.out.println("El cliente ha reservado un asiento");
					enviar("Ha reservado con éxito su asiento");
				}else {//En caso de que el asiento no esté disponible
					enviar("Lo sentimos, el asiento ya está ocupado, por favor, elija otro");
				}
				break;
			case "ANULAR":
				enviar("Por favor, indique el lado donde tiene reservado su asiento");
				System.out.println("El cliente está anulando una reserva");
				orden1=(String)entrada.readUTF();
				if(orden1.equalsIgnoreCase("D")) {//Si el cliente elige el lado derecho del avión
					lado=2;//Esta parte es igual que antes
					enviar("De acuerdo, su asiento está en el lado derecho. Ahora, indique el número del mismo");
					nAsiento=Integer.parseInt(entrada.readUTF());
					enviar("Ha elegido anular la reserva para el asiento " + nAsiento);
					enviar("¿Está seguro?(escriba si o no)");
					orden1=(String)entrada.readUTF();
					//Si el cliente escribe si y el asiento está realmente reservado
					if(orden1.equalsIgnoreCase("si") && avion[nAsiento][lado]=="X") {
						avion[nAsiento][lado]=" ";
						enviar("Su reserva ha sido anulada con éxito");
						System.out.println("El cliente ha anulado su reserva");
					}else if(avion[nAsiento][lado]!="X") {//Si el asiento elegido está libre
						enviar("Disculpe, pero el asiento que ha elegido todavía no ha sido reservado, asegúrese de que el asiento elegido corresponde con su reserva");
					}else {
						enviar("La reserva no será anulada");//Si el cliente dice que no
					}
				}else if(orden1.equalsIgnoreCase("I")) {//Si el cliente elige el lado izquierdo (el resto es igual que antes)
					lado=1;
					enviar("De acuerdo, su asiento está en el lado izquierdo. Ahora, indique el número del mismo");
					nAsiento=Integer.parseInt(entrada.readUTF());
					enviar("Ha elegido anular la reserva para el asiento " + nAsiento);
					enviar("¿Está seguro?(escriba si o no)");
					orden1=(String)entrada.readUTF();
					if(orden1.equalsIgnoreCase("si") && avion[nAsiento][lado]=="X") {
						avion[nAsiento][lado]=" ";
						enviar("Su reserva ha sido anulada con éxito");
						System.out.println("El cliente ha anulado su reserva");
					}else if(avion[nAsiento][lado]!="X") {
						enviar("Disculpe, pero el asiento que ha elegido todavía no ha sido reservado, asegúrese de que el asiento elegido corresponde con su reserva");
					}else {
						enviar("La reserva no será anulada");
					}
				}else {
					enviar("Orden no encontrada, escriba AYUDA para ver las órdenes disponibles y como trabajar con ellas");
				}		
				break;
			case "LISTA":
				int plazas=12;//Contamos con que tenemos 12 plazas en total
				
				for (int i=0; i<avion.length; i++) {
					datos+="\n";
					for (int j=0; j<avion[0].length; j++) {
						datos+=avion[i][j] + "    ";
						//Por cada x que nos encontremos (plaza ya reservada) restaremos una plaza disponible
						if(avion[i][j]=="X") {
							plazas--;
						}
						
					}
				}
				enviar(datos);//Enviamos la representación del avión
				enviar("Quedan " + plazas + " asientos disponibles");//Informamos al cliente de las plazas disponibles
				System.out.println("Enviando el estado del avión al cliente");
				break;
			case "AYUDA":
				enviar("Escriba LISTA para ver el estado del avión. Una vez compruebe que hay asientos y \n"
						+ "decida cual quiere reservar, puede escribir RESERVA. Acto seguido se le preguntará en qué lado\n"
						+ "del avión desea viajar, izquierdo o derecho. Para indicarlo, únicamente debe escribir I o D en \n"
						+ "función de su preferencia. Para finalizar, se le preguntará el número del asiento que desea y, \n"
						+ "en caso de estar disponible, se llevará a cabo la reserva del mismo. Si lo que desea es anular\n"
						+ "una reserva, debe escribir ANULAR y seguir las instrucciones del sistema, el proceso es el mismo\n"
						+ "que para realizar la reserva. Puede consultar esta guía en cualquier momento escribiendo AYUDA.");
				break;
			case "SALIR"://Si el cliente elige salir
				System.out.println("Un cliente se ha desconectado");
				enviar("Hasta pronto");//Le mandamos un mensaje de despedida
				cerrarConexion();//Llalamos al método encargado de cerrar la conexión
				break;
			
			default://En caso de que el cliente introduzca una orden errónea
				enviar("Orden no encontrada, escriba AYUDA para ver las órdenes disponibles y como trabajar con ellas");
				break;
			}
					
				}catch(Exception e) {
					
				}
		}
	
	  //Método encargado de cerrar los flujos de datos y la conexión
	  public void cerrarConexion() {
		  try {
			  entrada.close();
			  salidaCliente.close();
			  sc.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	    		
	
	
	public void run() {
		
		try {
			salidaCliente=new DataOutputStream(sc.getOutputStream());
			entrada=new DataInputStream(sc.getInputStream());
			System.out.println("Un cliente se ha conectado");
			enviar("Bienvenido a su aerolínea, por favor indique la operación que desea realizar:" + 
					"\n1->LISTA (muestra los asientos disponibles)"+
					"\n2->RESERVA (le permite reservar un asiento para el vuelo) "+ 
					"\n3->ANULAR (le permmite anular su reserva)" + 
					"\n4->SALIR (cierra la conexión)" + 
					"\n5->AYUDA (le muestra una serie de indicaciones para interactuar con el sistema)");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		while(!mensaje.equalsIgnoreCase(COMANDO_SALIR)) {
			try {
			mensaje=entrada.readUTF();
			devuelveDatos(mensaje);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
		//cerrarConexion();
		
		
	}

}
