# Jimmer DML Generator

> [!warning]  
> unstable, limited, not-tested-yet

> Using Jimmer for DML generation and manager data without Type-System and Compile-Time magic.

### Usage

#### 1. Prepare

```java
void prepareSchemas() {
    JSqlClient.Builder builder = JSqlClient.newBuilder();
    applyBuilder(builder);
    JSqlClientImplementor sqlClient = DynJSqlClientImpl.from((JSqlClientImplementor.Builder) builder);
    ImmutableType referred = ManualTypeBuilder
        .of(column -> column.name("id").type(UUID.class))
        .tableName("REFERRED_TABLE")
        .addColumn(column -> column.name("name").type(String.class))
        .build();
    ImmutableType main = ManualTypeBuilder.of("MAIN_TABLE")
        .addColumn(column -> column.name("id").type(Integer.TYPE).primaryKey().autoIncrement())
        .addColumn(column -> column.name("name").type(String.class))
        .addRelation(fk -> fk.type(referred).propName("ref")
            .self(column -> column.nullable(true))
        ).build();
    try (DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, Arrays.asList(referred, main))) {
        ddlAutoRunner.init();
        ddlAutoRunner.create();
        // Construct a TableProxy instance via DynTableProxy
        DynTableProxy tableProxy = new DynTableProxy(main);
        // TODO
    }
}
```

**Generated statements**

```sql
create table REFERRED_TABLE (
    ID uuid not null,
    NAME varchar(255) not null,
    primary key (ID)
);
create table MAIN_TABLE (
    ID integer not null auto_increment,
    NAME varchar(255) not null,
    REF_ID uuid,
    primary key (ID)
);
alter table if exists MAIN_TABLE add constraint FK_MAIN_TABLE_REF_ID foreign key (REF_ID) references REFERRED_TABLE (ID);
```

#### 2. Insert

```java
void insert() {
    ManualDraftSpi draft = new ManualDraftSpi(main);
    draft
        .set("id", 1)
        .set("name", "bar");
    ManualImmutableSpi entity = draft.__resolve();
    SimpleSaveResult<ManualImmutableSpi> insertResult = sqlClient.saveCommand(entity)
        .setMode(SaveMode.INSERT_ONLY)
        .execute();
//    Purpose: MUTATE
//    SQL: insert into MAIN_TABLE(ID, NAME) values(? /* 1 */, ? /* bar */)
    Assertions.assertEquals(1, insertResult.getModifiedEntity().__get("id"));
}
```

#### 3. Update

```java
void update() {
    Integer updateResult = sqlClient.createUpdate(tableProxy)
        .set(tableProxy.get("name"), "foo")
        .where(tableProxy.get("id").eq(1))
        .execute();
//    Purpose: UPDATE
//    SQL: update MAIN_TABLE tb_1_
//    set
//        NAME = ? /* foo */
//    where
//        tb_1_.ID = ? /* 1 */
    Assertions.assertEquals(1, updateResult);
}
```

#### 4. select

```java
void select() {
    MutableRootQuery<DynTableProxy> query = sqlClient.createQuery(tableProxy)
        .where(tableProxy.get("id").eq(1));
//    Purpose: QUERY
//    SQL: select
//        tb_1_.ID,
//        tb_1_.NAME,
//        tb_1_.REF_ID
//    from MAIN_TABLE tb_1_
//    where
//        tb_1_.ID = ? /* 1 */
//    limit ? /* 1 */
    Object o = query.select(tableProxy).fetchFirst();
    Assertions.assertEquals(1, ImmutableObjects.get(o, "id"));
    Assertions.assertEquals("foo", ImmutableObjects.get(o, "name"));
}

void selectWithJoin() {
    Table<?> join = tableProxy.join("ref");
    Object o1 = sqlClient.createQuery(tableProxy)
        .where(join.get("id").eq(UUID.randomUUID()))
        .where(join.get("name").eq("???"))
        .select(tableProxy).fetchFirstOrNull();
//    Purpose: QUERY
//    SQL: select
//    tb_1_.ID,
//        tb_1_.NAME,
//        tb_1_.REF_ID
//    from MAIN_TABLE tb_1_
//    inner join REFERRED_TABLE tb_2_
//        on tb_1_.REF_ID = tb_2_.ID
//    where
//            tb_1_.REF_ID = ? /* d0ee3887-2a3f-415b-91d5-340c112d37af */
//        and
//            tb_2_.NAME = ? /* ??? */
//    limit ? /* 1 */
}
```

#### 5. Delete

```java
void delete() {
    Integer deleteResult = sqlClient.createDelete(tableProxy)
        .where(tableProxy.get("id").eq(1))
        .where(tableProxy.get("name").eq("foo"))
        .execute();
//    Purpose: DELETE
//    SQL: delete
//    from MAIN_TABLE tb_1_
//    where
//            tb_1_.ID = ? /* 1 */
//        and
//            tb_1_.NAME = ? /* foo */
    Assertions.assertEquals(1, deleteResult);
}
```
