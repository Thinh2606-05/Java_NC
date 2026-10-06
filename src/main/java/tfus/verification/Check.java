package tfus.verification;

/**
 * A single verification check result.
 */
public record Check(
    CheckGroup group,
    String name,
    String expected,
    String actual,
    Status status,
    String note
) {
    public enum Status { PASS, ROUNDING, MISMATCH, MISSING, INFO }

    public boolean isPassing() {
        return status == Status.PASS || status == Status.ROUNDING || status == Status.INFO;
    }

    public static Check pass(CheckGroup g, String name, String value) {
        return new Check(g, name, value, value, Status.PASS, "");
    }

    public static Check mismatch(CheckGroup g, String name, String expected, String actual) {
        return new Check(g, name, expected, actual, Status.MISMATCH, "");
    }

    public static Check rounding(CheckGroup g, String name, String expected, String actual) {
        return new Check(g, name, expected, actual, Status.ROUNDING, "within rounding tolerance");
    }

    public static Check missing(CheckGroup g, String name, String expected) {
        return new Check(g, name, expected, "(not found)", Status.MISSING, "");
    }

    public static Check info(CheckGroup g, String name, String value) {
        return new Check(g, name, value, value, Status.INFO, "");
    }
}
