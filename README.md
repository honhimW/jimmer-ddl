# Jimmer ddl generator

[![Maven Central](https://img.shields.io/maven-central/v/io.github.honhimw/jimmer-ddl.svg)](https://central.sonatype.com/artifact/io.github.honhimw/jimmer-ddl)


> [!note]  
> unofficial maintain implementation

## Usage

```groovy
// Gradle
implementation 'io.github.honhimw:jimmer-ddl:0.0.5'
```

### Quick start

```java
JSqlClientImplementor client;
SchemaCreator schemaCreator = new SchemaCreator(sqlClient);
schemaCreator.init();

List<ImmutableType> types = new ArrayList<>(); // tables to be generated
// types.add(...);

// create sql statements
types = DDLUtils.sortByDependent(sqlClient.getMetadataStrategy(), types);
List<String> sqlCreateStrings = schemaCreator.getSqlCreateStrings(types);

// drop sql statements
Collections.reverse(types);
List<String> sqlDropStrings = schemaCreator.getSqlDropStrings(types);
```

### Auto Runner
```java
DDLAutoRunner autoRunner = new DDLAutoRunner(client, DDLAuto.CREATE_DROP, types)；
autoRunner.init();

// on application start-up
autoRunner.create();

// on application destroy
autoRunner.drop();
```

### Example with annotations

```java
@Entity
@TableDef(
    comment = "powerlifting player",
    indexes = {
        @Index(columns = "sbd.squat"),
        @Index(columns = "sbd.benchPress"),
        @Index(columns = "sbd.deadLift"),
    },
//    uniques = @Unique(columns = ""),
    checks = @Check(constraint = "AGE > 16")
)
public interface Player {
    @Id
    @ColumnDef(length = 36, comment = "id")
    String id();

    @Nullable
    SBD sbd();

    @Nullable
    @ColumnDef(
        jdbcType = Types.SMALLINT,
        comment = "age"
    )
    Integer age();
}
```

### Construct ImmutableType At Runtime

```java
ManualTypeBuilder builder = ManualTypeBuilder.u64("id");
ImmutableType build = builder
    .name("TEST_TABLE2")
    .addIndex(Kind.PATH, "name")
    .addUnique(Kind.PATH, "name")
    .addCheck("#name <> ''")
    .addColumn(column -> column
        .name("name")
        .returnClass(String.class)
        .nullable(false)
        .length(1024)
        .defaultValue("'foo'")
        .comment("comment on column")
    )
    .addColumn("uuidValue", UUID.class)
    .comment("comment on table")
    .build();
// Generate Via DDLAutoRunner ...
```
#### Generated statements
```sql
create table TEST_TABLE2 (
     ID bigint not null auto_increment,
     NAME varchar(1024) default 'foo' not null,
     UUID_VALUE uuid not null,
     primary key (ID),
     constraint UK_TEST_TABLE2_NAME unique (NAME),
     check (NAME <> '')
);
comment on table TEST_TABLE2 is 'comment on table';
comment on column TEST_TABLE2.NAME is 'comment on column';
create index IDX_TEST_TABLE2_NAME on TEST_TABLE2 (NAME);
drop table if exists TEST_TABLE2 cascade;
```
