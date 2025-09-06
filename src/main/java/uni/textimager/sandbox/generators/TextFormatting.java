package uni.textimager.sandbox.generators;

import lombok.Getter;
import uni.textimager.sandbox.sources.DBAccess;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class TextFormatting extends Generator implements TextFormattingInterface {

    public static final String DEFAULT_STYLE = "highlight";

    @Getter
    private final String filename;
    private final String sofaID;
    private final String text;
    private final Collection<Dataset> datasets;


    public TextFormatting(String id, String filename, String sofaID, String text, Collection<Dataset> datasets) {
        super(id);
        this.filename = filename;
        this.sofaID = sofaID;
        this.text = text;
        this.datasets = datasets;
    }

    public TextFormatting(String id, TextFormatting copyOf) {
        super(id);
        this.filename = copyOf.filename;
        this.sofaID = copyOf.sofaID;
        this.text = copyOf.text;
        this.datasets = new ArrayList<>();
        for (Dataset dataset : copyOf.datasets) {
            this.datasets.add(new Dataset(dataset));
        }
    }

    @Override
    public TextFormatting copy(String id) {
        return new TextFormatting(id, this);
    }

    @Override
    public void saveToDB(DBAccess dbAccess) throws SQLException {

    }

    public static class Dataset {
        private final String annotationType;
        private final String style;
        private final Map<String, Color> categoryColorMap;
        private final List<Segment> segments;

        public record Segment(int begin, int end, String category) {}

        public Dataset(String categoryName, String style, Map<String, Color> categoryColorMap, List<Segment> segments) {
            this.annotationType = categoryName;
            this.style = style;
            this.categoryColorMap = categoryColorMap;
            this.segments = segments;
        }

        public Dataset(Dataset copyOf) {
            this.annotationType = copyOf.annotationType;
            this.style = copyOf.style;
            this.categoryColorMap = new HashMap<>(copyOf.categoryColorMap);
            this.segments = new ArrayList<>(copyOf.segments);
        }


    }
}
