package team.bytephoria.bytechat.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.ui.CompleteInventoryPreviewMenu;
import team.bytephoria.bytechat.ui.EnderChestPreviewMenu;
import team.bytephoria.bytechat.ui.EquipmentPreviewMenu;
import team.bytephoria.bytechat.util.StringUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the resolution of inline chat tags by converting them into
 * unique placeholder markers during parsing. These markers are later
 * replaced by the renderer with their corresponding chat components.
 */
public final class TagResolverService {

    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';

    private static final String PLACEHOLDER_PREFIX = "<<<BYTECHAT_TAG_";
    private static final String PLACEHOLDER_SUFFIX = ">>>";

    private final ChatConfiguration configuration;

    public TagResolverService(final @NotNull ChatConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Resolves all supported tags within the message.
     * <p>
     * The parser works in a single pass:
     * <ul>
     *     <li>When a tag is detected, it is validated.</li>
     *     <li>A placeholder is inserted into the output string.</li>
     *     <li>A {@link TagComponent} is generated for later injection.</li>
     *     <li>Processing stops once the per-message tag limit is reached.</li>
     * </ul>
     *
     * @param player the player who sent the message
     * @param message the raw message string
     * @return the processed message along with all resolved tag components
     */
    public @NotNull TagResolutionResult resolveTags(
            final @NotNull Player player,
            final @NotNull String message
    ) {
        final int maxTags = this.configuration.chat().tags().maxTagsPerMessage();
        final List<TagComponent> tagComponents = new ArrayList<>();
        final StringBuilder processedMessage = new StringBuilder();

        int tagCounter = 0;
        int index = 0;
        final int length = message.length();

        while (index < length) {
            // Stop parsing if tag limit reached.
            if (tagCounter >= maxTags) {
                processedMessage.append(message.substring(index));
                break;
            }

            final char c = message.charAt(index);

            // Attempt to parse tag.
            if (c == OPEN_BRACKET) {
                final int tagStart = index;
                final int tagEnd = message.indexOf(CLOSE_BRACKET, tagStart);

                if (tagEnd != -1) {
                    final String tagContent = message.substring(tagStart + 1, tagEnd);
                    final Component tagComponent = this.createTagComponent(player, tagContent);

                    if (tagComponent != null) {
                        // Create unique placeholder for replacement.
                        final String placeholder = PLACEHOLDER_PREFIX + tagCounter + PLACEHOLDER_SUFFIX;

                        processedMessage.append(placeholder);
                        tagComponents.add(new TagComponent(placeholder, tagComponent));

                        tagCounter = tagCounter + 1;
                        index = tagEnd + 1;
                        continue;
                    }
                }
            }

            processedMessage.append(c);
            index = index + 1;
        }

        return new TagResolutionResult(processedMessage.toString(), tagComponents);
    }

    /**
     * Determines which tag type to create based on the content inside brackets.
     *
     * @param player the player whose state is used for tag data
     * @param tagType the tag identifier such as "item", "inv", or "armor"
     * @return the component representing the tag or null if unsupported
     */
    private @Nullable Component createTagComponent(final @NotNull Player player, final @NotNull String tagType) {
        return switch (tagType.toLowerCase()) {
            case "item" -> this.createItemComponent(player);
            case "inv" -> this.createInventoryComponent(player);
            case "armor" -> this.createEquipmentComponent(player);
            case "ec", "ender", "enderchest" -> this.createEnderchestInventoryComponent(player);
            default -> null;
        };
    }

    /**
     * Creates a component representing the player's main-hand item.
     * Includes a hover event with full item tooltip.
     * Displays fallback text if the player is not holding an item.
     */
    private @Nullable Component createItemComponent(final @NotNull Player player) {
        final ChatConfiguration.Tags.ItemTag itemConfig = this.configuration.chat().tags().item();
        if (!itemConfig.enabled() && !player.hasPermission(FeaturePermission.Format.TAG_ITEM)) {
            return null;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            final NamedTextColor color = this.parseColor(itemConfig.emptyHandColor());
            return Component.text(itemConfig.emptyHandText(), color);
        }

        return item.displayName()
                .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value()));
    }

    /**
     * Creates a component for the player's equipment preview.
     * Clicking the component opens a GUI showing their armor.
     */
    private @Nullable Component createEquipmentComponent(final @NotNull Player player) {
        final ChatConfiguration.Tags.ArmorTag armorConfig = this.configuration.chat().tags().armor();
        if (!armorConfig.enabled() || !player.hasPermission(FeaturePermission.Format.TAG_ARMOR)) {
            return null;
        }

        final String armorTitle = StringUtil.replaceSingle(
                this.configuration.chat().tags().armor().previewTitle(),
                "{player_name}", player.getName()
        );

        final EquipmentPreviewMenu previewMenu = EquipmentPreviewMenu.create(player, armorTitle);
        final NamedTextColor color = this.parseColor(armorConfig.displayColor());

        return Component.text(armorConfig.displayText(), color)
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player clickedPlayer) {
                        clickedPlayer.openInventory(previewMenu.getInventory());
                    }
                }, ClickCallback.Options.builder()
                        .uses(armorConfig.maxClicks())
                        .lifetime(Duration.ofSeconds(armorConfig.expirationSeconds()))
                        .build()));
    }

    /**
     * Creates a component for the player's full inventory preview.
     * Clicking the component opens a GUI showing the full inventory contents.
     */
    private @Nullable Component createInventoryComponent(final @NotNull Player player) {
        final ChatConfiguration.Tags.InventoryTag invConfig = this.configuration.chat().tags().inventory();
        if (!invConfig.enabled() || !player.hasPermission(FeaturePermission.Format.TAG_INVENTORY)) {
            return null;
        }

        final String inventoryTitle = StringUtil.replaceSingle(
                this.configuration.chat().tags().inventory().previewTitle(),
                "{player_name}", player.getName()
        );

        final CompleteInventoryPreviewMenu previewMenu = CompleteInventoryPreviewMenu.create(player, inventoryTitle);
        final NamedTextColor color = this.parseColor(invConfig.displayColor());

        return Component.text(invConfig.displayText(), color)
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player clickedPlayer) {
                        clickedPlayer.openInventory(previewMenu.getInventory());
                    }
                }, ClickCallback.Options.builder()
                        .uses(invConfig.maxClicks())
                        .lifetime(Duration.ofSeconds(invConfig.expirationSeconds()))
                        .build()));
    }

    private @Nullable Component createEnderchestInventoryComponent(final @NotNull Player player) {
        final ChatConfiguration.EnderChestTag enderChestTag = this.configuration.chat().tags().enderChest();
        if (!enderChestTag.enabled() && !player.hasPermission(FeaturePermission.Format.TAG_ENDERCHEST)) {
            return null;
        }

        final String inventoryTitle = StringUtil.replaceSingle(
                enderChestTag.previewTitle(),
                "{player_name}", player.getName()
        );

        final EnderChestPreviewMenu enderChestPreviewMenu = EnderChestPreviewMenu.create(player, Component.text(inventoryTitle));

        return Component.text(enderChestTag.displayText(), this.parseColor(enderChestTag.displayColor()))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player clickedPlayer) {
                        clickedPlayer.openInventory(enderChestPreviewMenu.getInventory());
                    }
                }, ClickCallback.Options.builder()
                        .uses(enderChestTag.maxClicks())
                        .lifetime(Duration.ofSeconds(enderChestTag.expirationSeconds()))
                        .build()));

    }

    /**
     * Converts a color name into a {@link NamedTextColor}, defaulting to white
     * if no matching color name is found.
     */
    private @NotNull NamedTextColor parseColor(final @NotNull String colorName) {
        return NamedTextColor.NAMES.valueOr(colorName.toLowerCase(), NamedTextColor.WHITE);
    }

    public record TagResolutionResult(
            @NotNull String processedMessage,
            @NotNull List<TagComponent> tagComponents
    ) {}

    public record TagComponent(
            @NotNull String placeholder,
            @NotNull Component component
    ) {}
}
