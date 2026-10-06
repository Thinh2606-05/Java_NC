package tfus.web.command;

import tfus.domain.MiningInput;
import tfus.io.TextDatasetReader;
import tfus.service.MiningFacade;
import tfus.service.PresetCatalog;
import tfus.verification.VerificationReport;
import tfus.web.ApiCommand;
import tfus.web.ApiRequest;
import tfus.web.ApiResponse;
import tfus.web.dto.JsonMapper;

import java.nio.file.Path;
import java.util.Map;

/**
 * Xử lý GET /api/verify/{id}
 * Đối chiếu kết quả chạy thuật toán với file hand-run reference của preset tương ứng.
 */
public final class VerifyCommand implements ApiCommand {

    private final PresetCatalog catalog;
    private final Path dataDir;

    public VerifyCommand(PresetCatalog catalog, Path dataDir) {
        this.catalog = catalog;
        this.dataDir = dataDir;
    }

    @Override
    public ApiResponse execute(ApiRequest request) throws Exception {
        String id = request.pathParam("id");
        if (id.isBlank()) {
            return ApiResponse.badRequest("Missing preset id");
        }

        if (!catalog.hasHandRun(id)) {
            return ApiResponse.notFound("Hand-run verification file not found for preset: " + id);
        }

        Path dsPath = catalog.datasetPath(id);
        TextDatasetReader reader = new TextDatasetReader(dataDir);
        MiningInput input = reader.read(dsPath.getFileName().toString());

        Path handRunPath = catalog.handRunPath(id);
        VerificationReport report = MiningFacade.verify(input, handRunPath);

        Map<String, Object> resp = JsonMapper.verificationToJson(report);
        return ApiResponse.ok(resp);
    }
}
