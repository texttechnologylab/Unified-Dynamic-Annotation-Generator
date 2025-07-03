package uni.textimager.sandbox.importer.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import uni.textimager.sandbox.importer.EntityRecord;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class XmiParserService {
    private final NameSanitizer nameSanitizer;
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public XmiParserService(NameSanitizer nameSanitizer) {
        this.nameSanitizer = nameSanitizer;
    }

    public List<EntityRecord> parse(Path file) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file.toFile());
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        List<EntityRecord> records = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element el = (Element) node;
            String entity = nameSanitizer.toClassName(el.getTagName());
            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("filename", file.getFileName().toString());
            NamedNodeMap rawAttrs = el.getAttributes();
            for (int j = 0; j < rawAttrs.getLength(); j++) {
                String raw = rawAttrs.item(j).getNodeName();
                String col = nameSanitizer.sanitize(raw);
                attrs.put(col, el.getAttribute(raw));
            }
            records.add(new EntityRecord(entity, attrs));
        }
        return records;
    }
}
