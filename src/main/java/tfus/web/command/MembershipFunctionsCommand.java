package tfus.web.command;

import tfus.web.ApiCommand;
import tfus.web.ApiRequest;
import tfus.web.ApiResponse;

import java.util.List;
import java.util.Map;

/**
 * Xử lý GET /api/membership-functions
 * Trả thông tin các hàm thành viên hỗ trợ để UI hiển thị biểu đồ/thông tin.
 */
public final class MembershipFunctionsCommand implements ApiCommand {

    @Override
    public ApiResponse execute(ApiRequest request) {
        List<Map<String, Object>> list = List.of(
            Map.of(
                "type", "triangular",
                "name", "Triangular Membership Function",
                "terms", List.of("L", "M", "H"),
                "formula", "L: max(0, 1-2r); M: 2r (r<=0.5) or 2(1-r); H: max(0, 2r-1)"
            ),
            Map.of(
                "type", "trapezoidal",
                "name", "Trapezoidal Membership Function",
                "terms", List.of("L", "M", "H"),
                "formula", "Piecewise linear with 4 knots per term"
            )
        );
        return ApiResponse.ok(list);
    }
}
