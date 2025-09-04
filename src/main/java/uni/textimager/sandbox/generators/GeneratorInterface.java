package uni.textimager.sandbox.generators;

import uni.textimager.sandbox.sources.DBAccess;
import java.sql.SQLException;

public interface GeneratorInterface {
    GeneratorInterface copy(String id);
    void saveToDB(DBAccess dbAccess) throws SQLException;
}
