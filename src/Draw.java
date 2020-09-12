import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Draw extends JFrame {
    public Paper paper = new Paper(this);
    public UDP udp;
    private String[]args;



    public static void main(String[] args) {
        Draw draw = new Draw(args);
        //Skapar UDP


    }

    public Draw(String[]args) {
        this.args = args;
        udp = new UDP(args, this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().add(paper, BorderLayout.CENTER);
        setSize(640, 480);
        setVisible(true);
    }


    public void addPoint(Point p){
        this.paper.addPoint(p);
    }
}

class Paper extends JPanel {
    //PUBLIC?
    private HashSet hs = new HashSet();
    private Draw draw;

    public Paper(Draw draw) {
        this.draw = draw;
        setBackground(Color.white);
        addMouseListener(new L1());
        addMouseMotionListener(new L2());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        Iterator i = hs.iterator();
        while(i.hasNext()) {
            Point p = (Point)i.next();
            g.fillOval(p.x, p.y, 2, 2);
        }
    }

    public void addPoint(Point p) {
        hs.add(p);
        repaint();
        this.sendPoint(draw.udp, p);
    }

    public void sendPoint(UDP udp, Point p){
        udp.send(p);
    }

    class L1 extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            addPoint(me.getPoint());
        }
    }

    class L2 extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent me) {
            addPoint(me.getPoint());
        }
    }
}

class UDP extends Thread{
    private int toPort;
    private int myPort;
    private String host;
    private Draw draw;
    private String incomming;
    private DatagramSocket datagramSocket;


    public UDP(String[] args, Draw draw){
        this.myPort = Integer.parseInt(args[0]);
        this.host = args[1];
        this.toPort = Integer.parseInt(args[2]);
        this.draw = draw;
        start();
    }


    public void run(){
        try {
            // Socket för min port
             datagramSocket = new DatagramSocket(myPort);

            //buffer för inkommande data
            byte[] buffer = new byte[65536];

            //Datapacket för inkommande data
            DatagramPacket incommingData = new DatagramPacket(buffer, buffer.length);

            //Loop för att fånga upp in inkommande data
            while(true){
                datagramSocket.receive(incommingData);
                byte [] data = incommingData.getData();
                incomming = new String(data);
                String [] xy = incomming.trim().split(",");
                Point p = new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
                draw.addPoint(new Point(p));

            }

        } catch (Exception e ){
            System.out.println("Fel i run.");
            System.out.println(e);
        }
    }

    public void send(Point point){
        try {
            String send = Integer.toString(point.x)+ "," + Integer.toString(point.y);
            byte[] data = send.getBytes();
            InetAddress toAdress = InetAddress.getByName(host);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, toAdress, toPort);
            datagramSocket.send(datagramPacket);

        } catch (Exception e ){
            System.out.println("Fel i send.");
            System.out.println(e);
        }
    }
}

/*  -protected DatagramSocket DatagramSocket(int port, InetAddress inetaddress):-
    -to send data: DatagramPacket(byte buf[], int length, InetAddress inetaddress, int port):-
    -to receive the data: DatagramPacket(byte buf[], int length):-
 */