package io.github.honhimw.jddl.manual;

import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.anno.*;
import io.github.honhimw.jman.ManualImmutablePropImpl;
import io.github.honhimw.jman.ManualTypeBuilder;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-11-11
 */

public class TableBuilder extends ManualTypeBuilder<TableBuilder, Column, TableBuilder.FK> {

    private final DDLUtils.DefaultTableDef tableDef = new DDLUtils.DefaultTableDef();

    public static TableBuilder of(String tableName) {
        TableBuilder builder = new TableBuilder();
        return builder.tableName(tableName);
    }

    private TableBuilder() {
        super();
        type.annotations = new Annotation[]{tableDef};
    }

    /**
     * Set the comment on table.
     *
     * @param comment comment
     * @return the current instance
     */
    public TableBuilder comment(String comment) {
        tableDef.comment = comment;
        return self();
    }

    /**
     * Set the table-type for MySQL e.g. InnoDB
     *
     * @param tableType MySQL table type
     * @return the current instance
     */
    public TableBuilder tableType(String tableType) {
        tableDef.tableType = tableType;
        return self();
    }

    /**
     * Add index on the table.
     *
     * @param index index definition
     * @return the current instance
     */
    public TableBuilder addIndex(Index index) {
        List<Index> list = asList(tableDef.indexes);
        list.add(index);
        tableDef.indexes = list.toArray(new Index[0]);
        return self();
    }

    /**
     * Add index on the table.
     *
     * @param kind    columns reference kind
     * @param columns index columns
     * @return the current instance
     */
    public TableBuilder addIndex(Kind kind, String... columns) {
        List<Index> list = asList(tableDef.indexes);
        DDLUtils.DefaultIndex defaultIndex = new DDLUtils.DefaultIndex(columns);
        defaultIndex.kind = kind;
        list.add(defaultIndex);
        tableDef.indexes = list.toArray(new Index[0]);
        return self();
    }

    /**
     * Add unique constraint on the table.
     *
     * @param unique unique definition
     * @return the current instance
     */
    public TableBuilder addUnique(Unique unique) {
        List<Unique> list = asList(tableDef.uniques);
        list.add(unique);
        tableDef.uniques = list.toArray(new Unique[0]);
        return self();
    }

    /**
     * Add unique constraint on the table.
     *
     * @param kind    columns reference kind
     * @param columns unique columns
     * @return the current instance
     */
    public TableBuilder addUnique(Kind kind, String... columns) {
        List<Unique> list = asList(tableDef.uniques);
        DDLUtils.DefaultUnique defaultUnique = new DDLUtils.DefaultUnique();
        defaultUnique.kind = kind;
        defaultUnique.columns = columns;
        list.add(defaultUnique);
        tableDef.uniques = list.toArray(new Unique[0]);
        return self();
    }

    /**
     * Add check constraint on the table.
     *
     * @param check check definition
     * @return the current instance
     */
    public TableBuilder addCheck(Check check) {
        List<Check> list = asList(tableDef.checks);
        list.add(check);
        tableDef.checks = list.toArray(new Check[0]);
        return this;
    }

    /**
     * Add check constraint on the table.
     *
     * @param check check constraint content
     * @return the current instance
     */
    public TableBuilder addCheck(String check) {
        List<Check> list = asList(tableDef.checks);
        list.add(new DDLUtils.DefaultCheck(check));
        tableDef.checks = list.toArray(new Check[0]);
        return self();
    }

    @Override
    protected Column propBuilder() {
        return new Column();
    }

    @Override
    protected Column propBuilder(ManualImmutablePropImpl prop) {
        return new Column(prop);
    }

    @Override
    protected FK fkBuilder() {
        return new FK();
    }

    public static class FK extends ManualTypeBuilder.FK<FK, Column> {
        private FK() {

        }

        /**
         * Foreign key on delete action
         *
         * @param action on delete action
         * @return the current instance
         */
        public FK action(OnDeleteAction action) {
            this.self(self -> {
                DDLUtils.DefaultRelation foreignKey = new DDLUtils.DefaultRelation();
                foreignKey.action = action;
                self.columnDef.foreignKey = foreignKey;
            });
            return self();
        }
    }

}
