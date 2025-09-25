# Jimmer ddl generator

[![Maven Central](https://img.shields.io/maven-central/v/io.github.honhimw/jimmer-ddl.svg)](https://central.sonatype.com/artifact/io.github.honhimw/jimmer-ddl)


> [!note]  
> unofficial maintain implementation

## Usage

```groovy
// Gradle
implementation 'io.github.honhimw:jimmer-ddl:0.0.2'
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
