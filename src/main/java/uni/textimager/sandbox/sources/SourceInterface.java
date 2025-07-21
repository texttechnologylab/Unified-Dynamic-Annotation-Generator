package uni.textimager.sandbox.sources;

import uni.textimager.sandbox.generators.Generator;

import java.sql.SQLException;
import java.util.Collection;

public interface SourceInterface {
    Collection<Generator> createGenerators() throws SQLException;
}
