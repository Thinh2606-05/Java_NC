package tfus.fuzzy;

import java.util.*;

/**
 * Hàm thành viên mờ dạng tam giác theo đúng định nghĩa Hình 1a và Phụ lục A trong bài báo:
 *
 * Với r = q / M(i) (0 <= r <= 1):
 * - Low:
 *     r <= 1/5          -> 1.0
 *     1/5 < r < 3/5     -> (3/5 - r) / (2/5) = (3M - 5q) / (2M)
 *     r >= 3/5          -> 0.0
 *
 * - Middle:
 *     r <= 1/5          -> 0.0
 *     1/5 < r <= 1/2    -> (r - 1/5) / (3/10) = (10q - 2M) / (3M)
 *     1/2 < r < 4/5     -> (4/5 - r) / (3/10) = (8M - 10q) / (3M)
 *     r >= 4/5          -> 0.0
 *
 * - High:
 *     r <= 2/5          -> 0.0
 *     2/5 < r < 4/5     -> (r - 2/5) / (2/5) = (5q - 2M) / (2M)
 *     r >= 4/5          -> 1.0
 *
 * Sử dụng số học phân số chính xác (exact rational arithmetic) để tránh sai số dấu phẩy động.
 */
public final class TriangularMembership implements MembershipFunction {

    @Override
    public String typeName() { return "triangular"; }

    @Override
    public Map<String, Double> fuzzify(int quantity, int maxQty) {
        long q = quantity;
        long m = maxQty;

        Map<String, Double> result = new LinkedHashMap<>();

        // Low
        if (5 * q <= m) {
            result.put("L", 1.0);
        } else if (5 * q < 3 * m) {
            double lVal = (double) (3 * m - 5 * q) / (2 * m);
            if (lVal > 0) result.put("L", lVal);
        }

        // Middle
        if (10 * q > 2 * m && 10 * q <= 5 * m) {
            double mVal = (double) (10 * q - 2 * m) / (3 * m);
            if (mVal > 0) result.put("M", mVal);
        } else if (10 * q > 5 * m && 10 * q < 8 * m) {
            double mVal = (double) (8 * m - 10 * q) / (3 * m);
            if (mVal > 0) result.put("M", mVal);
        }

        // High
        if (5 * q >= 4 * m) {
            result.put("H", 1.0);
        } else if (5 * q > 2 * m) {
            double hVal = (double) (5 * q - 2 * m) / (2 * m);
            if (hVal > 0) result.put("H", hVal);
        }

        return result;
    }
}
