package processor.impl;

import model.LogEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import processor.LogProcessor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class XmlLogProcessor implements LogProcessor {

    @Override
    public List<LogEvent> process(String filePath, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        SAXParserFactory factory = SAXParserFactory.newInstance();

        // безопасность
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {

            private StringBuilder currentText = new StringBuilder();

            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) {

                // очищаем буфер
                currentText.setLength(0);

                // обрабатываем атрибуты
                for (int i = 0; i < attributes.getLength(); i++) {
                    String value = attributes.getValue(i);

                    if (containsLevel(value, levels)) {
                        events.add(new LogEvent(
                                Instant.now(),
                                "XML",
                                "UNKNOWN",
                                value
                        ));
                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                currentText.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {

                String text = currentText.toString().trim();

                if (!text.isEmpty() && containsLevel(text, levels)) {
                    events.add(new LogEvent(
                            Instant.now(),
                            "XML",
                            "UNKNOWN",
                            text
                    ));
                }
            }
        };

        saxParser.parse(new File(filePath), handler);

        return events;
    }

    private boolean containsLevel(String text, List<String> levels) {

        String lower = text.toLowerCase();

        return levels.stream()
                .anyMatch(level -> lower.contains(level.toLowerCase()));
    }
}