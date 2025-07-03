package uni.textimager.sandbox.importer.dialect;

public interface SqlDialect {
    String varcharType(int length);

    String clobType();

    String autoIncrementPrimaryKey(String columnName);
}
