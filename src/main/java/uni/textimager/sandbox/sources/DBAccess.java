package uni.textimager.sandbox.sources;

import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import uni.textimager.sandbox.database.QueryHelper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DBAccess {
    @Getter
    private final DataSource dataSource;

    private Collection<String> sourceFiles;


    public DBAccess(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Set<String> getSourceFiles() throws SQLException {
        if (sourceFiles == null) {
            DSLContext dslContext = DSL.using(dataSource.getConnection());
            QueryHelper q = new QueryHelper(dslContext);

            Table<?> nullTable = q.table("cas");
            Field<Object> file = q.field("cas", "filename");

            Result<? extends org.jooq.Record> result = q.dsl()
                    .selectDistinct(file)
                    .from(nullTable)
                    .fetch();

            sourceFiles = new ArrayList<>();
            for (Record record : result) {
                Object value = record.getValue(file);
                if (value != null) {
                    sourceFiles.add(value.toString());
                }
            }
        }

        return new HashSet<>(sourceFiles);
    }
}
