package de.hwr.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verarbeitet {@code GET}, {@code POST} und {@code DELETE} auf {@code /scores}.
 *
 * <p>Die Bestenliste liegt als JSON-Datei auf der Festplatte und wird bei jedem
 * Zugriff neu gelesen bzw. komplett neu geschrieben — bei hoechstens zehn Eintraegen
 * lohnt keine In-Memory-Zwischenspeicherung, und so bleibt der Server nach jedem
 * Neustart sofort wieder korrekt, sofern die Datei ueberlebt (siehe README zu Render
 * Disks).</p>
 */
public class ScoresHandler implements HttpHandler {

    /** Maximale Groesse der Bestenliste, analog {@code HighscoreService.MAX_ENTRIES} im Spiel. */
    private static final int MAX_ENTRIES = 10;

    private static final Type ENTRY_LIST_TYPE = new TypeToken<List<ScoreEntry>>() { }.getType();

    private final Path file;
    private final Gson gson = new GsonBuilder().create();

    public ScoresHandler(Path file) {
        this.file = file;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> sendResponse(exchange, 405, "[]");
            }
        } catch (JsonSyntaxException malformed) {
            sendResponse(exchange, 400, "{\"error\":\"ungueltiges JSON\"}");
        } catch (RuntimeException unexpected) {
            sendResponse(exchange, 500, "{\"error\":\"interner Fehler\"}");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, gson.toJson(readAll()));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        ScoreEntry submitted = gson.fromJson(body, ScoreEntry.class);

        if (submitted == null || !submitted.isValid()) {
            sendResponse(exchange, 422, "{\"error\":\"ungueltiger Eintrag\"}");
            return;
        }

        List<ScoreEntry> entries = new ArrayList<>(readAll());
        entries.add(submitted);
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries.subList(MAX_ENTRIES, entries.size()).clear();
        }
        writeAll(entries);
        sendResponse(exchange, 201, gson.toJson(entries));
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        writeAll(List.of());
        sendResponse(exchange, 200, "[]");
    }

    private List<ScoreEntry> readAll() throws IOException {
        if (!Files.isRegularFile(file)) {
            return List.of();
        }
        String content = Files.readString(file, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return List.of();
        }
        List<ScoreEntry> entries = gson.fromJson(content, ENTRY_LIST_TYPE);
        return entries == null ? List.of() : entries;
    }

    private void writeAll(List<ScoreEntry> entries) throws IOException {
        Files.writeString(file, gson.toJson(entries), StandardCharsets.UTF_8);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
