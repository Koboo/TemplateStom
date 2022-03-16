package eu.koboo.minestom.api.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all"})
public class ProviderConfig {

  public static <T> Value<T> value(String key, T defaultValue, String comment) {
    return new Value<>(key, defaultValue, comment);
  }

  public static <T> Value<T> value(String key, T defaultValue) {
    return new Value<>(key, defaultValue, null);
  }

  public static ProviderConfig of(String file) {
    return new ProviderConfig(file);
  }

  private static final String SEPARATOR = ": ";

  private final String filePath;

  protected ProviderConfig(String filePath) {
    this.filePath = filePath;
    File file = new File(filePath);
    if (!file.exists()) {
      try {
        File parents = file.getParentFile();
        if (parents != null && !parents.exists()) {
          parents.mkdirs();
        }
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static List<String> readBuffer(String filePath) {
    List<String> content = new ArrayList<>();
    try {
      File file = new File(filePath);
      if (file.exists()) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
          content.add(line);
        }
        reader.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return content;
  }

  public <T> ProviderConfig set(String key, T value) {
    return set(key, value, null);
  }

  public <T> ProviderConfig set(String key, T value, String comment) {
    try {
      String valueSet = value.toString();
      if (value instanceof byte[]) {
        valueSet = new String((byte[]) value);
      }
      String keyValueString = key + SEPARATOR + valueSet;
      List<String> contentList = new ArrayList<>();
      String lastLine = null;
      for (String currentLine : readBuffer(filePath)) {
        if (currentLine.startsWith(key + SEPARATOR) && !currentLine.equals(keyValueString)) {
          currentLine = keyValueString;
          if (lastLine != null && lastLine.startsWith("#") && comment != null
              && !lastLine.equals(comment)) {
            contentList.remove(lastLine);
          }
          if (comment != null) {
            contentList.add("#" + comment);
          }
        }
        contentList.add(currentLine);
        lastLine = currentLine;
      }
      if (!contentList.contains(keyValueString)) {
        if (comment != null) {
          contentList.add("#" + comment);
        }
        contentList.add(keyValueString);
      }
      PrintWriter writer = new PrintWriter(filePath, "UTF-8");
      contentList.forEach(writer::println);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return this;
  }

  private void remove(String key) {
    try {
      key = key + SEPARATOR;
      List<String> contentList = new ArrayList<>();
      String lastContent = null;
      for (String contentString : readBuffer(filePath)) {
        if (!contentString.startsWith(key)) {
          contentList.add(contentString);
        }
      }
      PrintWriter writer = new PrintWriter(filePath, "UTF-8");
      contentList.forEach(writer::println);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private <T> T get(String key) {
    key = key + SEPARATOR;
    for (String contentLines : readBuffer(filePath)) {
      if (contentLines.startsWith(key)) {
        String value = contentLines.replaceFirst(key, "");
        return parseType(value);
      }
    }
    return null;
  }

  private <T> T parseType(String value) {
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
      return (T) Boolean.valueOf(value);
    }
    if (value.matches("-?\\d+(\\.\\d+)?")) {
      if (value.contains(".")) {
        double valueDouble = Double.valueOf(value);
        return (T) Double.valueOf(value);
      } else {
        long valueLong = Long.valueOf(value);
        if (valueLong <= Integer.MAX_VALUE && valueLong >= Integer.MIN_VALUE) {
          return (T) Integer.valueOf(value);
        }
        return (T) Long.valueOf(value);
      }
    }
    return (T) value;
  }

  public int getLineCount() {
    return readBuffer(filePath).size();
  }

  public String getFileName() {
    return getFile().getName();
  }

  public File getFile() {
    return new File(filePath);
  }

  public void delete() {
    deleteInternal(new File(filePath));
  }

  private boolean deleteInternal(File toDeleteFile) {
    File[] allContents = toDeleteFile.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        if (!file.isHidden() && !Files.isSymbolicLink(Paths.get(file.toURI()))) {
          deleteInternal(file);
        }
      }
    }
    return toDeleteFile.delete();
  }

  public <T> boolean contains(Value<T> value) {
    String finalKey = value.getKey() + SEPARATOR.replaceFirst(" ", "");
    return readBuffer(filePath).parallelStream().anyMatch(line -> line.startsWith(finalKey));
  }

  public <T> ProviderConfig apply(Value<T> value) {
    if (!contains(value)) {
      set(value.getKey(), value.getDefaultValue(), value.getComment());
    }
    return this;
  }

  public <T> T getOf(Value<T> value) {
    try {
      return get(value.getKey());
    } catch (Exception e) {
      throw new IllegalArgumentException("Key \"" + value.getKey() + "\" nested error: " + e.getMessage());
    }
  }

  public <T> T getOr(Value<T> value) {
    try {
      return getOf(value);
    } catch (Exception e) {
    }
    return value.getDefaultValue();
  }

  public <T> ProviderConfig invalidate(Value<T> value) {
    remove(value.getKey());
    return this;
  }


  public static class Value<T> {

    private final String key;
    private final T defaultValue;
    private final String comment;

    public Value(String key, T defaultValue, String comment) {
      this.key = key;
      this.defaultValue = defaultValue;
      this.comment = comment;
    }

    public String getKey() {
      return key;
    }

    public T getDefaultValue() {
      return defaultValue;
    }

    public String getComment() {
      return comment;
    }

  }

}
