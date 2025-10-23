package io.github.honhimw.jddl;

import io.github.honhimw.jddl.anno.TableDef;
import io.github.honhimw.jddl.manual.ManualImmutableTypeImpl;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * @author honhimW
 */

class BufferContext {

    public final StringBuilder buf;

    public final JSqlClientImplementor client;

    public final ImmutableType tableType;

    public final Map<String, ImmutableProp> allDefinitionProps;

    public final List<ImmutableProp> definitionProps;

    public final String tableName;

    public final List<String> commentStatements;

    public final boolean uppercase;

    @Nullable
    private TableDef tableDef;

    private final Map<Class<? extends ConstraintNamingStrategy>, ConstraintNamingStrategy> namingStrategies;

    public BufferContext(JSqlClientImplementor client, ImmutableType tableType) {
        this.buf = new StringBuilder();
        this.client = client;
        this.tableType = tableType;
        this.tableName = tableType.getTableName(client.getMetadataStrategy());
        this.uppercase = tableName.equals(tableName.toUpperCase());
        this.commentStatements = new ArrayList<>();
        this.namingStrategies = new HashMap<>();
        this.definitionProps = new ArrayList<>();
        this.allDefinitionProps = DDLUtils.allDefinitionProps(tableType);
        this.definitionProps.addAll(this.allDefinitionProps.values());
        if (tableType instanceof ManualImmutableTypeImpl) {
            ManualImmutableTypeImpl _tableType = (ManualImmutableTypeImpl) tableType;
            tableDef = _tableType.getAnnotation(TableDef.class);
        } else {
            if (tableType.getJavaClass().isAnnotationPresent(TableDef.class)) {
                tableDef = tableType.getJavaClass().getAnnotation(TableDef.class);
            }
        }
    }

    public Optional<TableDef> getTableDef() {
        return Optional.ofNullable(tableDef);
    }

    public ConstraintNamingStrategy getNamingStrategy(Class<? extends ConstraintNamingStrategy> namingStrategy) {
        return this.namingStrategies.compute(namingStrategy, (aClass, ns) -> {
            if (ns == null) {
                try {
                    ns = aClass.getConstructor().newInstance();
                    ns.uppercase = this.uppercase;
                } catch (Exception e) {
                    throw new IllegalArgumentException("NamingStrategy doesn't have a no-arg constructor");
                }
            }
            return ns;
        });
    }

}
