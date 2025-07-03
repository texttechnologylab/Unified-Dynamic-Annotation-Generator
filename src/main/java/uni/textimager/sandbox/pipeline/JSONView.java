package uni.textimager.sandbox.pipeline;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;


public class JSONView implements Iterable<JSONView> {

    private final Object node;

    public JSONView(Object node) {
        this.node = node;
    }


    /** Is the current node a Map? */
    public boolean isMap() {
        return node instanceof Map<?,?>;
    }

    /** Is the current node a List? */
    public boolean isList() {
        return node instanceof List<?>;
    }

    /** Is the current node a primitive or null? */
    public boolean isValue() {
        return !isMap() && !isList();
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> asMap() {
        if (!isMap()) {
            throw new IllegalStateException("Not a JSON object: " + node);
        }
        return (Map<String,Object>) node;
    }


    @SuppressWarnings("unchecked")
    public List<Object> asList() {
        if (!isList()) {
            throw new IllegalStateException("Not a JSON array: " + node);
        }
        return (List<Object>) node;
    }


    public JSONView get(String key) {
        Map<String,Object> m = asMap();
        if (!m.containsKey(key)) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        return new JSONView(m.get(key));
    }


    public JSONView get(int index) {
        List<Object> l = asList();
        if (index < 0 || index >= l.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for list of size " + l.size());
        }
        return new JSONView(l.get(index));
    }

    /** Returns the raw node (could be Map, List, String, Number, Boolean, or null). */
    public Object raw() {
        return node;
    }


    /** Iterable: if this is a list, iterate its elements as JSONViews, otherwise empty. */
    @Override
    public Iterator<JSONView> iterator() {
        if (!isList()) {
            return Collections.emptyIterator();
        }
        Iterator<Object> rawIt = asList().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return rawIt.hasNext();
            }

            @Override
            public JSONView next() {
                return new JSONView(rawIt.next());
            }
        };
    }

    /** Returns a Stream of JSONView elements if this node is a list, otherwise an empty. */
    public Stream<JSONView> stream() {
        if (!isList()) {
            return Stream.empty();
        }
        return asList().stream().map(JSONView::new);
    }



    public void printStructure() {
        printStructure(node, 0);
    }


    @SuppressWarnings("unchecked")
    public static void printStructure(Object obj, int indent) {
        String indentStr = " ".repeat(indent);

        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            System.out.println(indentStr + "{");
            int count = 0, size = map.size();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                System.out.print(indentStr + "  \"" + entry.getKey() + "\": ");
                printStructure(entry.getValue(), indent + 2);
                if (++count < size) System.out.print(",");
                System.out.println();
            }
            System.out.print(indentStr + "}");
        }
        else if (obj instanceof List<?>) {
            List<Object> list = (List<Object>) obj;
            System.out.println(indentStr + "[");
            for (int i = 0; i < list.size(); i++) {
                printStructure(list.get(i), indent + 2);
                if (i < list.size() - 1) System.out.print(",");
                System.out.println();
            }
            System.out.print(indentStr + "]");
        }
        else if (obj instanceof String) {
            System.out.print(indentStr + "\"" + obj + "\"");
        }
        else {
            // covers Number, Boolean, null, etc.
            System.out.print(indentStr + (obj == null ? "null" : obj.toString()));
        }
    }


    @Override
    public String toString() {
        if (isValue() || node == null) {
            return Objects.toString(node);
        }
        return node.toString();
    }
}
