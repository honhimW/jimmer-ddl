package io.github.honhimw.jddl;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-07-11
 */

public class SchemaValidator {

    private final JSqlClientImplementor client;

    public SchemaValidator(JSqlClientImplementor client) {
        this.client = client;
    }

    @Nullable
    private DatabaseVersion databaseVersion;

    public JSqlClientImplementor getClient() {
        return client;
    }

    public DatabaseVersion getDatabaseVersion() {
        if (databaseVersion == null) {
            databaseVersion = DDLUtils.getDatabaseVersion(client);
        }
        return databaseVersion;
    }

    public Schemas load(Collection<ImmutableType> immutableTypes) {
        return client.getConnectionManager().execute(connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                List<Table> tables = new ArrayList<>();
                for (ImmutableType immutableType : immutableTypes) {
                    String tableSchema = client.getMetadataStrategy().getSchemaStrategy().tableSchema(immutableType);
                    String tableName = immutableType.getTableName(client.getMetadataStrategy());
                    if (metaData.storesUpperCaseIdentifiers()) {
                        tableName = tableName.toUpperCase();
                    } else if (metaData.storesLowerCaseIdentifiers()) {
                        tableName = tableName.toLowerCase();
                    }
                    ResultSet table = metaData.getTables(null, tableSchema, tableName, null);
                    List<Map<String, Object>> result = toMap(table);

                    if (!result.isEmpty()) {
                        Map<String, Object> first = result.get(0);
                        List<Column> columns = new ArrayList<>();
                        List<Map<String, Object>> columnResult2 = toMap(metaData.getColumns(null, tableSchema, tableName, null));
                        for (Map<String, Object> map : columnResult2) {
                            columns.add(
                                new Column(
                                    cast(map.get("TABLE_CAT"), String.class),
                                    cast(map.get("TABLE_SCHEM"), String.class),
                                    cast(map.get("TABLE_NAME"), String.class),
                                    cast(map.get("COLUMN_NAME"), String.class),
                                    cast(map.get("DATA_TYPE"), Integer.class),
                                    cast(map.get("TYPE_NAME"), String.class),
                                    cast(map.get("COLUMN_SIZE"), Integer.class),
                                    cast(map.get("DECIMAL_DIGITS"), Integer.class),
                                    cast(map.get("NUM_PREC_RADIX"), Integer.class),
                                    cast(map.get("NULLABLE"), Integer.class),
                                    cast(map.get("NULLABLE"), String.class),
                                    cast(map.get("COLUMN_DEF"), String.class),
                                    cast(map.get("CHAR_OCTET_LENGTH"), Integer.class),
                                    cast(map.get("ORDINAL_POSITION"), Integer.class),
                                    cast(map.get("IS_NULLABLE"), String.class),
                                    cast(map.get("SCOPE_CATALOG"), String.class),
                                    cast(map.get("SCOPE_SCHEMA"), String.class),
                                    cast(map.get("SCOPE_TABLE"), String.class),
                                    cast(map.get("SOURCE_DATA_TYPE"), Short.class),
                                    cast(map.get("IS_AUTOINCREMENT"), String.class),
                                    cast(map.get("IS_GENERATEDCOLUMN"), String.class)
                                )
                            );
                        }
                        tables.add(new Table(
                            cast(first.get("TABLE_CAT"), String.class),
                            cast(first.get("TABLE_SCHEM"), String.class),
                            cast(first.get("TABLE_NAME"), String.class),
                            cast(first.get("TABLE_TYPE"), String.class),
                            cast(first.get("REMARKS"), String.class),
                            cast(first.get("TYPE_CAT"), String.class),
                            cast(first.get("TYPE_SCHEM"), String.class),
                            cast(first.get("TYPE_NAME"), String.class),
                            cast(first.get("SELF_REFERENCING_COL_NAME"), String.class),
                            cast(first.get("REF_GENERATION"), String.class),
                            columns
                        ));

                    }
                }
                return new Schemas(tables, metaData.storesLowerCaseIdentifiers(), metaData.storesUpperCaseIdentifiers(), metaData.storesMixedCaseIdentifiers());
            } catch (Exception e) {
                throw new IllegalStateException("cannot get tables", e);
            }
        });

    }

    @NullUnmarked
    public static final class Table {
        private final String tableCatalog;
        private final String tableSchema;
        private final String tableName;
        private final String tableType;
        private final String remarks;
        private final String typeCatalog;
        private final String typeSchema;
        private final String typeName;
        private final String selfReferencingColName;
        private final String refGeneration;
        private final List<Column> columns;

        public Table(String tableCatalog, String tableSchema, String tableName, String tableType, String remarks,
                     String typeCatalog, String typeSchema, String typeName, String selfReferencingColName,
                     String refGeneration, List<Column> columns) {
            this.tableCatalog = tableCatalog;
            this.tableSchema = tableSchema;
            this.tableName = tableName;
            this.tableType = tableType;
            this.remarks = remarks;
            this.typeCatalog = typeCatalog;
            this.typeSchema = typeSchema;
            this.typeName = typeName;
            this.selfReferencingColName = selfReferencingColName;
            this.refGeneration = refGeneration;
            this.columns = columns;
        }

        public String tableCatalog() {
            return tableCatalog;
        }

        public String tableSchema() {
            return tableSchema;
        }

        public String tableName() {
            return tableName;
        }

        public String tableType() {
            return tableType;
        }

        public String remarks() {
            return remarks;
        }

        public String typeCatalog() {
            return typeCatalog;
        }

        public String typeSchema() {
            return typeSchema;
        }

        public String typeName() {
            return typeName;
        }

        public String selfReferencingColName() {
            return selfReferencingColName;
        }

        public String refGeneration() {
            return refGeneration;
        }

        public List<Column> columns() {
            return columns;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Table that = (Table) obj;
            return Objects.equals(this.tableCatalog, that.tableCatalog) &&
                   Objects.equals(this.tableSchema, that.tableSchema) &&
                   Objects.equals(this.tableName, that.tableName) &&
                   Objects.equals(this.tableType, that.tableType) &&
                   Objects.equals(this.remarks, that.remarks) &&
                   Objects.equals(this.typeCatalog, that.typeCatalog) &&
                   Objects.equals(this.typeSchema, that.typeSchema) &&
                   Objects.equals(this.typeName, that.typeName) &&
                   Objects.equals(this.selfReferencingColName, that.selfReferencingColName) &&
                   Objects.equals(this.refGeneration, that.refGeneration) &&
                   Objects.equals(this.columns, that.columns);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tableCatalog, tableSchema, tableName, tableType, remarks, typeCatalog, typeSchema, typeName, selfReferencingColName, refGeneration, columns);
        }

        @Override
        public String toString() {
            return "Table[" +
                   "tableCatalog=" + tableCatalog + ", " +
                   "tableSchema=" + tableSchema + ", " +
                   "tableName=" + tableName + ", " +
                   "tableType=" + tableType + ", " +
                   "remarks=" + remarks + ", " +
                   "typeCatalog=" + typeCatalog + ", " +
                   "typeSchema=" + typeSchema + ", " +
                   "typeName=" + typeName + ", " +
                   "selfReferencingColName=" + selfReferencingColName + ", " +
                   "refGeneration=" + refGeneration + ", " +
                   "columns=" + columns + ']';
        }


    }

    @NullUnmarked
    public static final class Column {
        private final String tableCatalog;
        private final String tableSchema;
        private final String tableName;
        private final String columnName;
        private final Integer dataType;
        private final String typeName;
        private final Integer columnSize;
        private final Integer decimalDigits;
        private final Integer numPrecisionRadix;
        private final Integer nullable;
        private final String remarks;
        private final String columnDef;
        private final Integer charOctetLength;
        private final Integer ordinalPosition;
        private final String isNullable;
        private final String scopeCatalog;
        private final String scopeSchema;
        private final String scopeTable;
        private final Short sourceDataType;
        private final String isAutoincrement;
        private final String isGeneratedColumn;

        public Column(String tableCatalog, String tableSchema, String tableName, String columnName, Integer dataType,
                      String typeName, Integer columnSize, Integer decimalDigits, Integer numPrecisionRadix,
                      Integer nullable, String remarks, String columnDef, Integer charOctetLength,
                      Integer ordinalPosition, String isNullable, String scopeCatalog, String scopeSchema,
                      String scopeTable, Short sourceDataType, String isAutoincrement, String isGeneratedColumn) {
            this.tableCatalog = tableCatalog;
            this.tableSchema = tableSchema;
            this.tableName = tableName;
            this.columnName = columnName;
            this.dataType = dataType;
            this.typeName = typeName;
            this.columnSize = columnSize;
            this.decimalDigits = decimalDigits;
            this.numPrecisionRadix = numPrecisionRadix;
            this.nullable = nullable;
            this.remarks = remarks;
            this.columnDef = columnDef;
            this.charOctetLength = charOctetLength;
            this.ordinalPosition = ordinalPosition;
            this.isNullable = isNullable;
            this.scopeCatalog = scopeCatalog;
            this.scopeSchema = scopeSchema;
            this.scopeTable = scopeTable;
            this.sourceDataType = sourceDataType;
            this.isAutoincrement = isAutoincrement;
            this.isGeneratedColumn = isGeneratedColumn;
        }

        public String tableCatalog() {
            return tableCatalog;
        }

        public String tableSchema() {
            return tableSchema;
        }

        public String tableName() {
            return tableName;
        }

        public String columnName() {
            return columnName;
        }

        public Integer dataType() {
            return dataType;
        }

        public String typeName() {
            return typeName;
        }

        public Integer columnSize() {
            return columnSize;
        }

        public Integer decimalDigits() {
            return decimalDigits;
        }

        public Integer numPrecisionRadix() {
            return numPrecisionRadix;
        }

        public Integer nullable() {
            return nullable;
        }

        public String remarks() {
            return remarks;
        }

        public String columnDef() {
            return columnDef;
        }

        public Integer charOctetLength() {
            return charOctetLength;
        }

        public Integer ordinalPosition() {
            return ordinalPosition;
        }

        public String isNullable() {
            return isNullable;
        }

        public String scopeCatalog() {
            return scopeCatalog;
        }

        public String scopeSchema() {
            return scopeSchema;
        }

        public String scopeTable() {
            return scopeTable;
        }

        public Short sourceDataType() {
            return sourceDataType;
        }

        public String isAutoincrement() {
            return isAutoincrement;
        }

        public String isGeneratedColumn() {
            return isGeneratedColumn;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Column that = (Column) obj;
            return Objects.equals(this.tableCatalog, that.tableCatalog) &&
                   Objects.equals(this.tableSchema, that.tableSchema) &&
                   Objects.equals(this.tableName, that.tableName) &&
                   Objects.equals(this.columnName, that.columnName) &&
                   Objects.equals(this.dataType, that.dataType) &&
                   Objects.equals(this.typeName, that.typeName) &&
                   Objects.equals(this.columnSize, that.columnSize) &&
                   Objects.equals(this.decimalDigits, that.decimalDigits) &&
                   Objects.equals(this.numPrecisionRadix, that.numPrecisionRadix) &&
                   Objects.equals(this.nullable, that.nullable) &&
                   Objects.equals(this.remarks, that.remarks) &&
                   Objects.equals(this.columnDef, that.columnDef) &&
                   Objects.equals(this.charOctetLength, that.charOctetLength) &&
                   Objects.equals(this.ordinalPosition, that.ordinalPosition) &&
                   Objects.equals(this.isNullable, that.isNullable) &&
                   Objects.equals(this.scopeCatalog, that.scopeCatalog) &&
                   Objects.equals(this.scopeSchema, that.scopeSchema) &&
                   Objects.equals(this.scopeTable, that.scopeTable) &&
                   Objects.equals(this.sourceDataType, that.sourceDataType) &&
                   Objects.equals(this.isAutoincrement, that.isAutoincrement) &&
                   Objects.equals(this.isGeneratedColumn, that.isGeneratedColumn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, numPrecisionRadix, nullable, remarks, columnDef, charOctetLength, ordinalPosition, isNullable, scopeCatalog, scopeSchema, scopeTable, sourceDataType, isAutoincrement, isGeneratedColumn);
        }

        @Override
        public String toString() {
            return "Column[" +
                   "tableCatalog=" + tableCatalog + ", " +
                   "tableSchema=" + tableSchema + ", " +
                   "tableName=" + tableName + ", " +
                   "columnName=" + columnName + ", " +
                   "dataType=" + dataType + ", " +
                   "typeName=" + typeName + ", " +
                   "columnSize=" + columnSize + ", " +
                   "decimalDigits=" + decimalDigits + ", " +
                   "numPrecisionRadix=" + numPrecisionRadix + ", " +
                   "nullable=" + nullable + ", " +
                   "remarks=" + remarks + ", " +
                   "columnDef=" + columnDef + ", " +
                   "charOctetLength=" + charOctetLength + ", " +
                   "ordinalPosition=" + ordinalPosition + ", " +
                   "isNullable=" + isNullable + ", " +
                   "scopeCatalog=" + scopeCatalog + ", " +
                   "scopeSchema=" + scopeSchema + ", " +
                   "scopeTable=" + scopeTable + ", " +
                   "sourceDataType=" + sourceDataType + ", " +
                   "isAutoincrement=" + isAutoincrement + ", " +
                   "isGeneratedColumn=" + isGeneratedColumn + ']';
        }


    }

    public static class Schemas {
        public static final Schemas EMPTY = new Schemas(Collections.emptyList(), false, false, true);

        private final List<Table> tables;
        private final Map<String, Table> tableMap;
        public final boolean storesLowerCaseIdentifiers;
        public final boolean storesUpperCaseIdentifiers;
        public final boolean storesMixedCaseIdentifiers;

        private Schemas(List<Table> tables, boolean storesLowerCaseIdentifiers, boolean storesUpperCaseIdentifiers, boolean storesMixedCaseIdentifiers) {
            this.tables = tables;
            this.tableMap = tables.stream().collect(Collectors.toMap(Table::tableName, table -> table));
            this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
            this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
            this.storesMixedCaseIdentifiers = storesMixedCaseIdentifiers;
        }

        @Nullable
        public Table get(String tableName) {
            if (storesLowerCaseIdentifiers) {
                tableName = tableName.toLowerCase();
            } else if (storesUpperCaseIdentifiers) {
                tableName = tableName.toUpperCase();
            }
            return tableMap.get(tableName);
        }

        @Nullable
        public Column get(String tableName, String columnName) {
            if (storesLowerCaseIdentifiers) {
                tableName = tableName.toLowerCase();
                columnName = columnName.toLowerCase();
            } else if (storesUpperCaseIdentifiers) {
                tableName = tableName.toUpperCase();
                columnName = columnName.toUpperCase();
            }
            Table table = tableMap.get(tableName);
            if (table != null) {
                List<Column> columns = table.columns();
                for (Column column : columns) {
                    if (columnName.equals(column.columnName())) {
                        return column;
                    }
                }
            }
            return null;
        }

        public List<Table> getTables() {
            return tables;
        }

        public Map<String, Table> getTableMap() {
            return tableMap;
        }
    }

    private static List<Map<String, Object>> toMap(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount + 1];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i] = metaData.getColumnLabel(i);
        }

        while (resultSet.next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                Object columnValue = resultSet.getObject(i);
                rowMap.put(columnNames[i], columnValue);
            }

            resultList.add(rowMap);
        }

        return resultList;
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T cast(@Nullable Object value, Class<T> type) {
        if (value == null) {
            return null;
        }
        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        if (type == String.class) {
            return (T) String.valueOf(value);
        }
        if (type == Integer.class || type == int.class) {
            if (value instanceof String) {
                return (T) Integer.valueOf((String) value);
            } else if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
        }
        return (T) value;
    }

}