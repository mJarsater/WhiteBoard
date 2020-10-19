import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.List;

public class Draw extends JFrame {
    public Paper paper = new Paper(this);
    public UDP udp;
    private String[]args;


    public static void main(String[] args) {
        new Draw(args);
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
        this.paper.addToWhiteBoard(p);
    }
}

class Paper extends JPanel {

    /* Skapar en HashSet och gör den sedan
    *  "thread"-safe genom synchronizedSet(HashSet)*/
    public HashSet<Point> hs = new HashSet();
    public Set<Point> set = Collections.synchronizedSet(hs);
    private Draw draw;

    public Paper(Draw draw) {
        this.draw = draw;
        setBackground(Color.white);
        addMouseListener(new L1());
        addMouseMotionListener(new L2());
    }

    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        List<Point> pointList = new ArrayList<>(set);
        for(int i = 0; i< pointList.size(); i++){
            Point p = pointList.get(i);
            g.fillOval(p.x, p.y, 2, 2);
        }
    }

    /* Metod som tar parametern och
    *  lägger till den vår HashSet */
    public void addToWhiteBoard(Point p){
        set.add(p);
        repaint();
    }

    /* Metod som tar parametern
    * och lägger till det i vår
    * hashset, ritar ut det, och
    * kallar på sendPoint */
    public void addPoint(Point p) {
        set.add(p);
        repaint();
        sendPoint(draw.udp, p);
    }

    /* Metod som kallar på metoden send
    *  i vår UDP. */
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
    private DatagramSocket datagramSocket;

    // Konstruktor för klassen UDP
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
                String incoming = new String(data);
                String [] xy = incoming.trim().split(",");
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
            String send = point.x + "," + point.y;
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

