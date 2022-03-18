package eu.koboo.minestom.console;

public enum ConvertColor {

    RESET("\u001B[0m", "r"),
    BLACK("\u001B[30m", "0"),
    DARK_BLUE("\u001B[34m", "1"),
    DARK_GREEN("\u001B[32m", "2"),
    DARK_CYAN("\u001B[36m", "3"),
    DARK_RED("\u001B[31m", "4"),
    DARK_PURPLE("\u001B[35m", "5"),
    DARK_YELLOW("\u001B[33m", "6"),
    LIGHT_GRAY("\u001B[97m", "7"),
    DARK_GRAY("\u001B[90m", "8"),
    LIGHT_BLUE("\u001B[94m", "9"),
    LIGHT_GREEN("\u001B[92m", "a"),
    LIGHT_CYAN("\u001B[96m", "b"),
    LIGHT_RED("\u001B[91m", "c"),
    LIGHT_PURPLE("\u001B[95m", "d"),
    LIGHT_YELLOW("\u001B[93m", "e"),
    WHITE("\u001B[37m", "f");

    private final String ansiColor;
    private final String code;

    ConvertColor(String ansiColor, String code) {
        this.ansiColor = ansiColor;
        this.code = code;
    }

    public String getAnsiColor() {
        return ansiColor;
    }

    public String getWithAmp() {
        return "&" + code;
    }

    public String getWithParagraph() {
        return "§" + code;
    }

    private static String replaceColor(ConvertColor color, String message, String replacement) {
        message = message.contains(color.getWithAmp()) ? message.replaceAll(color.getWithAmp(), replacement) : message;
        return message.contains(color.getWithParagraph()) ? message.replaceAll(color.getWithParagraph(), replacement) : message;
    }

    public static String parseColor(String message) {
        for(ConvertColor convertColor : values()) {
            message = replaceColor(convertColor, message, convertColor.getAnsiColor());
        }
        return message;
    }

}
