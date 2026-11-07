package serverPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TP4 - Étape 3 : Chat bidirectionnel avec diffusion (serveur).
 * Le serveur garde la liste des adresses des clients qui ont envoyé un message
 * et retransmet chaque message à tous les autres clients (sauf l'expéditeur).
 */
public class Server4_12 {
    static byte buffer[] = new byte[1024];

    // Liste des clients "connectés" (adresse IP + port), sans doublons
    static Set<SocketAddress> clients = new LinkedHashSet<>();

    public static void main(String argv[]) throws Exception {
        // 1. Socket UDP liée au port 1234 (via InetSocketAddress)
        DatagramSocket socket = new DatagramSocket(new InetSocketAddress(1234));
        System.out.println("Serveur de chat (diffusion) lancé sur le port 1234...");

        while (true) {
            // 2. Réception d'un datagramme
            DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquet);

            String message = new String(paquet.getData(), 0, paquet.getLength(), "UTF-8");
            SocketAddress expediteur = paquet.getSocketAddress();

            // 3. Enregistrer l'expéditeur s'il est nouveau
            if (clients.add(expediteur)) {
                System.out.println("Nouveau client enregistré : " + expediteur
                        + " (total : " + clients.size() + ")");
            }

            System.out.println("Reçu de " + expediteur + " -> " + message);

            // 4. Diffuser le message à tous les autres clients
            byte[] donnees = message.getBytes("UTF-8");
            for (SocketAddress client : clients) {
                if (!client.equals(expediteur)) {
                    DatagramPacket envoi = new DatagramPacket(donnees, donnees.length, client);
                    socket.send(envoi);
                }
            }
            System.out.println("Message diffusé à " + (clients.size() - 1) + " autre(s) client(s).");

            // 5. Si le client quitte le chat, on le retire de la liste
            if (message.endsWith("a quitté le chat ***")) {
                clients.remove(expediteur);
                System.out.println("Client retiré : " + expediteur
                        + " (total : " + clients.size() + ")");
            }
        }
    }
}
