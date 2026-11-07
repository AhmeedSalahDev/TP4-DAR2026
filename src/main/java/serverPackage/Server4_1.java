package serverPackage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * TP4 - Étape 2 : Chat unidirectionnel (serveur).
 * Le serveur écoute sur le port 1234 et affiche chaque message reçu
 * avec l'adresse (IP et port) de l'expéditeur.
 */
public class Server4_1 {
    static byte buffer[] = new byte[1024];

    public static void main(String argv[]) throws Exception {
        // 1. Le serveur crée une socket UDP liée au port 1234 (via InetSocketAddress)
        DatagramSocket socket = new DatagramSocket(new InetSocketAddress(1234));
        System.out.println("Lancement du serveur sur le port 1234...");

        while (true) {
            // 2. Le serveur prépare un paquet vide pour recevoir un datagramme
            DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);

            // 3. receive() bloque jusqu'à l'arrivée d'un paquet UDP
            socket.receive(paquet);

            // 4. Reconstruction de la chaîne à partir des octets reçus
            int taille = paquet.getLength();
            String donnees = new String(paquet.getData(), 0, taille, "UTF-8");

            // 5. Affichage : adresse de l'expéditeur + contenu
            System.out.println("Reçu de " + paquet.getAddress().getHostAddress()
                    + ":" + paquet.getPort() + " -> " + donnees);
        }
    }
}
