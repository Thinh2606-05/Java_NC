package tfus.fuzzy;

/**
 * Factory for creating MembershipFunction instances by type name.
 */
public final class MembershipFunctionFactory {

    private MembershipFunctionFactory() {}

    public static MembershipFunction create(String typeName) {
        if (typeName == null) return new TriangularMembership();
        return switch (typeName.toLowerCase()) {
            case "triangular", "triangle" -> new TriangularMembership();
            case "trapezoidal", "trapezoid" -> new TrapezoidalMembership();
            default -> throw new IllegalArgumentException("Unknown membership function: " + typeName);
        };
    }

    public static MembershipFunction defaultFunction() {
        return new TriangularMembership();
    }
}
