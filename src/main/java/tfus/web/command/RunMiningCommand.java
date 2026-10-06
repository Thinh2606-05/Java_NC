package tfus.web.command;

import tfus.domain.MiningInput;
import tfus.io.JsonDatasetReader;
import tfus.io.json.Json;
import tfus.service.MiningFacade;
import tfus.web.ApiCommand;
import tfus.web.ApiRequest;
import tfus.web.ApiResponse;
import tfus.web.dto.JsonMapper;

import java.util.Map;

/**
 * Xử lý POST /api/mine
 * Nhận body JSON chứa config và dataset, chạy thuật toán và trả kết quả.
 */
public final class RunMiningCommand implements ApiCommand {

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse execute(ApiRequest request) throws Exception {
        if (request.body() == null || request.body().isBlank()) {
            return ApiResponse.badRequest("Request body is empty");
        }

        Object parsed = Json.parse(request.body());
        if (!(parsed instanceof Map<?, ?> root)) {
            return ApiResponse.badRequest("Invalid JSON object");
        }

        MiningInput input = JsonDatasetReader.parse((Map<String, Object>) root);
        MiningFacade.RunResult runResult = MiningFacade.runWithTrace(input);

        Map<String, Object> respMap = JsonMapper.runResultToJson(input, runResult);
        return ApiResponse.ok(respMap);
    }
}
