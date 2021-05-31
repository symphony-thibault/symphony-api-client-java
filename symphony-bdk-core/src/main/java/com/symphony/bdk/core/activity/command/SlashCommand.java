package com.symphony.bdk.core.activity.command;

import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.gen.api.model.V4Initiator;
import com.symphony.bdk.gen.api.model.V4MessageSent;

import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * A "slash" command if the most basic action that can be performed by an end-user through the chat.
 */
@API(status = API.Status.EXPERIMENTAL)
public class SlashCommand extends PatternCommandActivity<CommandContext> {

  private final String slashCommandName;
  private final boolean requiresBotMention;
  private final Consumer<CommandContext> callback;
  private final String description;

  /**
   * Returns a new {@link SlashCommand} instance.
   *
   * @param slashCommandName Identifier of the command (ex: '/gif' or 'gif').
   * @param callback         Callback to be processed when command is detected.
   */
  public static SlashCommand slash(@Nonnull String slashCommandName, @Nonnull Consumer<CommandContext> callback) {
    return slash(slashCommandName, true, callback);
  }

  /**
   * Returns a new {@link SlashCommand} instance.
   *
   * @param slashCommandName   Identifier of the command (ex: '/gif' or 'gif').
   * @param requiresBotMention Indicates whether the bot has to be mentioned in order to trigger the command.
   * @param callback           Callback to be processed when command is detected.
   * @throws IllegalArgumentException if command name if empty.
   */
  public static SlashCommand slash(@Nonnull String slashCommandName, boolean requiresBotMention,
      @Nonnull Consumer<CommandContext> callback) {
    return new SlashCommand(slashCommandName, requiresBotMention, callback, "");
  }

  /**
   * Returns a new {@link SlashCommand} instance.
   *
   * @param slashCommandName Identifier of the command (ex: '/gif' or 'gif').
   * @param callback         Callback to be processed when command is detected.
   * @param description      The summary of the command.
   * @return a {@link SlashCommand} instance.
   */
  public static SlashCommand slash(@Nonnull String slashCommandName, @Nonnull Consumer<CommandContext> callback,
      String description) {
    return slash(slashCommandName, true, callback, description);
  }

  /**
   * Returns a new {@link SlashCommand} instance.
   *
   * @param slashCommandName   Identifier of the command (ex: '/gif' or 'gif').
   * @param requiresBotMention Indicates whether the bot has to be mentioned in order to trigger the command.
   * @param callback           Callback to be processed when command is detected.
   * @param description        The summary of the command.
   * @return a {@link SlashCommand} instance.
   */
  public static SlashCommand slash(@Nonnull String slashCommandName, boolean requiresBotMention,
      @Nonnull Consumer<CommandContext> callback, String description) {
    return new SlashCommand(slashCommandName, requiresBotMention, callback, description);
  }

  /**
   * Default private constructor, new instances from static methods only.
   */
  private SlashCommand(@Nonnull String slashCommandName, boolean requiresBotMention,
      @Nonnull Consumer<CommandContext> callback, String description) {

    if (StringUtils.isEmpty(slashCommandName)) {
      throw new IllegalArgumentException("The slash command name cannot be empty.");
    }

    this.slashCommandName = slashCommandName;
    this.requiresBotMention = requiresBotMention;
    this.callback = callback;
    this.description = description;
  }

  @Override
  public Pattern pattern() {
    final String botMention = this.requiresBotMention ? "@" + this.getBotDisplayName() + " " : "";
    return Pattern.compile("^" + botMention + this.slashCommandName + "$");
  }

  @Override
  public void onActivity(CommandContext context) {
    this.callback.accept(context);
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo()
        .type(ActivityType.COMMAND)
        .name(this.slashCommandName)
        .description(this.buildCommandDescription());
  }

  @Override
  protected CommandContext createContextInstance(V4Initiator initiator, V4MessageSent event) {
    return new CommandContext(initiator, event);
  }

  private String buildCommandDescription() {
    return this.requiresBotMention ? this.description + " (mention required)"
        : this.description + " (mention not required)";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    SlashCommand that = (SlashCommand) o;
    return requiresBotMention == that.requiresBotMention && slashCommandName.equals(that.slashCommandName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(slashCommandName, requiresBotMention);
  }
}
