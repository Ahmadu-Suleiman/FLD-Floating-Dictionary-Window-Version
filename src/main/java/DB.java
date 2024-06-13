import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DB {
    private static Connection getConnection() throws SQLException {
        String path = "jdbc:sqlite::resource:WiktionaryDatabase.db";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Utils.logger.error(e.toString());
        }

        return DriverManager.getConnection(path);
    }

    public static CompletableFuture<String[]> getSimilarEntryWords(String word) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> similarWords = new ArrayList<>();
            String sql = "SELECT entry_word FROM entry_words WHERE entry_word LIKE? LIMIT 9";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, word + "%");
                ResultSet resultSet = statement.executeQuery();

                similarWords.add(word);
                while (resultSet.next()) {
                    similarWords.add(resultSet.getString("entry_word"));
                }
            } catch (SQLException e) {
                Utils.logger.error(e.toString());
            }

            return similarWords.stream().distinct().toArray(String[]::new);
        });
    }

    public static CompletableFuture<Boolean> wordExist(String word) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT (COUNT(*) > 0) AS found FROM entry_words WHERE entry_word =?";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, word);
                ResultSet resultSet = statement.executeQuery();
                return resultSet.getBoolean("found");
            } catch (SQLException e) {
                Utils.logger.error(e.toString());
                return false;
            }
        });
    }

    public static CompletableFuture<String> getRandomWord() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT entry_word FROM entry_words ORDER BY random() LIMIT 1";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("entry_word");
                }
            } catch (SQLException e) {
                Utils.logger.error("Error occurred while fetching random word", e);
            }
            return "A"; // Default value in case of failure
        });
    }

    public static CompletableFuture<ArrayList<String>> getPreviousWords(String word) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<String> previousWords = new ArrayList<>(20);
            String sql = "SELECT entry_word FROM entry_words WHERE entry_word <? ORDER BY entry_word DESC LIMIT 20";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, word);

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    previousWords.add(resultSet.getString("entry_word"));
                }
            } catch (SQLException e) {
                Utils.logger.error("Error occurred while fetching previous words", e);
            }
            return previousWords;
        });
    }

    public static CompletableFuture<ArrayList<String>> getNextWords(String word) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<String> nextWords = new ArrayList<>(20);
            String sql = "SELECT entry_word FROM entry_words WHERE entry_word >? ORDER BY entry_word ASC LIMIT 20";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, word);

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    nextWords.add(resultSet.getString("entry_word"));
                }
            } catch (SQLException e) {
                Utils.logger.error("Error occurred while fetching next words", e);
            }
            return nextWords;
        });
    }

    public static CompletableFuture<ArrayList<String>> getEntryWords(String word) {
        return wordExist(word)
                .thenCompose(exists -> {
                    if (exists) {
                        return getPreviousWords(word)
                                .thenCombine(getNextWords(word), (prevWords, nextWords) -> {
                                    prevWords.add(word);
                                    prevWords.addAll(nextWords);
                                    prevWords.sort(String::compareToIgnoreCase);
                                    return prevWords;
                                });
                    } else {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                    }
                });
    }

    public static ArrayList<Entry> getAllEntriesForWord(String word) {
        ArrayList<Entry> entries = new ArrayList<>();

        if (word.compareToIgnoreCase("M") >= 0) entries.addAll(getAllEntriesForWordGreaterThanL(word));
        else entries.addAll(getAllEntriesForWordLessEqualToL(word));
        return getDistinct(entries);
    }

    private static List<Entry> getAllEntriesForWordGreaterThanL(String word) {
        ArrayList<Entry> entries = new ArrayList<>();
        String sql = "SELECT * FROM entries_greater_than_L WHERE entry_word = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, word);
            ResultSet resultSet = statement.executeQuery();

            do entries.add(getEntry(resultSet)); while (resultSet.next());
        } catch (SQLException e) {
            Utils.logger.error(e.toString());
        }
        return entries;
    }

    private static List<Entry> getAllEntriesForWordLessEqualToL(String word) {
        ArrayList<Entry> entries = new ArrayList<>();
        String sql = "SELECT * FROM entries_less_equal_to_L WHERE entry_word = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, word);
            ResultSet resultSet = statement.executeQuery();

            do entries.add(getEntry(resultSet)); while (resultSet.next());
        } catch (SQLException e) {
            Utils.logger.error(e.toString());
        }
        return entries;
    }

    private static Entry getEntry(ResultSet resultSet) throws SQLException {
        Entry entry = new Entry();
        entry.setEntryId(resultSet.getString("entry_id"));
        entry.setWord(resultSet.getString("entry_word"));
        entry.setPlural(resultSet.getString("entry_plural"));
        entry.setTenses(resultSet.getString("entry_tenses"));
        entry.setCompare(resultSet.getString("entry_compare"));
        entry.setPartOfSpeech(resultSet.getString("entry_part_of_speech"));
        entry.setDefinitions(Utils.toStringArray(resultSet.getString("entry_definitions")));
        entry.setSynonyms(Utils.toStringArray(resultSet.getString("entry_synonyms")));
        entry.setAntonyms(Utils.toStringArray(resultSet.getString("entry_antonyms")));
        entry.setHypernyms(Utils.toStringArray(resultSet.getString("entry_hypernyms")));
        entry.setHyponyms(Utils.toStringArray(resultSet.getString("entry_hyponyms")));
        entry.setHomophones(Utils.toStringArray(resultSet.getString("entry_homophones")));
        return entry;
    }

    private static ArrayList<Entry> getDistinct(ArrayList<Entry> entries) {
        return entries.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }
}
