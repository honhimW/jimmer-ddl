# Jimmer ddl generator

> [!note]  
> unofficial maintain implementation

### Usage

```groovy
// Gradle
implementation 'io.github.honhimw:jimmer-ddl:0.0.1'
```

#### Quick start

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

#### Auto Runner
```java
DDLAutoRunner autoRunner = new DDLAutoRunner(client, DDLAuto.CREATE_DROP, types)；
autoRunner.init();

// on application start-up
autoRunner.create();

// on application destroy
autoRunner.drop();
```
