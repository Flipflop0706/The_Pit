package de.hwr.leaderboard;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

/**
 * Startpunkt des Bestenlisten-Servers.
 *
 * <p>Der Port wird aus der Umgebungsvariable {@code PORT} gelesen, weil Render (und
 * die meisten anderen Cloud-Anbieter) den Port zur Laufzeit vorgeben und nicht
 * fest einprogrammiert werden duerfen. Der Speicherort der Datei kommt aus
 * {@code DATA_DIR}, damit er auf ein Persistent Disk zeigen kann (siehe README).</p>
 */
public final class LeaderboardServer {

    private LeaderboardServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String dataDir = System.getenv().getOrDefault("DATA_DIR", ".");
        Path scoresFile = Path.of(dataDir, "scores.json");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/scores", new ScoresHandler(scoresFile));
        server.setExecutor(null);
        server.start();

        System.out.println("Leaderboard-Server laeuft auf Port " + port + ", Datei: " + scoresFile.toAbsolutePath());
    }
}
