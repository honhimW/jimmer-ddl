package io.github.honhimw.jdml;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JoinType;
import org.babyfish.jimmer.sql.ast.impl.AbstractMutableStatementImpl;
import org.babyfish.jimmer.sql.ast.impl.AstContext;
import org.babyfish.jimmer.sql.ast.impl.base.BaseTableOwner;
import org.babyfish.jimmer.sql.ast.impl.table.RootTableResolver;
import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.ast.table.spi.AbstractTypedTable;
import org.babyfish.jimmer.sql.ast.table.spi.TableLike;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class DynTableProxy extends AbstractTypedTable<Object> {

    private final Table<Object> parent;

    private final DelayedOperation<Object> delayedOperation;

    public DynTableProxy(ImmutableType type) {
        this(type, null, null);
    }

    public DynTableProxy(ImmutableType type, DelayedOperation<Object> delayedOperation) {
        this(type, null, delayedOperation);
    }

    public DynTableProxy(ImmutableType type, Table<Object> parent) {
        this(type, parent, null);
    }

    public DynTableProxy(ImmutableType type, Table<Object> parent, DelayedOperation<Object> delayedOperation) {
        super(type);
        this.parent = parent;
        this.delayedOperation = delayedOperation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends TableProxy<Object>> P __disableJoin(String reason) {
        return (P) this;
    }

    @Override
    public TableProxy<Object> __baseTableOwner(BaseTableOwner baseTableOwner) {
        return this;
    }

    @Override
    public TableEx<Object> asTableEx() {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(String prop) {
        return join(prop, JoinType.INNER, null);
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop) {
        return join(prop, JoinType.INNER, null);
    }

    @Override
    public <XT extends Table<?>> XT join(String prop, JoinType joinType) {
        return join(prop, joinType, null);
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop, JoinType joinType) {
        return join(prop, joinType, null);
    }

    @Override
    public <XT extends Table<?>> XT join(String prop, JoinType joinType, ImmutableType treatedAs) {
        ImmutableProp _prop = getImmutableType().getProp(prop);
        return join(_prop, joinType, treatedAs);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop, JoinType joinType, ImmutableType treatedAs) {
        return (XT) new DynTableProxy(prop.getTargetType(), this, new DynDelayedOperation(this, prop, joinType, treatedAs));
    }

    @Override
    public Table<?> __parent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TableImplementor<Object> __resolve(RootTableResolver resolver) {
        if (raw != null) {
            return raw;
        }
        if (delayedOperation != null) {
            return delayedOperation.resolve(resolver);
        }
        if (resolver == null) {
            throw new IllegalArgumentException("resolver cannot be null when the table proxy is not wrapper");
        }
        AbstractMutableStatementImpl statement = ((AstContext) resolver).getStatement();
        BaseTableOwner baseTableOwner = BaseTableOwner.of(this);
        for (AbstractMutableStatementImpl stat = statement; stat != null; stat = stat.getParent()) {
            TableLike<?> stmtTable = stat.getTable();
            if (AbstractTypedTable.__refEquals(stmtTable, this)) {
                TableImplementor<Object> tableImplementor = (TableImplementor<Object>) stat.getTableLikeImplementor();
                return tableImplementor.baseTableOwner(baseTableOwner);
            }
        }
        return super.__resolve(resolver);
    }

    @Override
    public String toString() {
        return getImmutableType().toString();
    }

    @Override
    public ImmutableProp __prop() {
        if (delayedOperation != null) {
            return delayedOperation.prop();
        }
        return super.__prop();
    }
}
