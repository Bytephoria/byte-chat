package team.bytephoria.bytechat.loader;

import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import team.bytephoria.bytechat.chat.component.ChatComponent;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.configuration.FormatConfiguration;
import team.bytephoria.bytechat.registry.ChatFormatRegistry;
import team.bytephoria.bytechat.util.Loader;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads every chat format defined under {@code plugins/ByteChat/formats/}.
 * <p>
 * Each {@code *.yml} file in that directory describes exactly one format whose id
 * is the file name without its extension. On first run, if the directory does not
 * exist, the bundled {@code formats/default.yml} resource is copied so the server
 * starts with a working default format.
 */
public final class FormatDirectoryLoader implements Loader {

    private static final String FORMATS_DIRECTORY = "formats";
    private static final String DEFAULT_FORMAT_RESOURCE = "formats/default.yml";
    private static final String YML_EXTENSION = ".yml";

    private final JavaPlugin plugin;
    private final ChatFormatRegistry registry;

    public FormatDirectoryLoader(
            final @NotNull JavaPlugin plugin,
            final @NotNull ChatFormatRegistry registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public void load() {
        final File directory = new File(this.plugin.getDataFolder(), FORMATS_DIRECTORY);
        if (!directory.exists()) {
            this.plugin.saveResource(DEFAULT_FORMAT_RESOURCE, false);
        }

        final File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(YML_EXTENSION));
        if (files == null) {
            return;
        }

        for (final File file : files) {
            this.loadFormat(file);
        }
    }

    private void loadFormat(final @NotNull File file) {
        final String fileName = file.getName();
        final String id = fileName.substring(0, fileName.length() - YML_EXTENSION.length());

        final FormatConfiguration configuration = this.read(file);
        if (configuration == null) {
            return;
        }

        final Map<String, ChatComponent> components = new LinkedHashMap<>(configuration.components().size());
        configuration.components().forEach((componentId, componentConfig) ->
                components.put(componentId, this.createComponent(componentConfig)));

        final ChatFormat chatFormat = new ChatFormat(
                id,
                configuration.permission(),
                configuration.priority(),
                components
        );

        this.registry.register(id, chatFormat);
    }

    private @Nullable FormatConfiguration read(final @NotNull File file) {
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .file(file)
                .build();

        try {
            return loader.load().get(FormatConfiguration.class);
        } catch (final ConfigurateException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load chat format file: " + file.getName(), exception);
            return null;
        }
    }

    private @NotNull ChatComponent createComponent(final @NotNull FormatConfiguration.ChatComponent componentConfig) {
        final FormatConfiguration.ClickAction clickConfig = componentConfig.click();
        final ClickEvent.Action action = this.parseClickAction(clickConfig.action());

        return new ChatComponent(componentConfig.text(), componentConfig.hover(), action, clickConfig.value());
    }

    private @Nullable ClickEvent.Action parseClickAction(final @NotNull String actionName) {
        if (actionName.isBlank()) {
            return null;
        }

        return ClickEvent.Action.NAMES.value(
                actionName
                        .trim()
                        .replace('-', '_')
                        .toLowerCase(Locale.ROOT)
        );
    }

    @Override
    public void unload() {
        this.registry.clearAll();
    }
}
