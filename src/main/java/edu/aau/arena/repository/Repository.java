package edu.aau.arena.repository;

import static edu.aau.ccms.Model.*;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;

/** Single-process local repository with filtered reads and atomic replacement. */
public final class Repository implements AutoCloseable {
  private final Path file;
  private final FileChannel channel;
  private final FileLock lock;

  public Repository(Path file) throws IOException {
    this.file = file.toAbsolutePath();
    Files.createDirectories(this.file.getParent());
    channel =
        FileChannel.open(
            Path.of(this.file + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    try {
      lock = channel.tryLock();
      if (lock == null)
          throw new IOException("This data folder is already open in another Arena application.");
    } catch (IOException | OverlappingFileLockException e) {
      channel.close();
      throw new IOException("This data folder is already open or cannot be locked.", e);
    }
  }

  public State load() throws IOException {
    if (!Files.exists(file)) return null;
    if (Files.size(file) > 20_000_000)
      throw new IOException("Saved data exceeds the supported 20 MB limit.");
    try {
      State state = decode(Files.readAllBytes(file));
      if (state.schema() != 1 && state.schema() != 2) throw new IOException("Unsupported data version.");
      state.upgradeSchema(); // In-memory migration; the original file changes only after a successful save.
      return state;
    } catch (ClassNotFoundException | RuntimeException e) {
      throw new IOException(
          "Cannot read saved data. Keep the file for recovery; it has not been reset.", e);
    }
  }

  public static byte[] encode(State state) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(state);
    }
    return bytes.toByteArray();
  }

  public static State decode(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      in.setObjectInputFilter(
          ObjectInputFilter.Config.createFilter(
              "maxdepth=40;maxrefs=200000;maxarray=200000;maxbytes=20000000;edu.aau.ccms.Model$*;java.base/*;!*"));
      return (State) in.readObject();
    }
  }

  public void save(State state) throws IOException {
    if (!channel.isOpen() || !lock.isValid()) throw new IOException("Repository is closed.");
    byte[] bytes = encode(state);
    if (bytes.length > 20_000_000)
      throw new IOException("Saved data exceeds the supported 20 MB limit.");
    // Never commit a snapshot which the configured input filter cannot read back.
    try {
      decode(bytes);
    } catch (ClassNotFoundException e) {
      throw new IOException("Snapshot cannot be reloaded.", e);
    }
    Path temporary = Path.of(file + ".tmp");
    boolean created = false;
    try {
      try (FileChannel output =
          FileChannel.open(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
        created = true;
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) output.write(buffer);
        output.force(true);
      }
      try {
        Files.move(
            temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException failure) {
      if (created)
        try {
          Files.deleteIfExists(temporary);
        } catch (IOException cleanup) {
          failure.addSuppressed(cleanup);
        }
      throw failure;
    }
  }

  @Override
  public void close() throws IOException {
    if (channel.isOpen())
      try {
        if (lock.isValid()) lock.release();
      } finally {
        channel.close();
      }
  }
}
