package uni.textimager.sandbox.generators;

import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import uni.textimager.sandbox.database.DBConstants;
import uni.textimager.sandbox.sources.DBAccess;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class TextFormatting extends Generator implements TextFormattingInterface {

    public static final String DEFAULT_STYLE = "underline";

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
        try (Connection connection = dbAccess.getDataSource().getConnection()) {
            DSLContext dsl = DSL.using(connection);

            dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_TEXT),
                            DSL.field(DBConstants.TABLEATTR_GENERATORID),
                            DSL.field(DBConstants.TABLEATTR_GENERATORDATA_TEXT))
                    .values(id, text).execute();

            if (datasets == null || datasets.isEmpty()) return;

            for (Dataset ds : datasets) {
                List<Query> insertsBatch = new ArrayList<>();

                dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_TYPESTYLE),
                                DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_TYPE),
                                DSL.field(DBConstants.TABLEATTR_GENERATORDATA_STYLE))
                        .values(id, ds.columnType, ds.style).execute();

                for (Map.Entry<String, Color> entry : ds.categoryColorMap.entrySet()) {
                    String category = entry.getKey();
                    Color colorObj = entry.getValue();
                    String color = String.format("#%02x%02x%02x", colorObj.getRed(), colorObj.getGreen(), colorObj.getBlue());
                    insertsBatch.add(dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_TYPECATEGORYCOLOR),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_TYPE),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_COLOR))
                            .values(id, ds.columnType, category, color));
                }
                dsl.batch(insertsBatch).execute();
                insertsBatch.clear();

                for (Dataset.Segment s : ds.segments) {
                    insertsBatch.add(dsl.insertInto(DSL.table(DBConstants.TABLENAME_GENERATORDATA_TYPESEGMENTS),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORID),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_TYPE),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_BEGIN),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_END),
                                    DSL.field(DBConstants.TABLEATTR_GENERATORDATA_CATEGORY))
                            .values(id, ds.columnType, s.begin, s.end, s.category));
                }
                dsl.batch(insertsBatch).execute();
                insertsBatch.clear();
            }
        }
    }

    public static class Dataset {
        private final String columnType;
        private final String style;
        private final Map<String, Color> categoryColorMap;
        private final List<Segment> segments;

        public record Segment(int begin, int end, String category) {}

        public Dataset(String categoryName, String style, Map<String, Color> categoryColorMap, List<Segment> segments) {
            this.columnType = categoryName;
            this.style = style;
            this.categoryColorMap = categoryColorMap;
            this.segments = segments;
        }

        public Dataset(Dataset copyOf) {
            this.columnType = copyOf.columnType;
            this.style = copyOf.style;
            this.categoryColorMap = new HashMap<>(copyOf.categoryColorMap);
            this.segments = new ArrayList<>(copyOf.segments);
        }


    }
}
