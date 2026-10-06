package tfus.fuzzy;

import java.util.*;

/**
 * Trapezoidal membership function (4-point shape per term).
 * Uses normalized ratio r = q / M(i).
 *
 * Each term defined by breakpoints [a, b, c, d]:
 *   f(r) = 0              if r < a or r > d
 *   f(r) = (r-a)/(b-a)   if a <= r < b
 *   f(r) = 1              if b <= r <= c
 *   f(r) = (d-r)/(d-c)   if c < r <= d
 *
 * Default 3-term (L/M/H) trapezoidal configuration:
 *   L: [0, 0, 1/3, 2/3]
 *   M: [1/3, 2/3, 2/3, 1]  (not typically used; triangular is standard)
 *   H: [1/3, 2/3, 1, 1]
 */
public final class TrapezoidalMembership implements MembershipFunction {

    /** A single trapezoidal term */
    public record Trapezoid(String term, double a, double b, double c, double d) {}

    private final List<Trapezoid> trapezoids;

    /** Default 3-term LMH trapezoidal */
    public TrapezoidalMembership() {
        this(List.of(
            new Trapezoid("L", 0.0, 0.0, 1.0/3, 2.0/3),
            new Trapezoid("M", 1.0/3, 2.0/3, 2.0/3, 1.0),
            new Trapezoid("H", 1.0/3, 2.0/3, 1.0, 1.0)
        ));
    }

    public TrapezoidalMembership(List<Trapezoid> trapezoids) {
        this.trapezoids = List.copyOf(trapezoids);
    }

    @Override
    public String typeName() { return "trapezoidal"; }

    @Override
    public Map<String, Double> fuzzify(int quantity, int maxQty) {
        double r = (double) quantity / maxQty;
        Map<String, Double> result = new LinkedHashMap<>();

        for (Trapezoid t : trapezoids) {
            double fv = evaluate(r, t);
            if (fv > 1e-15) {
                result.put(t.term(), fv);
            }
        }
        return result;
    }

    private double evaluate(double r, Trapezoid t) {
        if (r < t.a() || r > t.d()) return 0.0;
        if (r >= t.b() && r <= t.c()) return 1.0;
        if (r < t.b()) {
            double den = t.b() - t.a();
            return den < 1e-15 ? 1.0 : (r - t.a()) / den;
        }
        // r > t.c()
        double den = t.d() - t.c();
        return den < 1e-15 ? 1.0 : (t.d() - r) / den;
    }
}
