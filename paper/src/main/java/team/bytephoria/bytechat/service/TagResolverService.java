package team.bytephoria.bytechat.service;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.tag.Tag;
import team.bytephoria.bytechat.api.tag.TagContext;
import team.bytephoria.bytechat.api.tag.TagRegistry;
import team.bytephoria.bytechat.configuration.ChatConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles the resolution of inline chat tags by converting them into
 * unique placeholder markers during parsing. These markers are later
 * replaced by the renderer with their corresponding chat components.
 * <p>
 * The set of recognised tags is supplied by a {@link TagRegistry}, so both
 * built-in tags and tags registered by third-party plugins are resolved through
 * the same path.
 */
public final class TagResolverService {

    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';

    public static final String PLACEHOLDER_PREFIX = "<<<BYTECHAT_TAG_";
    public static final String PLACEHOLDER_SUFFIX = ">>>";

    /**
     * First code point of the private-use sentinel range used by the inline render
     * path. Each inline tag occurrence is replaced by a single sentinel character
     * ({@code INLINE_SENTINEL_BASE + tagIndex}) that the renderer later splices the
     * real component into. Private-use characters never appear in normal chat input.
     */
    public static final char INLINE_SENTINEL_BASE = '\uE000';

    /**
     * Pre-compiled pattern for splitting strings on ByteChat placeholder boundaries.
     * Matches immediately before a prefix and immediately after a suffix, allowing
     * callers to isolate placeholder markers from surrounding text in a single pass.
     */
    public static final Pattern PLACEHOLDER_SPLIT_PATTERN = Pattern.compile(
            "(?=" + Pattern.quote(PLACEHOLDER_PREFIX) + ")"
                    + "|(?<=" + Pattern.quote(PLACEHOLDER_SUFFIX) + ")"
    );

    private final ChatConfiguration configuration;
    private final TagRegistry tagRegistry;

    public TagResolverService(
            final @NotNull ChatConfiguration configuration,
            final @NotNull TagRegistry tagRegistry
    ) {
        this.configuration = configuration;
        this.tagRegistry = tagRegistry;
    }

    /**
     * Resolves all supported tags within the message.
     * <p>
     * The parser works in a single pass:
     * <ul>
     *     <li>When a {@code [key]} token is detected, the key is looked up in the
     *         {@link TagRegistry}.</li>
     *     <li>If a matching tag exists and the player has its permission, a
     *         placeholder is inserted and a {@link TagComponent} is generated for
     *         later injection.</li>
     *     <li>Unknown tokens (or tags the player cannot use) are left as literal
     *         text.</li>
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
            // Stop parsing if tag limit reached (negative limit means unlimited).
            if (maxTags >= 0 && tagCounter >= maxTags) {
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
                    final Component tagComponent = this.resolveTag(player, tagContent);

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
     * Resolves tags for the single-pass (inline) render path.
     * <p>
     * Each tag occurrence is replaced by a single private-use sentinel character
     * rather than a placeholder string. The renderer deserializes the message with
     * the sentinels stripped — so surrounding gradients flow continuously, as if the
     * tags were not there — and then splices each rendered component back at the
     * sentinel's position. This keeps the tag's own color intact and prevents it
     * from consuming any of the gradient's progression.
     *
     * @param player  the player who sent the message
     * @param message the raw message string
     * @return the message with sentinels in place, and the components in order
     */
    public @NotNull InlineSplit splitInlineTags(
            final @NotNull Player player,
            final @NotNull String message
    ) {
        final int maxTags = this.configuration.chat().tags().maxTagsPerMessage();
        final List<Component> components = new ArrayList<>();
        final StringBuilder processedMessage = new StringBuilder();

        int tagCounter = 0;
        int index = 0;
        final int length = message.length();

        while (index < length) {
            if (maxTags >= 0 && tagCounter >= maxTags) {
                processedMessage.append(message.substring(index));
                break;
            }

            final char c = message.charAt(index);

            if (c == OPEN_BRACKET) {
                final int tagEnd = message.indexOf(CLOSE_BRACKET, index);

                if (tagEnd != -1) {
                    final String tagContent = message.substring(index + 1, tagEnd);
                    final Component tagComponent = this.resolveTag(player, tagContent);

                    if (tagComponent != null) {
                        processedMessage.append((char) (INLINE_SENTINEL_BASE + tagCounter));
                        components.add(tagComponent);

                        tagCounter = tagCounter + 1;
                        index = tagEnd + 1;
                        continue;
                    }
                }
            }

            processedMessage.append(c);
            index = index + 1;
        }

        return new InlineSplit(processedMessage.toString(), components);
    }

    /**
     * Looks up the registered tag for the given key and renders it for the player.
     *
     * @param player  the player whose state and permissions are used
     * @param tagType the tag identifier such as "item", "inv", or "armor"
     * @return the rendered component, or {@code null} if no usable tag matches
     */
    private @Nullable Component resolveTag(final @NotNull Player player, final @NotNull String tagType) {
        final Tag tag = this.tagRegistry.tag(tagType.toLowerCase());
        if (tag == null) {
            return null;
        }

        final String permission = tag.permission();
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            return null;
        }

        return tag.render(new TagContext(player, tagType));
    }

    public record TagResolutionResult(
            @NotNull String processedMessage,
            @NotNull List<TagComponent> tagComponents
    ) {}

    public record TagComponent(
            @NotNull String placeholder,
            @NotNull Component component
    ) {}

    /**
     * Result of {@link #splitInlineTags(Player, String)}: the message with each tag
     * occurrence replaced by a sentinel character, and the resolved components in
     * occurrence order (component {@code i} corresponds to sentinel
     * {@code INLINE_SENTINEL_BASE + i}).
     */
    public record InlineSplit(
            @NotNull String processedMessage,
            @NotNull List<Component> components
    ) {}
}
