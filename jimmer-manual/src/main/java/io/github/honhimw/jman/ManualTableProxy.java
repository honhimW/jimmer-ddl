package io.github.honhimw.jman;

import org.babyfish.jimmer.View;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.JoinType;
import org.babyfish.jimmer.sql.ast.NumericExpression;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.Selection;
import org.babyfish.jimmer.sql.ast.impl.base.BaseTableOwner;
import org.babyfish.jimmer.sql.ast.impl.table.RootTableResolver;
import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor;
import org.babyfish.jimmer.sql.ast.impl.table.WeakJoinHandle;
import org.babyfish.jimmer.sql.ast.query.Example;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class ManualTableProxy implements TableProxy<Object> {

    public Table<?> __parent;

    @Override
    public Table<?> __parent() {
        return __parent;
    }

    public ImmutableProp __prop;

    @Override
    public ImmutableProp __prop() {
        return __prop;
    }

    public WeakJoinHandle __weakJoinHandle;

    @Override
    public WeakJoinHandle __weakJoinHandle() {
        return __weakJoinHandle;
    }

    public boolean __isInverse;

    @Override
    public boolean __isInverse() {
        return __isInverse;
    }

    public TableImplementor<Object> __unwrap;

    @Override
    public TableImplementor<Object> __unwrap() {
        return __unwrap;
    }

    public TableImplementor<Object> __resolve;

    @Override
    public TableImplementor<Object> __resolve(RootTableResolver resolver) {
        return __resolve;
    }

    public TableProxy<Object> __disableJoin;

    @SuppressWarnings("unchecked")
    @Override
    public <P extends TableProxy<Object>> P __disableJoin(String reason) {
        return (P) __resolve;
    }

    public TableProxy<Object> __baseTableOwner;

    @Override
    public TableProxy<Object> __baseTableOwner(BaseTableOwner baseTableOwner) {
        return __baseTableOwner;
    }

    public BaseTableOwner baseTableOwner;

    @Override
    public @Nullable BaseTableOwner __baseTableOwner() {
        return baseTableOwner;
    }

    public JoinType __joinType;

    @Override
    public JoinType __joinType() {
        return __joinType;
    }

    @Override
    public Predicate eq(Table<Object> other) {
        return null;
    }

    @Override
    public Predicate eq(Example<Object> example) {
        return null;
    }

    @Override
    public Predicate eq(Object example) {
        return null;
    }

    @Override
    public Predicate eq(View<Object> view) {
        return null;
    }

    @Override
    public Predicate isNull() {
        return null;
    }

    @Override
    public Predicate isNotNull() {
        return null;
    }

    @Override
    public NumericExpression<Long> count() {
        return null;
    }

    @Override
    public NumericExpression<Long> count(boolean distinct) {
        return null;
    }

    @Override
    public Selection<Object> fetch(Fetcher<Object> fetcher) {
        return null;
    }

    @Override
    public <V extends View<Object>> Selection<V> fetch(Class<V> viewType) {
        return null;
    }

    @Override
    public TableEx<Object> asTableEx() {
        return null;
    }

    @Override
    public ImmutableType getImmutableType() {
        return null;
    }

    @Override
    public <X> PropExpression<X> get(ImmutableProp prop) {
        return null;
    }

    @Override
    public <X> PropExpression<X> get(String prop) {
        return null;
    }

    @Override
    public <X> PropExpression<X> getId() {
        return null;
    }

    @Override
    public <X> PropExpression<X> getAssociatedId(ImmutableProp prop) {
        return null;
    }

    @Override
    public <X> PropExpression<X> getAssociatedId(String prop) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(String prop) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop, JoinType joinType) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(String prop, JoinType joinType) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop, JoinType joinType, ImmutableType treatedAs) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT join(String prop, JoinType joinType, ImmutableType treatedAs) {
        return null;
    }

    @Override
    public <X> PropExpression<X> inverseGetAssociatedId(ImmutableProp prop) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(ImmutableProp prop) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(ImmutableProp prop, JoinType joinType) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(TypedProp.Association<?, ?> prop) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(TypedProp.Association<?, ?> prop, JoinType joinType) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(Class<XT> targetTableType, Function<XT, ? extends Table<?>> backPropBlock) {
        return null;
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(Class<XT> targetTableType, Function<XT, ? extends Table<?>> backPropBlock, JoinType joinType) {
        return null;
    }

    @Override
    public <XT extends Table<?>> Predicate exists(String prop, Function<XT, Predicate> block) {
        return null;
    }

    @Override
    public <XT extends Table<?>> Predicate exists(ImmutableProp prop, Function<XT, Predicate> block) {
        return null;
    }
}
