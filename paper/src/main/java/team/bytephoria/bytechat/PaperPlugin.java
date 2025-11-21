package team.bytephoria.bytechat;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import team.bytephoria.bytechat.commands.ChatCommand;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.configuration.FormatConfiguration;
import team.bytephoria.bytechat.listener.AsyncChatListener;
import team.bytephoria.bytechat.loader.ChatFormatLoader;
import team.bytephoria.bytechat.manager.ChatManager;
import team.bytephoria.bytechat.registry.ChatFormatRegistry;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerFactory;
import team.bytephoria.bytechat.servive.MentionResolverService;

import java.io.File;

public final class PaperPlugin extends JavaPlugin {

    private ChatConfiguration chatConfiguration;
    private FormatConfiguration formatConfiguration;

    private ComponentSerializerAdapter componentSerializerAdapter;

    private ChatFormatRegistry chatFormatRegistry;
    private ChatManager chatManager;
    private MentionResolverService mentionResolverService;

    private Metrics metrics;

    @Override
    public void onEnable() {
        this.chatConfiguration = this.loadConfiguration("config", ChatConfiguration.class, true);
        this.formatConfiguration = this.loadConfiguration("formats", FormatConfiguration.class, true);

        final String serializerType = this.chatConfiguration.settings().serializer();
        this.componentSerializerAdapter = ComponentSerializerFactory.create(serializerType);
        this.chatFormatRegistry = new ChatFormatRegistry();
        this.chatManager = new ChatManager(this.chatFormatRegistry, this.chatConfiguration);
        this.mentionResolverService = new MentionResolverService(this.chatConfiguration);

        new ChatFormatLoader(this.chatFormatRegistry, this.formatConfiguration).load();

        if (this.chatConfiguration.chat().enabled()) {
            this.getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
        }

        this.getServer().getCommandMap().register("bytechat", new ChatCommand(this));

        this.metrics = new Metrics(this, 27686);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        this.getServer().getCommandMap().getKnownCommands().remove("bytechat");

        if (this.metrics != null) {
            this.metrics.shutdown();
        }

        if (this.chatFormatRegistry != null) {
            this.chatFormatRegistry.clearAll();
        }

        this.metrics = null;
        this.mentionResolverService = null;
        this.chatManager = null;
        this.chatFormatRegistry = null;
        this.componentSerializerAdapter = null;
        this.formatConfiguration = null;
        this.chatConfiguration = null;
    }

    // I hate writing reload functions... but anything for my users!
    public void reload() {
        this.onDisable();
        this.onEnable();
    }

    public MentionResolverService mentionResolverService() {
        return this.mentionResolverService;
    }

    public ChatConfiguration chatConfiguration() {
        return this.chatConfiguration;
    }

    public FormatConfiguration formatConfiguration() {
        return this.formatConfiguration;
    }

    public ComponentSerializerAdapter serializerAdapter() {
        return this.componentSerializerAdapter;
    }

    public ChatManager chatManager() {
        return this.chatManager;
    }

    private <T> @NotNull AbstractConfigurationLoader<@NotNull CommentedConfigurationNode> createConfiguration(final @NotNull String fileName, final @NotNull Class<T> tClass, final boolean copyFromResources) {
        final File file = this.resolveFile(fileName);
        if (copyFromResources && !file.exists()) {
            this.saveResource(file.getName(), false);
        }

        return YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .file(file)
                .build();
    }

    private <T> @Nullable T loadConfiguration(final @NotNull String fileName, final @NotNull Class<T> tClass, final boolean copyFromResources) {
        final @NotNull AbstractConfigurationLoader<@NotNull CommentedConfigurationNode> yamlConfigurationLoader =
                this.createConfiguration(fileName, tClass, copyFromResources);

        try {
            final CommentedConfigurationNode commentedConfigurationNode = yamlConfigurationLoader.load();
            final T configuration = commentedConfigurationNode.get(tClass);

            commentedConfigurationNode.set(tClass, configuration);
            //yamlConfigurationLoader.save(commentedConfigurationNode);

            return configuration;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull File resolveFile(final @NotNull String fileName) {
        return new File(this.getDataFolder(), fileName + ".yml");
    }

}
