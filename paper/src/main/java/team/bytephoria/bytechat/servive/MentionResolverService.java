package team.bytephoria.bytechat.servive;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.Constants;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.util.StringUtil;

public final class MentionResolverService {

    private final ChatConfiguration configuration;
    public MentionResolverService(final @NotNull ChatConfiguration configuration) {
        this.configuration = configuration;
    }

    public @NotNull String resolveMentions(final @NotNull Player player, final @NotNull String message) {
        final ChatConfiguration.Mentions mentions = this.configuration.chat().mentions();
        final String[] parts = StringUtil.split(message, ' ');
        if (parts == null) {
            return message;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            final String word = parts[i];
            if (word.charAt(0) != mentions.triggerChar()) {
                stringBuilder.append(word).append(' ');
                continue;
            }

            final int wordLength = word.length();
            final int minLength = Constants.MINECRAFT_MIN_USERNAME_LENGTH + 1;
            final int maxLength = Constants.MINECRAFT_MAX_USERNAME_LENGTH + 1;
            if (wordLength < minLength || wordLength > maxLength) {
                this.appendWord(stringBuilder, word, i, parts.length);
                continue;
            }

            final String mentionedPlayerName = word.substring(1);
            final Player mentionedPlayer = Bukkit.getPlayerExact(mentionedPlayerName);
            if (mentionedPlayer == null) {
                this.appendWord(stringBuilder, word, i, parts.length);
                continue;
            }

            if (!mentions.allowSelfMention() && player == mentionedPlayer) {
                this.appendWord(stringBuilder, word, i, parts.length);
                continue;
            }

            final String formattedMention = StringUtil.replace(
                    mentions.format(),
                    "{input_name}", mentionedPlayerName,
                    "{player_name}", mentionedPlayer.getName()
            );

            this.appendWord(stringBuilder, formattedMention, i, parts.length);
            if (!mentions.selfMentionSound() && mentionedPlayer == player) {
                continue;
            }

            final ChatConfiguration.Sound sound = mentions.sound();
            if (!sound.key().isBlank()) {
                mentionedPlayer.playSound(
                        Sound.sound(Key.key(sound.key()),
                                Sound.Source.MASTER,
                                sound.volume(),
                                sound.pitch()
                        )
                );
            }
        }

        return stringBuilder.toString();
    }

    private void appendWord(
            final @NotNull StringBuilder stringBuilder,
            final @NotNull String word,
            final int index,
            final int total
    ) {
        stringBuilder.append(word);
        if (index < total - 1) {
            stringBuilder.append(' ');
        }
    }

}
