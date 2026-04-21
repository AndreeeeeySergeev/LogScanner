package processor.impl;

import model.LogEvent;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import processor.LogProcessor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class XmlLogProcessor implements LogProcessor {

    private final List<String> levels;

    public XmlLogProcessor(List<String> levels) {
        this.levels = levels;
    }

    @Override
    public void process(String filePath,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        SAXParserFactory factory = SAXParserFactory.newInstance();

        // безопасность
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {

            private StringBuilder buffer = new StringBuilder();
            private int depth = 0;

            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) {

                // начинаем новый event на верхнем уровне
                if (depth == 0) {
                    buffer.setLength(0);
                }

                depth++;

                buffer.append("<").append(qName);

                // атрибуты
                for (int i = 0; i < attributes.getLength(); i++) {
                    buffer.append(" ")
                            .append(attributes.getQName(i))
                            .append("=\"")
                            .append(attributes.getValue(i))
                            .append("\"");
                }

                buffer.append(">");
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                buffer.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {

                buffer.append("</").append(qName).append(">");

                depth--;

                // закрыли верхнеуровневый элемент → это событие
                if (depth == 0) {

                    String event = buffer.toString().trim();

                    if (!event.isEmpty() && matchesLevel(event)) {
                        consumer.accept(new LogEvent(event));
                    }

                    buffer.setLength(0);
                }
            }
        };

        saxParser.parse(new File(filePath), handler);
    }

    // PRE-FILTER (лёгкий)
    private boolean matchesLevel(String message) {

        if (levels == null || levels.isEmpty()) {
            return true;
        }

        String lower = message.toLowerCase();

        for (String level : levels) {
            if (lower.contains(level.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}