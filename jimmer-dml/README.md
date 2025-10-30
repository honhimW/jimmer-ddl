# Jimmer DML Generator

> [!warning]  
> unstable, limited, not-tested-yet

> Using Jimmer for DML generation and manager data without Type-System and Compile-Time magic.

### Usage
```java
JSqlClient.Builder builder = JSqlClient.newBuilder();
JSqlClientImplementor sqlClient = DynamicJSqlClientImpl.from((JSqlClientImplementor.Builder) builder);

ManualImmutableTypeImpl testType = (ManualImmutableTypeImpl) ManualTypeBuilder
    .u32("id")
    .tableName("test")
    .addColumn(column -> column.name("name").type(String.class))
    .build();

// INSERT
ManualImmutableSpi entity = new ManualImmutableSpi(testType);
entity.set("id", 1);
entity.set("name", "bar");
SimpleSaveResult<ManualImmutableSpi> insertResult = sqlClient.saveCommand(entity)
    .setMode(SaveMode.INSERT_ONLY)
    .execute();
Assertions.assertEquals(1, insertResult.getModifiedEntity().__get("id"));

// UPDATE
Integer updateResult = sqlClient.createUpdate(tableProxy)
    .set(tableProxy.get("name"), "foo")
    .where(tableProxy.get("id").eq(1))
    .execute();
Assertions.assertEquals(1, updateResult);

// SELECT
MutableRootQuery<TableProxy<Object>> query = sqlClient.createQuery(tableProxy);
MutableRootQuery<TableProxy<Object>> id = query.where(tableProxy.get("id").eq(1));
Object o = id.select(tableProxy).fetchFirstOrNull();
Assertions.assertEquals(1, ImmutableObjects.get(o, "id"));
Assertions.assertEquals("foo", ImmutableObjects.get(o, "name"));

// DELETE
Integer deleteResult = sqlClient.createDelete(tableProxy)
    .where(tableProxy.get("id").eq(1))
    .where(tableProxy.get("name").eq("foo"))
    .execute();
Assertions.assertEquals(1, deleteResult);

```

