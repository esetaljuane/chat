import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Cada usuario llega a esta clase siendo un socket
 * Cada objeto de esta clase estara guardado en un
 * arraylist estatico para poder destruirlo en caso necesario
 * Ademas este array sirve para poder mandar los mensajes a todos los usuario
 * Cada mensaje que llegue por parte de cualquiera de los usuarioos
 * sera mandando al metodo brodcastMensaje
 * este metodo recorre la lista enviando el mensaje a todos los usuarios.
 */
public class ManejadorClientes implements Runnable{

    public static ArrayList<ManejadorClientes> manejadorUsuarios = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String nombreUsuario;

    public ManejadorClientes(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nombreUsuario = bufferedReader.readLine();
            manejadorUsuarios.add(this);

            String mensaje = "<html>SERVIDOR: " + nombreUsuario +
                    "<html> ha ingresado al chat<br/>";
            brodcastMensaje(mensaje);
        } catch (IOException e) {
            System.out.println("MC1: " + e.getMessage());
            cerrarSocket(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String mensajeCliente;

        while (socket.isConnected()){
            try{
                mensajeCliente = bufferedReader.readLine();
                brodcastMensaje(mensajeCliente);

            }catch (IOException | NullPointerException e){
                System.out.println("MC2: " + e.getMessage());
                cerrarSocket(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void brodcastMensaje(String mensajeAEnviar){
        for (ManejadorClientes manejador : manejadorUsuarios){
            try {
                if (!manejador.nombreUsuario.equals(this.nombreUsuario)){
                    manejador.bufferedWriter.write(mensajeAEnviar);
                    manejador.bufferedWriter.newLine();
                    manejador.bufferedWriter.flush();
                }
            }catch (IOException | NullPointerException e){
                System.out.println("MC3: " + e.getMessage());
                cerrarSocket(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void eliminarCliente(){
        try {
            manejadorUsuarios.remove(this);
            String mensaje = "<html>SERVIDOR: " + this.nombreUsuario + " ha sido eliminado<br/>";
            brodcastMensaje(mensaje);
            System.out.println("Eliminado usuaro:" + this.nombreUsuario);
        } catch (Exception e) {
            System.out.println("MC4: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cerrarSocket(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        eliminarCliente();
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }

        }catch (IOException e){
            System.out.println("MC5: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
