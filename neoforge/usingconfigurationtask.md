Title: Using Configuration Tasks | NeoForged docs

URL Source: https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks

Markdown Content:
The networking protocol for the client and server has a specific phase where the server can configure the client before the player actually joins the game. This phase is called the configuration phase, and is for example used by the vanilla server to send the resource pack information to the client.

This phase can also be used by mods to configure the client before the player joins the game.

## Registering a configuration task[​](https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks#registering-a-configuration-task "Direct link to Registering a configuration task")

The first step to using the configuration phase is to register a configuration task. This can be done by registering a new configuration task in the `RegisterConfigurationTasksEvent` event.

`@SubscribeEvent // on the mod event buspublic static void register(final RegisterConfigurationTasksEvent event) {    event.register(new MyConfigurationTask());}`

The `RegisterConfigurationTasksEvent` event is fired on the mod bus, and exposes the current listener used by the server to configure the relevant client. A modder can use the exposed listener to figure out if the client is running the mod, and if so, register a configuration task.

## Implementing a configuration task[​](https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks#implementing-a-configuration-task "Direct link to Implementing a configuration task")

A configuration task is a simple interface: `ICustomConfigurationTask`. This interface has two methods: `void run(Consumer<CustomPacketPayload> sender);`, and `ConfigurationTask.Type type();` which returns the type of the configuration task. The type is used to identify the configuration task. An example of a configuration task is shown below:

`public record MyConfigurationTask implements ICustomConfigurationTask {    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath("mymod", "my_task"));        @Override    public void run(final Consumer<CustomPacketPayload> sender) {        final MyData payload = new MyData();        sender.accept(payload);    }    @Override    public ConfigurationTask.Type type() {        return TYPE;    }}`

## Acknowledging a configuration task[​](https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks#acknowledging-a-configuration-task "Direct link to Acknowledging a configuration task")

Your configuration is executed on the server, and the server needs to know when the next configuration task can be executed. This is done by acknowledging the execution of said configuration task.

There are two primary ways of achieving this:

### Capturing the listener[​](https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks#capturing-the-listener "Direct link to Capturing the listener")

When the client does not need to acknowledge the configuration task, then the listener can be captured, and the configuration task can be acknowledged directly on the server side.

`public record MyConfigurationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath("mymod", "my_task"));        @Override    public void run(final Consumer<CustomPacketPayload> sender) {        final MyData payload = new MyData();        sender.accept(payload);        this.listener().finishCurrentTask(this.type());    }    @Override    public ConfigurationTask.Type type() {        return TYPE;    }}`

To use such a configuration task, the listener needs to be captured in the `RegisterConfigurationTasksEvent` event.

`@SubscribeEvent // on the mod event buspublic static void register(final RegisterConfigurationTasksEvent event) {    event.register(new MyConfigurationTask(event.getListener()));}`

Then the next configuration task will be executed immediately after the current configuration task has completed, and the client does not need to acknowledge the configuration task. Additionally, the server will not wait for the client to properly process the send payloads.

### Acknowledging the configuration task[​](https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks#acknowledging-the-configuration-task "Direct link to Acknowledging the configuration task")

When the client needs to acknowledge the configuration task, then you will need to send your own payload to the client:

`public record AckPayload() implements CustomPacketPayload {    public static final CustomPacketPayload.Type<AckPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mymod", "ack"));        // Unit codec with no data to write    public static final StreamCodec<ByteBuf, AckPayload> STREAM_CODEC = StreamCodec.unit(new AckPayload());    @Override    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {        return TYPE;    }}`

When a payload from a server side configuration task is properly processed you can send this payload to the server to acknowledge the configuration task.

`public void onMyData(MyData data, IPayloadContext context) {    context.enqueueWork(() -> {        blah(data.name());    })    .exceptionally(e -> {        // Handle exception        context.disconnect(Component.translatable("my_mod.configuration.failed", e.getMessage()));        return null;    })    .thenAccept(v -> {        context.reply(new AckPayload());    });     }`

Where `onMyData` is the handler for the payload that was sent by the server side configuration task.

When the server receives this payload it will acknowledge the configuration task, and the next configuration task will be executed:

`public void onAck(AckPayload payload, IPayloadContext context) {    context.finishCurrentTask(MyConfigurationTask.TYPE);}`

Where `onAck` is the handler for the payload that was sent by the client.

## Stalling the login process[​](https://docs.neoforged.net/docs/1.21.1/networking/configuration-tasks#stalling-the-login-process "Direct link to Stalling the login process")

When the configuration is not acknowledged, then the server will wait forever, and the client will never join the game. So it is important to always acknowledge the configuration task, unless the configuration task failed, then you can disconnect the client.