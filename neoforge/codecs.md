Title: Codecs | NeoForged docs

URL Source: https://docs.neoforged.net/docs/1.21.1/datastorage/codecs

Markdown Content:
Codecs are a serialization tool from Mojang's [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) used to describe how objects can be transformed between different formats, such as `JsonElement`s for JSON and `Tag`s for NBT.

## Using Codecs[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#using-codecs "Direct link to Using Codecs")

Codecs are primarily used to encode, or serialize, Java objects to some data format type and decode, or deserialize, formatted data objects back to its associated Java type. This is typically accomplished using `Codec#encodeStart` and `Codec#parse`, respectively.

### DynamicOps[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#dynamicops "Direct link to DynamicOps")

To determine what intermediate file format to encode and decode to, both `#encodeStart` and `#parse` require a `DynamicOps` instance to define the data within that format.

The [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) library contains `JsonOps` to codec JSON data stored in [`Gson`'s](https://github.com/google/gson)`JsonElement` instances. `JsonOps` supports two versions of `JsonElement` serialization: `JsonOps#INSTANCE` which defines a standard JSON file, and `JsonOps#COMPRESSED` which allows data to be compressed into a single string.

`// Let exampleCodec represent a Codec<ExampleJavaObject>// Let exampleObject be a ExampleJavaObject// Let exampleJson be a JsonElement// Encode Java object to regular JsonElementexampleCodec.encodeStart(JsonOps.INSTANCE, exampleObject);// Encode Java object to compressed JsonElementexampleCodec.encodeStart(JsonOps.COMPRESSED, exampleObject);// Decode JsonElement into Java object// Assume JsonElement was parsed normallyexampleCodec.parse(JsonOps.INSTANCE, exampleJson);`

Minecraft also provides `NbtOps` to codec NBT data stored in `Tag` instances. This can be referenced using `NbtOps#INSTANCE`.

`// Let exampleCodec represent a Codec<ExampleJavaObject>// Let exampleObject be a ExampleJavaObject// Let exampleNbt be a Tag// Encode Java object to TagexampleCodec.encodeStart(NbtOps.INSTANCE, exampleObject);// Decode Tag into Java objectexampleCodec.parse(NbtOps.INSTANCE, exampleNbt);`

To handle registry entries, Minecraft provides `RegistryOps`, which contains a lookup provider to get available registry elements. These can be created by `RegistryOps#create` that takes in the `DynamicOps` with the specific type to store the data within and the lookup provider containing access to the available registries. NeoForge extends `RegistryOps` to create `ConditionalOps`: a registry codec lookup that can handle [conditions to load the entry](https://docs.neoforged.net/docs/1.21.1/resources/server/conditions).

`// Let lookupProvider be a HolderLookup.Provider// Let exampleCodec represent a Codec<ExampleJavaObject>// Let exampleObject be a ExampleJavaObject// Let exampleJson be a JsonElement// Get the registry ops for JsonElementRegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, lookupProvider);// Encode Java object to JsonElementexampleCodec.encodeStart(ops, exampleObject);// Decode JsonElement into Java objectexampleCodec.parse(ops, exampleJson);`

#### Format Conversion[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#format-conversion "Direct link to Format Conversion")

`DynamicOps` can also be used separately to convert between two different encoded formats. This can be done using `#convertTo` and supplying the `DynamicOps` format and the encoded object to convert.

`// Convert Tag to JsonElement// Let exampleTag be a TagJsonElement convertedJson = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, exampleTag);`

### DataResult[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#dataresult "Direct link to DataResult")

Encoded or decoded data using codecs return a `DataResult` which holds the converted instance or some error data depending on whether the conversion was successful. When the conversion is successful, the `Optional` supplied by `#result` will contain the successfully converted object. If the conversion fails, the `Optional` supplied by `#error` will contain the `PartialResult`, which holds the error message and a partially converted object depending on the codec.

Additionally, there are many methods on `DataResult` that can be used to transform the result or error into the desired format. For example, `#resultOrPartial` will return an `Optional` containing the result on success, and the partially converted object on failure. The method takes in a string consumer to determine how to report the error message if present.

`// Let exampleCodec represent a Codec<ExampleJavaObject>// Let exampleJson be a JsonElement// Decode JsonElement into Java objectDataResult<ExampleJavaObject> result = exampleCodec.parse(JsonOps.INSTANCE, exampleJson);result    // Get result or partial on error, report error message    .resultOrPartial(errorMessage -> /* Do something with error message */)    // If result or partial is present, do something    .ifPresent(decodedObject -> /* Do something with decoded object */);`

## Existing Codecs[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#existing-codecs "Direct link to Existing Codecs")

### Primitives[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#primitives "Direct link to Primitives")

The `Codec` class contains static instances of codecs for certain defined primitives.

| Codec | Java Type |
| --- | --- |
| `BOOL` | `Boolean` |
| `BYTE` | `Byte` |
| `SHORT` | `Short` |
| `INT` | `Integer` |
| `LONG` | `Long` |
| `FLOAT` | `Float` |
| `DOUBLE` | `Double` |
| `STRING` | `String`* |
| `BYTE_BUFFER` | `ByteBuffer` |
| `INT_STREAM` | `IntStream` |
| `LONG_STREAM` | `LongStream` |
| `PASSTHROUGH` | `Dynamic<?>`** |
| `EMPTY` | `Unit`*** |

* `String` can be limited to a certain number of characters via `Codec#string` or `Codec#sizeLimitedString`.

** `Dynamic` is an object which holds a value encoded in a supported `DynamicOps` format. These are typically used to convert encoded object formats into other encoded object formats.

*** `Unit` is an object used to represent `null` objects.

### Vanilla and NeoForge[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#vanilla-and-neoforge "Direct link to Vanilla and NeoForge")

Minecraft and NeoForge define many codecs for objects that are frequently encoded and decoded. Some examples include `ResourceLocation#CODEC` for `ResourceLocation`s, `ExtraCodecs#INSTANT_ISO8601` for `Instant`s in the `DateTimeFormatter#ISO_INSTANT` format, and `CompoundTag#CODEC` for `CompoundTag`s.

caution

`CompoundTag`s cannot decode lists of numbers from JSON using `JsonOps`. `JsonOps`, when converting, sets a number to its most narrow type. `ListTag`s force a specific type for its data, so numbers with different types (e.g. `64` would be `byte`, `384` would be `short`) will throw an error on conversion.

Vanilla and NeoForge registries also have codecs for the type of object the registry contains (e.g. `BuiltInRegistries#BLOCK` have a `Codec<Block>`). `Registry#byNameCodec` will encode the registry object to their registry name. Vanilla registries also have a `Registry#holderByNameCodec` which encodes to a registry name and decodes to the registry object wrapped in a `Holder`.

## Creating Codecs[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#creating-codecs "Direct link to Creating Codecs")

Codecs can be created for encoding and decoding any object. For understanding purposes, the equivalent encoded JSON will be shown.

### Records[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#records "Direct link to Records")

Codecs can define objects through the use of records. Each record codec defines any object with explicit named fields. There are many ways to create a record codec, but the simplest is via `RecordCodecBuilder#create`.

`RecordCodecBuilder#create` takes in a function which defines an `Instance` and returns an application (`App`) of the object. A correlation can be drawn to creating a class _instance_ and the constructors used to _apply_ the class to the constructed object.

`// Some object to create a codec forpublic class SomeObject {    public SomeObject(String s, int i, boolean b) { /* ... */ }    public String s() { /* ... */ }    public int i() { /* ... */ }    public boolean b() { /* ... */ }}`

#### Fields[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#fields "Direct link to Fields")

An `Instance` can define up to 16 fields using `#group`. Each field must be an application defining the instance the object is being made for and the type of the object. The simplest way to meet this requirement is by taking a `Codec`, setting the name of the field to decode from, and setting the getter used to encode the field.

A field can be created from a `Codec` using `#fieldOf`, if the field is required, or `#optionalFieldOf`, if the field is wrapped in an `Optional` or defaulted. Either method requires a string containing the name of the field in the encoded object. The getter used to encode the field can then be set using `#forGetter`, taking in a function which given the object, returns the field data.

warning

`#optionalFieldOf` will throw an error if there is an element that throws an error when parsing. If the error should be consumed, use `#lenientOptionalFieldOf` instead.

From there, the resulting product can be applied via `#apply` to define how the instance should construct the object for the application. For ease of convenience, the grouped fields should be listed in the same order they appear in the constructor such that the function can simply be a constructor method reference.

`public static final Codec<SomeObject> RECORD_CODEC = RecordCodecBuilder.create(instance -> // Given an instance    instance.group( // Define the fields within the instance        Codec.STRING.fieldOf("s").forGetter(SomeObject::s), // String        Codec.INT.optionalFieldOf("i", 0).forGetter(SomeObject::i), // Integer, defaults to 0 if field not present        Codec.BOOL.fieldOf("b").forGetter(SomeObject::b) // Boolean    ).apply(instance, SomeObject::new) // Define how to create the object);`

`// Encoded SomeObject{    "s": "value",    "i": 5,    "b": false}// Another encoded SomeObject{    "s": "value2",    // i is omitted, defaults to 0    "b": true}// Another encoded SomeObject{    "s": "value2",    // Will throw an error as lenientOptionalFieldOf is not used    "i": "bad_value",    "b": true}`

### Transformers[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#transformers "Direct link to Transformers")

Codecs can be transformed into equivalent, or partially equivalent, representations through mapping methods. Each mapping method takes in two functions: one to transform the current type into the new type, and one to transform the new type back to the current type. This is done through the `#xmap` function.

`// A classpublic class ClassA {    public ClassB toB() { /* ... */ }}// Another equivalent classpublic class ClassB {    public ClassA toA() { /* ... */ }}// Assume there is some codec A_CODECpublic static final Codec<ClassB> B_CODEC = A_CODEC.xmap(ClassA::toB, ClassB::toA);`

If a type is partially equivalent, meaning that there are some restrictions during conversion, there are mapping functions which return a `DataResult` which can be used to return an error state whenever an exception or invalid state is reached.

| Is A Fully Equivalent to B | Is B Fully Equivalent to A | Transform Method |
| --- | --- | --- |
| Yes | Yes | `#xmap` |
| Yes | No | `#flatComapMap` |
| No | Yes | `#comapFlatMap` |
| No | No | `#flatXMap` |

`// Given an string codec to convert to a integer// Not all strings can become integers (A is not fully equivalent to B)// All integers can become strings (B is fully equivalent to A)public static final Codec<Integer> INT_CODEC = Codec.STRING.comapFlatMap(    s -> { // Return data result containing error on failure        try {            return DataResult.success(Integer.valueOf(s));        } catch (NumberFormatException e) {            return DataResult.error(s + " is not an integer.");        }    },    Integer::toString // Regular function);`

`// Will return 5"5"// Will error, not an integer"value"`

#### Range Codecs[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#range-codecs "Direct link to Range Codecs")

Range codecs are an implementation of `#flatXMap` which returns an error `DataResult` if the value is not inclusively between the set minimum and maximum. The value is still provided as a partial result if outside the bounds. There are implementations for integers, floats, and doubles via `#intRange`, `#floatRange`, and `#doubleRange` respectively.

`public static final Codec<Integer> RANGE_CODEC = Codec.intRange(0, 4);`

`// Will be valid, inside [0, 4]4// Will error, outside [0, 4]5`

#### String Resolver[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#string-resolver "Direct link to String Resolver")

`Codec#stringResolver` is an implementation of `flatXmap` which maps a string to some kind of object.

`public record StringResolverObject(String name) { /* ... */ }// Assume there is some Map<String, StringResolverObject> OBJECT_MAPpublic static final Codec<StringResolverObject> STRING_RESOLVER_CODEC = Codec.stringResolver(StringResolverObject::name, OBJECT_MAP::get);`

`// Will map this string to its associated object"example_name"`

### Defaults[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#defaults "Direct link to Defaults")

If the result of encoding or decoding fails, a default value can be supplied instead via `Codec#orElse` or `Codec#orElseGet`.

`public static final Codec<Integer> DEFAULT_CODEC = Codec.INT.orElse(    errorMessage -> /* Do something with the error message */,    0 // Can also be a supplied value via #orElseGet);`

`// Not an integer, defaults to 0"value"`

### Unit[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#unit "Direct link to Unit")

A codec which supplies an in-code value and encodes to nothing can be represented using `Codec#unit`. This is useful if a codec uses a non-encodable entry within the data object.

`public static final Codec<IEventBus> UNIT_CODEC = Codec.unit(    () -> NeoForge.EVENT_BUS // Can also be a raw value);`

`// Nothing here, will return the NeoForge event bus`

### Lazy Initialized[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#lazy-initialized "Direct link to Lazy Initialized")

Sometimes, a codec may rely on data that is not present when it is constructed. In these situations `Codec#lazyInitialized` can be used to for a codec to construct itself on first encoding/decoding. The method takes in a supplied codec.

`public static final Codec<IEventBus> LAZY_CODEC = Codec.lazyInitialized(    () -> Codec.Unit(NeoForge.EVENT_BUS));`

`// Nothing here, will return the NeoForge event bus// Encodes/decodes the same way as the normal codec`

### List[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#list "Direct link to List")

A codec for a list of objects can be generated from an object codec via `Codec#listOf`. `listOf` can also take in integers representing the minimum and maximum size of the list. `sizeLimitedListOf` does the same but only specifies a maximum bound.

`// BlockPos#CODEC is a Codec<BlockPos>public static final Codec<List<BlockPos>> LIST_CODEC = BlockPos.CODEC.listOf();`

`// Encoded List<BlockPos>[    [1, 2, 3], // BlockPos(1, 2, 3)    [4, 5, 6], // BlockPos(4, 5, 6)    [7, 8, 9]  // BlockPos(7, 8, 9)]`

List objects decoded using a list codec are stored in an **immutable** list. If a mutable list is needed, a [transformer](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#transformer-codecs) should be applied to the list codec.

### Map[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#map "Direct link to Map")

A codec for a map of keys and value objects can be generated from two codecs via `Codec#unboundedMap`. Unbounded maps can specify any string-based or string-transformed value to be a key.

`// BlockPos#CODEC is a Codec<BlockPos>public static final Codec<Map<String, BlockPos>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, BlockPos.CODEC);`

`// Encoded Map<String, BlockPos>{    "key1": [1, 2, 3], // key1 -> BlockPos(1, 2, 3)    "key2": [4, 5, 6], // key2 -> BlockPos(4, 5, 6)    "key3": [7, 8, 9]  // key3 -> BlockPos(7, 8, 9)}`

Map objects decoded using a unbounded map codec are stored in an **immutable** map. If a mutable map is needed, a [transformer](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#transformer-codecs) should be applied to the map codec.

caution

Unbounded maps only support keys that encode/decode to/from strings. A key-value [pair](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#pair) list codec can be used to get around this restriction.

### Pair[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#pair "Direct link to Pair")

A codec for pairs of objects can be generated from two codecs via `Codec#pair`.

A pair codec decodes objects by first decoding the left object in the pair, then taking the remaining part of the encoded object and decodes the right object from that. As such, the codecs must either express something about the encoded object after decoding (such as [records](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#records)), or they have to be augmented into a `MapCodec` and transformed into a regular codec via `#codec`. This can typically done by making the codec a [field](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#fields) of some object.

`public static final Codec<Pair<Integer, String>> PAIR_CODEC = Codec.pair(    Codec.INT.fieldOf("left").codec(),    Codec.STRING.fieldOf("right").codec());`

`// Encoded Pair<Integer, String>{    "left": 5,       // fieldOf looks up 'left' key for left object    "right": "value" // fieldOf looks up 'right' key for right object}`

tip

A map codec with a non-string key can be encoded/decoded using a list of key-value pairs applied with a [transformer](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#transformer-codecs).

### Either[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#either "Direct link to Either")

A codec for two different methods of encoding/decoding some object data can be generated from two codecs via `Codec#either`.

An either codec attempts to decode the object using the first codec. If it fails, it attempts to decode using the second codec. If that also fails, then the `DataResult` will only contain the error from the second codec failure.

`public static final Codec<Either<Integer, String>> EITHER_CODEC = Codec.either(    Codec.INT,    Codec.STRING);`

`// Encoded Either.Left<Integer, String>5// Encoded Either.Right<Integer, String>"value"`

tip

This can be used in conjunction with a [transformer](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#transformer-codecs) to get a specific object from two different methods of encoding.

#### Xor[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#xor "Direct link to Xor")

`Codec#xor` is a special case of the [either](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#either) codec where a result is only successful if one of the two methods are processed successfully. If both codecs can be processed, then an error is thrown instead.

`public static final Codec<Either<Integer, String>> XOR_CODEC = Codec.xor(    Codec.INT.fieldOf("number").codec(),    Codec.STRING.fieldOf("text").codec());`

`// Encoded Either.Left<Integer, String>{    "number": 4}// Encoded Either.Right<Integer, String>{    "text": "value"}// Throws an error as both can be decoded{    "number": 4,    "text": "value"}`

#### Alternative[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#alternative "Direct link to Alternative")

`Codec#withAlternative` is a special case of the [either](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#either) codec where both codecs are trying to decode the same object, but stored in a different format. The first, or primary, codec will attempt to decode the object. On failure, the second codec will be used instead. Encoding will always use the primary codec.

`public static final Codec<BlockPos> ALTERNATIVE_CODEC = Codec.withAlternative(    BlockPos.CODEC,    RecordCodecBuilder.create(instance -> instance.group(        Codec.INT.fieldOf("x").forGetter(BlockPos::getX),        Codec.INT.fieldOf("y").forGetter(BlockPos::getY),        Codec.INT.fieldOf("z").forGetter(BlockPos::getZ)    ), BlockPos::new));`

`// Normal method to decode BlockPos[ 1, 2, 3 ]// Alternative method to decode BlockPos{    "x": 1,    "y": 2,    "z": 3}`

### Recursive[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#recursive "Direct link to Recursive")

Sometimes, an object may reference an object of the same type as a field. For example, `EntityPredicate` takes in an `EntityPredicate` for the vehicle, passenger, and targeted entity. In this case, `Codec#recursive` can be used to supply the codec as part of a function to create the codec.

`// Define our recursive objectpublic record RecursiveObject(Optional<RecursiveObject> inner) { /* ... */ }public static final Codec<RecursiveObject> RECURSIVE_CODEC = Codec.recursive(    RecursiveObject.class.getSimpleName(), // This is for the toString method    recursedCodec -> RecordCodecBuilder.create(instance -> instance.group(        recursedCodec.optionalFieldOf("inner").forGetter(RecursiveObject::inner)    ).apply(instance, RecursiveObject::new)));`

`// An encoded recursive object{    "inner": {        "inner": {}    }}`

### Dispatch[​](https://docs.neoforged.net/docs/1.21.1/datastorage/codecs#dispatch "Direct link to Dispatch")

Codecs can have subcodecs which can decode a particular object based upon some specified type via `Codec#dispatch`. This is typically used in registries which contain codecs, such as rule tests or block placers.

A dispatch codec first attempts to get the encoded type from some string key (usually `type`). From there, the type is decoded, calling a getter for the specific codec used to decode the actual object. If the `DynamicOps` used to decode the object compresses its maps, or the object codec itself is not augmented into a `MapCodec` (such as records or fielded primitives), then the object needs to be stored within a `value` key. Otherwise, the object is decoded at the same level as the rest of the data.

`// Define our objectpublic abstract class ExampleObject {    // Define the method used to specify the object type for encoding    public abstract MapCodec<? extends ExampleObject> type();}// Create simple object which stores a stringpublic class StringObject extends ExampleObject {    public StringObject(String s) { /* ... */ }    public String s() { /* ... */ }    public MapCodec<? extends ExampleObject> type() {        // A registered registry object        // "string":        //   Codec.STRING.xmap(StringObject::new, StringObject::s).fieldOf("string")        return STRING_OBJECT_CODEC.get();    }}// Create complex object which stores a string and integerpublic class ComplexObject extends ExampleObject {    public ComplexObject(String s, int i) { /* ... */ }    public String s() { /* ... */ }    public int i() { /* ... */ }    public MapCodec<? extends ExampleObject> type() {        // A registered registry object        // "complex":        //   RecordCodecBuilder.mapCodec(instance ->        //     instance.group(        //       Codec.STRING.fieldOf("s").forGetter(ComplexObject::s),        //       Codec.INT.fieldOf("i").forGetter(ComplexObject::i)        //     ).apply(instance, ComplexObject::new)        //   )        return COMPLEX_OBJECT_CODEC.get();    }}// Assume there is an Registry<MapCodec<? extends ExampleObject>> DISPATCHpublic static final Codec<ExampleObject> = DISPATCH.byNameCodec() // Gets Codec<MapCodec<? extends ExampleObject>>    .dispatch(        ExampleObject::type, // Get the codec from the specific object        Function.identity() // Get the codec from the registry    );`

`// Simple object{    "type": "string", // For StringObject    "value": "value" // Codec type is not augmented from MapCodec, needs field}// Complex object{    "type": "complex", // For ComplexObject    // Codec type is augmented from MapCodec, can be inlined    "s": "value",    "i": 0}`