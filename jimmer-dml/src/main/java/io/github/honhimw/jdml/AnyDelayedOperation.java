package io.github.honhimw.jdml;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JoinType;
import org.babyfish.jimmer.sql.ast.impl.base.BaseTableOwner;
import org.babyfish.jimmer.sql.ast.impl.table.RootTableResolver;
import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor;
import org.babyfish.jimmer.sql.ast.impl.table.WeakJoinHandle;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.spi.AbstractTypedTable;

/**
 * @author honhimW
 * @since 2025-10-31
 */

public class AnyDelayedOperation implements AbstractTypedTable.DelayedOperation<Object> {

    private final AbstractTypedTable<?> parent;
    private final ImmutableProp prop;
    private final JoinType joinType;
    private final ImmutableType treatedAs;

    public AnyDelayedOperation(AbstractTypedTable<?> parent, ImmutableProp prop, JoinType joinType, ImmutableType treatedAs) {
        this.parent = parent;
        this.prop = prop;
        this.joinType = joinType;
        this.treatedAs = treatedAs;
    }

    public AnyDelayedOperation(AnyDelayedOperation base, BaseTableOwner baseTableOwner) {
        this.parent = (AbstractTypedTable<?>) base.parent.__baseTableOwner(baseTableOwner);
        this.joinType = base.joinType;
        this.treatedAs = base.treatedAs;
        this.prop = base.prop;
    }

    @Override
    public Table<?> parent() {
        return parent;
    }

    @Override
    public ImmutableProp prop() {
        return prop;
    }

    @Override
    public WeakJoinHandle weakJoinHandle() {
        return null;
    }

    @Override
    public ImmutableType targetType() {
        if (treatedAs != null) {
            return treatedAs;
        }
        if (prop != null) {
            return prop.getTargetType();
        }
        return null;
    }

    @Override
    public TableImplementor<Object> resolve(RootTableResolver ctx) {
        TableImplementor<Object> tableImplementor;
        if (prop != null) {
            tableImplementor = parent.__resolve(ctx).joinImplementor(prop.getName(), joinType, treatedAs);
            return tableImplementor.baseTableOwner(parent.__baseTableOwner());
        }
        return null;
    }

    @Override
    public AbstractTypedTable.DelayedOperation<Object> baseTableOwner(BaseTableOwner baseTableOwner) {
        return new AnyDelayedOperation(
            this,
            baseTableOwner
        );
    }
}
