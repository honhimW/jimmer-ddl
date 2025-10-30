package io.github.honhimw.jdml;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.ast.impl.base.BaseTableOwner;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.ast.table.spi.AbstractTypedTable;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class AnyTableProxy extends AbstractTypedTable<Object> {

    public AnyTableProxy(ImmutableType type) {
        super(type);
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
}
