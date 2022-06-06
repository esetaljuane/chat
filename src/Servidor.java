import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * La clase Servidor será la encargada de esperar la recección
 * de cada usuario o cliente que sea creado (socket)
 * Una vez llege el socket manda este socket a la clase
 * ManejadorClientres
 */
public class Servidor{

    private ServerSocket serverSocket;

    public Servidor(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("Nuevo usuario conectado");

                ManejadorClientes manejadorClientes = new ManejadorClientes(socket);

                Thread hilo = new Thread(manejadorClientes);
                hilo.start();
            }
        }catch (IOException e){
            cerrarServidor();
        }
    }

    public void cerrarServidor(){
        try{
            if (serverSocket != null){
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);

            Servidor servidor = new Servidor(serverSocket);
            servidor.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
