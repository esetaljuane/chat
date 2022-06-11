import java.io.*;
import java.net.*;

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
            System.out.println("S1: " + e.getMessage());
            cerrarServidor();
        }
    }

    public void cerrarServidor(){
        try{
            if (serverSocket != null){
                serverSocket.close();
            }
        }catch (IOException e){
            System.out.println("S2: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // --- ATRIBUTOS SOCKET--- //
        Socket socket;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        try{
            ServerSocket serverSocket = new ServerSocket(1234);
            Servidor servidor = new Servidor(serverSocket);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    servidor.startServer();
                }
            }).start();
            socket = new Socket("localhost", 1234);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            servidor.getSocketServidor(socket, bufferedReader, bufferedWriter);
        }catch (IOException e){
            System.out.println("Main: " + e.getMessage());
            e.getStackTrace();
        }
    }

    private void getSocketServidor(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        recibirMensaje(socket, bufferedReader);
        enviarPrimerMensaje(socket, bufferedWriter);
    }

    private void enviarPrimerMensaje(Socket socket, BufferedWriter bufferedWriter) {
        try {
            bufferedWriter.write("Soy el servidor");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            System.out.println("S3: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recibirMensaje(Socket socket, BufferedReader bufferedReader) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg;
                while (socket.isConnected()){
                    try {
                        msg = bufferedReader.readLine();
                        //TODO se puede guardar como log
                        System.out.println(msg);
                    } catch (IOException e) {
                        System.out.println("S4: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
