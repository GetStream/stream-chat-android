package io.getstream.logging;

import static io.getstream.logging.StreamLogger.DEBUG;
import static io.getstream.logging.StreamLogger.ERROR;
import static io.getstream.logging.StreamLogger.INFO;
import static io.getstream.logging.StreamLogger.VERBOSE;
import static io.getstream.logging.StreamLogger.WARN;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class TaggedLoggerImpl implements TaggedLogger {

  private final String tag;
  private final StreamLogger delegate;

  public TaggedLoggerImpl(String tag, StreamLogger logger) {
    this.tag = tag;
    this.delegate = logger;
  }

  @Override
  public void e(@NotNull Throwable throwable, @NotNull String message, @Nullable Object... args) {
    delegate.log(ERROR, tag, throwable, message, args);
  }

  @Override
  public void e(@NotNull String message, @Nullable Object... args) {
    delegate.log(ERROR, tag, null, message, args);
  }

  @Override
  public void w(@NotNull String message, @Nullable Object... args) {
    delegate.log(WARN, tag, null, message, args);
  }

  @Override
  public void i(@NotNull String message, @Nullable Object... args) {
    delegate.log(INFO, tag, null, message, args);
  }

  @Override
  public void d(@NotNull String message, @Nullable Object... args) {
    delegate.log(DEBUG, tag, null, message, args);
  }

  @Override
  public void v(@NotNull String message, @Nullable Object... args) {
    delegate.log(VERBOSE, tag, null, message, args);
  }
}
