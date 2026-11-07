package clientPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * TP4 - Étape 3 : Chat bidirectionnel (client).
 * Le thread principal envoie les messages saisis ;
 * un second thread reçoit en temps réel les messages diffusés par le serveur.
 */
public class Client4_12 {
    public static void main(String argv[]) throws Exception {
        // 1. Socket UDP du client (port choisi par le système)
        DatagramSocket socket = new DatagramSocket();
        InetAddress adresseServeur = InetAddress.getByName("localhost");
        int portServeur = 1234;

        Scanner sc = new Scanner(System.in);
        System.out.print("Nom d'utilisateur : ");
        String nom = sc.nextLine().trim();
        if (nom.isEmpty()) nom = "Anonyme";

        // 2. Thread de réception : écoute en permanence les messages du serveur
        Thread reception = new Thread(() -> {
            byte[] buffer = new byte[1024];
            try {
                while (true) {
                    DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(paquet);
                    String msg = new String(paquet.getData(), 0, paquet.getLength(), "UTF-8");
                    System.out.println(msg);
                }
            } catch (SocketException e) {
                // socket fermée : fin du thread de réception
            } catch (Exception e) {
                System.out.println("Erreur de réception : " + e.getMessage());
            }
        });
        reception.setDaemon(true);
        reception.start();

        // 3. Message d'arrivée : il enregistre le client auprès du serveur
        envoyer(socket, "*** " + nom + " a rejoint le chat ***", adresseServeur, portServeur);

        System.out.println("Tapez vos messages (quit pour quitter) :");

        // 4. Thread principal : envoi des messages saisis
        while (true) {
            String saisie = sc.nextLine();
            if (saisie.equalsIgnoreCase("quit")) {
                envoyer(socket, "*** " + nom + " a quitté le chat ***", adresseServeur, portServeur);
                break;
            }
            envoyer(socket, "[" + nom + "] : " + saisie, adresseServeur, portServeur);
        }

        socket.close();
        sc.close();
        System.out.println("Client terminé.");
    }

    private static void envoyer(DatagramSocket socket, String message,
                                InetAddress adresse, int port) throws Exception {
        byte[] buffer = message.getBytes("UTF-8");
        socket.send(new DatagramPacket(buffer, buffer.length, adresse, port));
    }
}
