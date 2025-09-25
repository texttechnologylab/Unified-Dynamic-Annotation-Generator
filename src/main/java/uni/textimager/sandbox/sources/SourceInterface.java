package uni.textimager.sandbox.sources;

import uni.textimager.sandbox.generators.Generator;

import java.sql.SQLException;
import java.util.Map;

public interface SourceInterface {
    Map<String, Generator> createGenerators() throws SQLException;
}
