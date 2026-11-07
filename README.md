# TP4-DAR2026 — Sockets en mode non-connecté UDP : service de chat Client/Serveur

**Matière :** Développement d'applications réparties — LSI 3
**Faculté des Sciences de Sfax** — Département d'informatique et des Communications
**Année universitaire :** 2026–2027
**Étudiant :** Salah Ahmed

---

## Sommaire

1. [Objectifs](#1-objectifs)
2. [Environnement](#2-environnement)
3. [Structure du projet](#3-structure-du-projet)
4. [Rappel : UDP et les classes Java utilisées](#4-rappel--udp-et-les-classes-java-utilisées)
5. [Étape 2 : Chat unidirectionnel (un seul client)](#5-étape-2--chat-unidirectionnel-un-seul-client)
6. [Étape 3 : Chat bidirectionnel et diffusion à plusieurs clients](#6-étape-3--chat-bidirectionnel-et-diffusion-à-plusieurs-clients)
7. [Exécution du projet](#7-exécution-du-projet)
8. [Synthèse](#8-synthèse)

---

## 1. Objectifs

- Échanger des données sous forme de **datagrammes** (`DatagramPacket`, tableaux d'octets) entre un client et un serveur avec les sockets UDP (`DatagramSocket`).
- Réaliser une application de **chat client-serveur** en mode non-connecté.
- Gérer l'envoi et la réception **asynchrones** (en temps quasi réel) de messages avec un ou plusieurs clients.

## 2. Environnement

| Élément | Version / outil |
|---|---|
| JDK | Java 26 |
| IDE | IntelliJ IDEA |
| Build | Maven (`pom.xml`) |
| Versionnement | Git / GitHub |
| Port du serveur | `1234` (UDP) |

## 3. Structure du projet

```
TP4-DAR2026
├── pom.xml
└── src/main/java
    ├── serverPackage
    │   ├── Server4_1.java     → Étape 2 : serveur unidirectionnel
    │   └── Server4_12.java    → Étape 3 : serveur de diffusion
    └── clientPackage
        ├── Client4_1.java     → Étape 2 : client émetteur
        └── Client4_12.java    → Étape 3 : client émetteur + récepteur (thread)
```

| Classe | Rôle |
|---|---|
| `Server4_1` | Écoute sur le port 1234 et affiche chaque message reçu avec l'IP et le port de l'expéditeur. |
| `Client4_1` | Demande un nom d'utilisateur, puis envoie chaque message saisi au serveur, préfixé par `[Nom] :`. |
| `Server4_12` | Garde la liste des clients et retransmet chaque message à tous les autres clients. |
| `Client4_12` | Envoie les messages (thread principal) et reçoit en temps réel les messages des autres (thread de réception). |

## 4. Rappel : UDP et les classes Java utilisées

UDP est un protocole **non connecté** : il n'y a ni `accept()` ni connexion établie. Chaque message est un **datagramme indépendant**, qui contient lui-même l'adresse et le port du destinataire. Il n'y a pas de garantie de livraison ni d'ordre, mais l'échange est léger et rapide, ce qui convient bien à un chat.

| Classe | Utilisation dans le TP |
|---|---|
| `DatagramSocket` | Socket UDP. Côté serveur : `new DatagramSocket(new InetSocketAddress(1234))`. Côté client : `new DatagramSocket()` (port choisi par le système). |
| `DatagramPacket` | Paquet envoyé ou reçu : tableau d'octets, longueur, et adresse/port du destinataire (pour l'envoi). |
| `InetSocketAddress` | Couple (IP, port), utilisé pour lier le serveur au port 1234. |
| `SocketAddress` | Adresse d'un client (IP + port), stockée dans la liste du serveur à l'étape 3. |
| `send()` / `receive()` | Envoi d'un paquet / attente **bloquante** de l'arrivée d'un paquet. |

Les messages sont convertis en octets avec `getBytes("UTF-8")` à l'envoi et reconstruits avec `new String(data, 0, length, "UTF-8")` à la réception, ce qui garde les accents corrects.

---

## 5. Étape 2 : Chat unidirectionnel (un seul client)

### Fonctionnement

**Serveur (`Server4_1`)**
1. Crée une `DatagramSocket` liée au port **1234** avec `InetSocketAddress`.
2. Dans une boucle infinie, prépare un `DatagramPacket` vide et attend un datagramme avec `receive()`.
3. Reconstruit le texte reçu et l'affiche avec l'**adresse IP et le port** de l'expéditeur.

**Client (`Client4_1`)**
1. Crée une `DatagramSocket` (port automatique).
2. Demande le **nom d'utilisateur** (`Anonyme` si rien n'est saisi).
3. Pour chaque message saisi, construit la chaîne `[Nom] : message`, la convertit en octets et l'envoie au serveur (`localhost:1234`).
4. La saisie de `quit` ferme la socket.

La communication va dans **un seul sens** : du client vers le serveur.

### Exemple d'exécution

**Console du client**
```
Nom d'utilisateur : Ahmed
Tapez vos messages (quit pour quitter) :
Bonjour serveur
Test UDP
quit
Client terminé.
```

**Console du serveur**
```
Lancement du serveur sur le port 1234...
Reçu de 127.0.0.1:52189 -> [Ahmed] : Bonjour serveur
Reçu de 127.0.0.1:52189 -> [Ahmed] : Test UDP
```

Le serveur affiche bien l'IP (`127.0.0.1`) et le port de l'expéditeur (`52189`, choisi automatiquement par le système pour le client), suivis du message préfixé par le nom d'utilisateur.

---

## 6. Étape 3 : Chat bidirectionnel et diffusion à plusieurs clients

### Fonctionnement

**Serveur (`Server4_12`)**
1. Écoute sur le port **1234**, comme à l'étape 2.
2. Maintient une **liste des clients** : un `Set<SocketAddress>` (`LinkedHashSet`), donc chaque client n'est enregistré qu'une fois.
3. À chaque datagramme reçu :
   - il **enregistre l'expéditeur** s'il ne le connaît pas encore (cela simule la « connexion ») ;
   - il **retransmet le message à tous les autres clients**, sauf l'expéditeur ;
   - si le message annonce un départ (`a quitté le chat`), il **retire** le client de la liste.

**Client (`Client4_12`)**
- Utilise **deux threads** pour envoyer et recevoir en même temps :
  - **thread principal** : lit les messages au clavier et les envoie au serveur ;
  - **thread de réception** : boucle sur `receive()` et affiche immédiatement les messages diffusés par le serveur.
- Au démarrage, il envoie `*** Nom a rejoint le chat ***`. Ce premier message enregistre le client auprès du serveur, qui peut ensuite lui envoyer les messages des autres même s'il n'a encore rien écrit.
- Avec `quit`, il envoie `*** Nom a quitté le chat ***`, puis ferme la socket.

```java
// Diffusion côté serveur : à tous les clients sauf l'expéditeur
for (SocketAddress client : clients) {
    if (!client.equals(expediteur)) {
        socket.send(new DatagramPacket(donnees, donnees.length, client));
    }
}
```

### Exemple d'exécution (3 clients : Ahmed, Sami, Ines)

**Console du serveur**
```
Serveur de chat (diffusion) lancé sur le port 1234...
Nouveau client enregistré : /127.0.0.1:53200 (total : 1)
Reçu de /127.0.0.1:53200 -> *** Ahmed a rejoint le chat ***
Message diffusé à 0 autre(s) client(s).
Nouveau client enregistré : /127.0.0.1:47714 (total : 2)
Reçu de /127.0.0.1:47714 -> *** Sami a rejoint le chat ***
Message diffusé à 1 autre(s) client(s).
Nouveau client enregistré : /127.0.0.1:38028 (total : 3)
Reçu de /127.0.0.1:38028 -> *** Ines a rejoint le chat ***
Message diffusé à 2 autre(s) client(s).
Reçu de /127.0.0.1:47714 -> [Sami] : Bonjour Ahmed
Message diffusé à 2 autre(s) client(s).
Reçu de /127.0.0.1:53200 -> [Ahmed] : Salut tout le monde
Message diffusé à 2 autre(s) client(s).
Reçu de /127.0.0.1:53200 -> *** Ahmed a quitté le chat ***
Message diffusé à 2 autre(s) client(s).
Client retiré : /127.0.0.1:53200 (total : 2)
```

**Console du client Ahmed**
```
Nom d'utilisateur : Ahmed
Tapez vos messages (quit pour quitter) :
*** Sami a rejoint le chat ***
*** Ines a rejoint le chat ***
[Sami] : Bonjour Ahmed
Salut tout le monde
quit
Client terminé.
```

**Console du client Ines**
```
Nom d'utilisateur : Ines
Tapez vos messages (quit pour quitter) :
[Sami] : Bonjour Ahmed
[Ahmed] : Salut tout le monde
*** Ahmed a quitté le chat ***
```

**Ce que montre ce test :**
- chaque nouveau client est enregistré une seule fois, et le nombre de clients augmente (1 → 2 → 3) ;
- un message envoyé par un client arrive **chez tous les autres**, mais **pas chez l'expéditeur** (Ahmed ne reçoit pas son propre « Salut tout le monde ») ;
- les messages s'affichent **en temps réel**, même pendant qu'un client attend une saisie, grâce au thread de réception ;
- un client qui quitte est retiré de la liste et ne reçoit plus rien.

---

## 7. Exécution du projet

1. Ouvrir le projet dans IntelliJ IDEA.
2. **Étape 2 :** lancer `Server4_1`, puis **un** `Client4_1`. Saisir un nom, puis des messages.
3. Arrêter `Server4_1` (bouton ■) pour libérer le port 1234.
4. **Étape 3 :** lancer `Server4_12`, puis **plusieurs** `Client4_12`.
   Pour lancer plusieurs clients : *Run → Edit Configurations → Client4_12 → Modify options → Allow multiple instances*.
5. Écrire dans un client : le message apparaît dans tous les autres.
6. Taper `quit` pour quitter un client.

> Pour un test entre deux machines du même réseau, remplacer `"localhost"` dans les clients par l'adresse IPv4 du serveur (`ipconfig`) et autoriser le port UDP 1234 dans le pare-feu.

---

## 8. Synthèse

Ce TP m'a permis de mettre en pratique la communication **en mode non-connecté** avec UDP. Contrairement à TCP (TP1 à TP3), il n'y a ni connexion ni `accept()` : le serveur ne connaît un client qu'au moment où il reçoit un de ses datagrammes. Chaque `DatagramPacket` porte lui-même l'adresse et le port de son destinataire, et toutes les données passent par des **tableaux d'octets**.

À l'**étape 2**, le chat est **unidirectionnel** : le client envoie, le serveur affiche le message et l'adresse (IP + port) de l'expéditeur, récupérée directement dans le paquet reçu.

À l'**étape 3**, le serveur simule une notion de « connexion » en gardant la **liste des adresses** (`SocketAddress`) des clients qui lui ont écrit, puis il **diffuse** chaque message à tous les autres. Côté client, l'envoi et la réception doivent se faire **en même temps** : `receive()` et la lecture au clavier sont tous les deux bloquants, donc la réception est confiée à un **thread séparé**. C'est ce qui permet d'avoir un chat **asynchrone en temps réel**.

**Limites de UDP observées :** aucune garantie de livraison ni d'ordre des messages, et pas de détection de déconnexion. Si un client se ferme sans envoyer `quit`, le serveur le garde dans sa liste. Sur un réseau local, ces limites ont peu d'effet, et UDP reste léger et rapide pour ce type d'application.
