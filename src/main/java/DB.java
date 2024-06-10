import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DB {
    private static Connection getConnection() throws SQLException {
        String path = "jdbc:sqlite::resource:WiktionaryDatabase.db";
//        String path  = "jdbc:sqlite:resources/WiktionaryDatabase.db";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(path);
    }

    public static String[] getSimilarEntryWords(String word) {
        ArrayList<String> similarWords = new ArrayList<>(10);
        String sql = "SELECT entry_word FROM entry_words WHERE entry_word LIKE ? LIMIT 9";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, word + "%");
            ResultSet resultSet = statement.executeQuery();

            similarWords.add(word);
            do similarWords.add(resultSet.getString("entry_word")); while (resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return similarWords.stream().distinct().toArray((String[]::new));
    }

    public static boolean wordExist(String word) {
        String sql = "SELECT (COUNT(*) > 0) AS found FROM entry_words WHERE entry_word = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, word);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getBoolean("found");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getRandomWord() {
        String sql = "SELECT entry_word FROM entry_words ORDER BY random() LIMIT 1";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getString("entry_word");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "A";
    }

    private static ArrayList<String> getPreviousWords(String word) {
        ArrayList<String> previousWords = new ArrayList<>(20);
        String sql = "SELECT entry_word FROM entry_words WHERE entry_word < ? ORDER BY entry_word DESC LIMIT 20";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, word);

            ResultSet resultSet = statement.executeQuery();
            do previousWords.add(resultSet.getString("entry_word")); while (resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return previousWords;
    }

    private static ArrayList<String> getNextWords(String word) {
        ArrayList<String> nextWords = new ArrayList<>(20);
        String sql = "SELECT entry_word FROM entry_words WHERE entry_word > ? ORDER BY entry_word ASC LIMIT 20";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, word);

            ResultSet resultSet = statement.executeQuery();
            do nextWords.add(resultSet.getString("entry_word")); while (resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextWords;
    }

    public static ArrayList<String> getEntryWords(String word) {
        ArrayList<String> words = new ArrayList<>();
        if (wordExist(word)) {
            words.addAll(getPreviousWords(word));
            words.add(word);
            words.addAll(getNextWords(word));
            words.sort(String::compareToIgnoreCase);
        }
        return words;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
