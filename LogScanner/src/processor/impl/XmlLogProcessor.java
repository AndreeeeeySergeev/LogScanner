package processor.impl;

import model.LogEvent;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import processor.LogProcessor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.function.Consumer;

public class XmlLogProcessor implements LogProcessor {

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

            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) {

                buffer.setLength(0);

                for (int i = 0; i < attributes.getLength(); i++) {
                    String value = attributes.getValue(i);

                    if (value != null && !value.isBlank()) {
                        consumer.accept(new LogEvent(null, null, null, value));
                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                buffer.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {

                String text = buffer.toString().trim();

                if (!text.isEmpty()) {
                    consumer.accept(new LogEvent(null, null, null, text));
                }
            }
        };

        saxParser.parse(new File(filePath), handler);
    }
}