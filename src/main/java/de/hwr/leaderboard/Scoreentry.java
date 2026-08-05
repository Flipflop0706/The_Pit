package de.hwr.leaderboard;

import java.util.Objects;

/**
 * Ein Eintrag der Bestenliste.
 *
 * <p>Die Feldnamen entsprechen absichtlich exakt {@code HighscoreEntry} im
 * Spiel-Projekt (Paket {@code de.hwr.thepit.persistence}). Gson serialisiert und
 * deserialisiert ueber Reflection anhand der Feldnamen, nicht anhand von Konstruktoren
 * — weichen die Namen ab, bleiben Felder beim Parsen einfach {@code null}/{@code 0},
 * ohne dass ein Fehler auftritt. Die Sortierordnung in {@link #compareTo(ScoreEntry)}
 * ist ebenfalls bewusst identisch zur Client-Seite gehalten, damit beide Seiten
 * dieselbe Bestenliste im selben Sinne verstehen.
 */
public class ScoreEntry implements Comparable<ScoreEntry> {

    private String playerName;
    private int score;
    private int levelReached;
    private String difficultyName;
    private long timestampEpochSeconds;
    private boolean victory;

    /** Fuer Gson; Felder werden per Reflection gesetzt. */
    public ScoreEntry() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getLevelReached() {
        return levelReached;
    }

    public String getDifficultyName() {
        return difficultyName;
    }

    public long getTimestampEpochSeconds() {
        return timestampEpochSeconds;
    }

    public boolean isVictory() {
        return victory;
    }

    /**
     * Prueft, ob der Eintrag plausibel genug ist, um gespeichert zu werden.
     *
     * <p>Der Server vertraut keinem Client blind: Ein manipulierter oder fehlerhafter
     * Request darf die Bestenliste nicht mit leeren Namen oder negativen Werten
     * fuellen.</p>
     *
     * @return {@code true}, wenn alle Felder plausibel gesetzt sind
     */
    public boolean isValid() {
        return playerName != null && !playerName.isBlank()
                && difficultyName != null && !difficultyName.isBlank()
                && score >= 0
                && levelReached > 0
                && timestampEpochSeconds >= 0;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        Objects.requireNonNull(other, "other darf nicht null sein");
        int byVictory = Boolean.compare(other.victory, victory);
        if (byVictory != 0) {
            return byVictory;
        }
        int byLevel = Integer.compare(other.levelReached, levelReached);
        if (byLevel != 0) {
            return byLevel;
        }
        int byScore = Integer.compare(other.score, score);
        if (byScore != 0) {
            return byScore;
        }
        int byTime = Long.compare(timestampEpochSeconds, other.timestampEpochSeconds);
        if (byTime != 0) {
            return byTime;
        }
        int byName = playerName.compareTo(other.playerName);
        return byName != 0 ? byName : difficultyName.compareTo(other.difficultyName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ScoreEntry other)) {
            return false;
        }
        return score == other.score && levelReached == other.levelReached
                && timestampEpochSeconds == other.timestampEpochSeconds && victory == other.victory
                && Objects.equals(playerName, other.playerName)
                && Objects.equals(difficultyName, other.difficultyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, score, levelReached, difficultyName, timestampEpochSeconds, victory);
    }
}
