import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

public class Utils {

    public static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String randomWord = DB.getRandomWord().join();

    public static String getUnderLinedText(String text) {
        return String.format("<HTML><U>%s</U></HTML>", text);
    }

    public static String getBoldedHtmlText(String text) {
        return String.format("<b>%s</b>", text);
    }

    public static String getHtmlText(String text) {
        return String.format("<p>%s</p>", text);
    }

    public static String getHtmlListDefinition(ArrayList<String> list) {
        StringBuilder builder = new StringBuilder("<ol>");
        for (String item : list)
            builder.append(String.format("<li>%s</li>", item));
        builder.append("</ol>");
        return builder.toString();
    }

    public static String getHtmlList(ArrayList<String> list) {
        StringBuilder builder = new StringBuilder("<ul>");
        for (String item : list)
            builder.append(String.format("<li>%s</li>", item));
        builder.append("</ul>");
        return builder.toString();
    }

    public static String getStyle() {
        return """
                <head>
                l<style>
                b {font-size: 30pt;color: rgb(119,91,66); font-family: JetBrains Mono;}
                p {font-size: 25pt;color: rgb(119,91,66); font-family: JetBrains Mono;}
                li {font-size: 20pt;color: rgb(119,91,66); font-family: JetBrains Mono;}
                hr {background-color: rgb(119,91,66);}
                </style>
                </head>""";
    }

    public static String getEntryHtml(Entry entry) {
        StringBuilder builder = new StringBuilder();
        builder.append(getBoldedHtmlText(entry.getWord()));
        builder.append(getHtmlText(getPartOfSpeech(entry.getPartOfSpeech())));

        if (entry.getCompare() != null) builder.append(getHtmlText(entry.getCompare()));
        if (entry.getPlural() != null) builder.append(getHtmlText(entry.getPlural()));
        if (entry.getTenses() != null) builder.append(getHtmlText(entry.getTenses()));

        builder.append(getHtmlText("Definition"));
        builder.append(getHtmlListDefinition(entry.getDefinitions()));

        if (!entry.getSynonyms().isEmpty()) {
            builder.append(getHtmlText("Synonyms"));
            builder.append(getHtmlList(entry.getSynonyms()));
        }
        if (!entry.getAntonyms().isEmpty()) {
            builder.append(getHtmlText("Antonyms"));
            builder.append(getHtmlList(entry.getAntonyms()));
        }
        if (!entry.getHypernyms().isEmpty()) {
            builder.append(getHtmlText("Hypernyms"));
            builder.append(getHtmlList(entry.getHypernyms()));
        }
        if (!entry.getHyponyms().isEmpty()) {
            builder.append(getHtmlText("Hyponyms"));
            builder.append(getHtmlList(entry.getHyponyms()));
        }
        if (!entry.getHomophones().isEmpty()) {
            builder.append(getHtmlText("Homophones"));
            builder.append(getHtmlList(entry.getHomophones()));
        }

        builder.append("<hr>");
        return builder.toString();
    }

    public static String getEntriesHtml(ArrayList<Entry> entries) {
        StringBuilder builder = new StringBuilder("<html>");
        builder.append(getStyle());
        for (Entry entry : entries)
            builder.append(getEntryHtml(entry));
        builder.append("</html>");
        return builder.toString();
    }

    public static ArrayList<String> toStringArray(String value) {
        if (value == null) return null;

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(value, type);
    }

    public static String getPartOfSpeech(String abbreviation) {
        return switch (abbreviation) {
            case "n" -> "Noun";
            case "prp" -> "Preposition";
            case "adj" -> "Adjective";
            case "adv" -> "Adverb";
            case "prn" -> "Pronoun";
            case "v" -> "Verb";
            case "cn" -> "Conjunction";
            case "int" -> "Interjection";
            case "pct" -> "Punctuation";
            case "prt" -> "Particle";
            case "ar" -> "Article";
            case "dt" -> "Determiner";
            case "prv" -> "Proverb";
            case "sf" -> "Suffix";
            case "prf" -> "Prefix";
            case "intf" -> "Interfix";
            case "inf" -> "Infix";
            case "sm" -> "Symbol";
            case "ph" -> "Phrase";
            case "ab" -> "Abbreviation";
            case "af" -> "Affix";
            case "ch" -> "Character";
            case "cr" -> "Circumfix";
            case "nm" -> "Name";
            case "num" -> "Numeral";
            case "pp" -> "Postposition";
            case "prpp" -> "Prepositional phrase";
            default -> abbreviation;
        };
    }

    public static void openLink(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(URI.create(url));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void setImage(JFrame jFrame) {
        try {
            String path = "/fld_floating_dictionary.png";
            Image image = ImageIO.read(Objects.requireNonNull(Utils.class.getResource(path)));
            jFrame.setIconImage(image);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void setAttributes(JFrame jFrame) {
        jFrame.setTitle("FLD Floating Dictionary");
        jFrame.setVisible(true);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setImage(jFrame);
    }
}
