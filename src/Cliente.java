import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Cliente extends JFrame {

    /**
     * La clase cliente es un socket que puede enviar y recibir mensajes
     * Dispone de interfaz grafica donde visualizaremos los mensajes
     * El propio panel sera el encargado de crear nuevos usuarios, la idea es
     * simular la creacion de un grupo de whatsapp donde el administrador va añadiendo a personas
     * Esta clase necesita implementar un hilo de ejecucion para que este continuamente alerta de recibir
     * mensajes, podríamos haber extendido de Thread directamente,
     * pero para ver nuevas formas de implementar hilos, crearemos el hilo sobre el propio metodo [recibirMensaje()]
     */
    // --- ATRIBUTOS GRAFICOS ---//
    private String[][] coloresPaneles = {{"#4DEA0D","#50B129"},{"#E0BBF2","#911EC9"},{"#F48686","#EB2424"}}; //verde, morado rojo
    private String[] coloresTexto = {"green", "purple", "red"};
    private JPanel panelPrincipal;
    private JTextField areaTexto;
    private JButton btnEnviar;
    private JScrollPane myScrollPane;
    public JLabel etiquetaMensaje;
    private JPanel panelInterno;
    private JMenuBar mb;
    private JMenu menu1;
    private JMenuItem mi1,mi2,mi3;

    // --- ATRIBUTOS SOCKET--- //
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    //USUARIO
    private static AtomicInteger idusuario = new AtomicInteger(0);
    String nombreUsuario;
    private int contadorId;
    public static ArrayList<Cliente> arrayClientes = new ArrayList<>();


    public Cliente(Socket socket, String nombreUsuario){
        contadorId = idusuario.getAndIncrement();
        if (idusuario.get() == 3){
            idusuario.set(0);
        }
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.nombreUsuario = nombreUsuario;
        } catch (IOException e) {
            cerrarSocket(this.socket, bufferedReader, bufferedWriter);
        }
        initWindow();
        menu();
        add(panelPrincipal);
        setVisible(true);
        setTitle(nombreUsuario);
        recibirMensaje();
    }

    public void initWindow(){
        //BASICO
        setSize(600,400);
        setLocationRelativeTo(null);
        setResizable(false);
        //Modificamos colores panel
        panelPrincipal.setBackground(Color.decode(coloresPaneles[contadorId][0]));
        panelInterno.setBackground(Color.decode(coloresPaneles[contadorId][1]));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensajeServidor();
            }
        });
    }

    public void menu(){
        mb = new JMenuBar();
        menu1 = new JMenu("Opciones");
        mi1 = new JMenuItem("Nuevo participante");
        mi1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = JOptionPane.showInputDialog("Nombre de usuario");
                try {

                    arrayClientes.add(new Cliente(new Socket("localhost", 1234), usuario));
                    arrayClientes.get(arrayClientes.size()-1).enviarPrimerMensaje();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        mi2 = new JMenuItem("Abandonar chat");
        mi2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                cerrarSocket(socket, bufferedReader, bufferedWriter);
                arrayClientes.remove(this);
            }
        });
        menu1.add(mi1);
        menu1.add(mi2);
        mb.add(menu1);
        setJMenuBar(mb);
    }


    public void enviarMensajeServidor() {
        try {
            String mensajePropio;
            String mensaje = areaTexto.getText();
            areaTexto.setText("");
            mensajePropio = "<html><DIV align='right' width='550'>"
                    + "<html><FONT COLOR='" + coloresTexto[contadorId] + "'>"
                    + "<html><P>" + mensaje + "</FONT></DIV>";
            etiquetaMensaje.setText(etiquetaMensaje.getText() + mensajePropio);

            mensaje = "<html><FONT COLOR='" + coloresTexto[contadorId] + "'>"
                    + "<html>" + nombreUsuario + ": "
                    + mensaje + "</FONT><br/>";
            bufferedWriter.write(mensaje);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            areaTexto.setText("");
        } catch (IOException ex) {
            cerrarSocket(socket, bufferedReader, bufferedWriter);
        }
    }

    public void enviarPrimerMensaje(){
        try {
            bufferedWriter.write(nombreUsuario);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            cerrarSocket(socket, bufferedReader, bufferedWriter);
        }
    }

    public void recibirMensaje(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String mensajeAlGrupo;
                while (socket.isConnected()){
                    try {
                        mensajeAlGrupo = bufferedReader.readLine();
                        System.out.println(mensajeAlGrupo + nombreUsuario);
                        etiquetaMensaje.setText(etiquetaMensaje.getText() + mensajeAlGrupo);
                    } catch (IOException e) {
                        cerrarSocket(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }


    public void cerrarSocket(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String usuario = JOptionPane.showInputDialog("Nombre de usuario");
        Socket socket = new Socket("localhost", 1234);
        Cliente cliente = new Cliente(socket, usuario);
        arrayClientes.add(cliente);
        cliente.enviarPrimerMensaje();
        System.out.println("acabo mi vida");
    }
}
