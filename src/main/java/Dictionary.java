import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class Dictionary extends JFrame {
    private final CardLayout cardLayout;
    private JPanel dictionaryPanel;
    private JButton buttonSearch;
    private JButton buttonPrevious;
    private JButton buttonNext;
    private JButton buttonRandom;
    private JPanel cardPanel;
    private JComboBox<String> searchBox;

    public Dictionary(String word) {
        customizeSearchBox(searchBox);
        searchBox.setBorder(BorderFactory.createMatteBorder(1, 1, 3, 2, new Color(119, 91, 66)));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        JTextComponent textComponent = (JTextComponent) searchBox.getEditor().getEditorComponent();
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                populateAndShowPopUp();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                populateAndShowPopUp();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                populateAndShowPopUp();
            }
        });

        if (word != null) setEntries(word);
        else setEntries(Utils.randomWord);
        buttonPrevious.addActionListener(_ -> cardLayout.previous(cardPanel));
        buttonNext.addActionListener(_ -> cardLayout.next(cardPanel));

        buttonSearch.addActionListener(_ -> setEntries((String) searchBox.getEditor().getItem()));
        buttonRandom.addActionListener(_ -> {
            DB.getRandomWord().thenAccept(this::setEntries);
        });
    }

    public static void launchDictionary(String word) {
        Dictionary dictionary = new Dictionary(word);
        dictionary.setContentPane(dictionary.dictionaryPanel);
        dictionary.setSize(600, 700);
        Utils.setAttributes(dictionary);
    }

    private void customizeSearchBox(Container container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof AbstractButton) container.remove(component);
            if (component instanceof JComponent) ((JComponent) component).setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }

    private void setEntries(String text) {
        DB.wordExist(text).thenAccept(exists -> {
            if (exists) {
                DB.getEntryWords(text).thenAccept(entryWords -> {
                    cardPanel.removeAll();

                    for (String word : entryWords) {
                        String entries = Utils.getEntriesHtml(DB.getAllEntryWordsForWord(word));
                        JTextPane textPane = new JTextPane();
                        textPane.setContentType("text/html");
                        textPane.setText(entries);
                        textPane.setEditable(false);

                        JScrollPane scrollPane = new JScrollPane(textPane);
                        scrollPane.setName(word);
                        customizeScrollPane(scrollPane);
                        cardPanel.add(scrollPane, word);
                    }
                    cardLayout.show(cardPanel, text);
                });
            }
        });
    }

    private void customizeScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(119, 91, 66);
                this.thumbDarkShadowColor = new Color(255, 235, 205);
            }
        });

        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(119, 91, 66);
                this.thumbDarkShadowColor = new Color(255, 235, 205);
            }
        });
    }

    private void populateAndShowPopUp() {
        SwingUtilities.invokeLater(() -> {
            String query = (String) searchBox.getEditor().getItem();
            DB.getSimilarEntryWords(query).thenAccept(similarWords -> {
                DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>(similarWords);
                searchBox.setModel(boxModel);
                searchBox.showPopup();
            });
        });
    }
}
