package clientPackage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;



public class Client_4_1 {
    public static void main(String argv[]) throws Exception {
        // 1. Le client crée une socket UDP (port choisi par le système)
        DatagramSocket socket = new DatagramSocket();
        InetAddress adresseServeur = InetAddress.getByName("localhost");
        int portServeur = 1234;

        Scanner sc = new Scanner(System.in);
        System.out.print("Nom d'utilisateur : ");
        String nom = sc.nextLine().trim();
        if (nom.isEmpty()) nom = "Anonyme";

        System.out.println("Tapez vos messages (quit pour quitter) :");
        while (true) {
            String saisie = sc.nextLine();
            if (saisie.equalsIgnoreCase("quit")) break;

            // 2. Préfixer le message par le nom d'utilisateur
            String ch = "[" + nom + "] : " + saisie;

            // 3. Convertir le message en tableau d'octets
            byte buffer[] = ch.getBytes("UTF-8");
            int length = buffer.length;

            // 4. Préparer le paquet : message, adresse et port du serveur
            DatagramPacket donneesEmises = new DatagramPacket(buffer, length,
                    adresseServeur, portServeur);

            // 5. Envoyer le paquet au serveur
            socket.send(donneesEmises);
        }

        socket.close();
        sc.close();
        System.out.println("Client terminé.");
    }
}
