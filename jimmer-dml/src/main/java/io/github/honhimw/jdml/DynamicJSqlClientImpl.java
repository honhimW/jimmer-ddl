package io.github.honhimw.jdml;

import org.babyfish.jimmer.sql.ast.impl.mutation.ISimpleEntitySaveCommandImpl;
import org.babyfish.jimmer.sql.ast.mutation.SimpleEntitySaveCommand;
import org.babyfish.jimmer.sql.di.AbstractJSqlClientDelegate;
import org.babyfish.jimmer.sql.meta.IdGenerator;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.meta.impl.IdentityIdGenerator;
import org.babyfish.jimmer.sql.meta.impl.SequenceIdGenerator;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

/**
 * @author honhimW
 * @since 2025-10-30
 */

public class DynamicJSqlClientImpl extends AbstractJSqlClientDelegate {

    private final JSqlClientImplementor delegate;

    public DynamicJSqlClientImpl(JSqlClientImplementor delegate) {
        this.delegate = delegate;
    }

    public static DynamicJSqlClientImpl from(JSqlClientImplementor.Builder builder) {
        JSqlClientImplementor sqlClient = (JSqlClientImplementor) builder
            .setEntityManager(new NoDissociationEntityManager())
            .build();
        return new DynamicJSqlClientImpl(sqlClient);
    }

    @Override
    protected JSqlClientImplementor sqlClient() {
        return delegate;
    }

    @Override
    public <E> SimpleEntitySaveCommand<E> saveCommand(E entity) {
        return new ISimpleEntitySaveCommandImpl<>(this, null, entity);
    }

    @Override
    public IdGenerator getIdGenerator(Class<?> entityType) {
        if (entityType == Object.class) {
            return IdentityIdGenerator.INSTANCE;
        }
        return super.getIdGenerator(entityType);
    }
}
