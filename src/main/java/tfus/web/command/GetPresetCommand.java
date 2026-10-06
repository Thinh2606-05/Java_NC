package tfus.web.command;

import tfus.domain.MiningInput;
import tfus.io.TextDatasetReader;
import tfus.service.PresetCatalog;
import tfus.web.ApiCommand;
import tfus.web.ApiRequest;
import tfus.web.ApiResponse;
import tfus.web.dto.JsonMapper;

import java.nio.file.Path;
import java.util.Map;

/**
 * Xử lý GET /api/presets/{id}
 * Đọc preset dạng text và trả về JSON có thể sửa trên giao diện.
 */
public final class GetPresetCommand implements ApiCommand {

    private final PresetCatalog catalog;
    private final Path dataDir;

    public GetPresetCommand(PresetCatalog catalog, Path dataDir) {
        this.catalog = catalog;
        this.dataDir = dataDir;
    }

    @Override
    public ApiResponse execute(ApiRequest request) throws Exception {
        String id = request.pathParam("id");
        if (id.isBlank()) {
            return ApiResponse.badRequest("Missing preset id");
        }

        Path dsPath = catalog.datasetPath(id);
        if (!dsPath.toFile().exists()) {
            return ApiResponse.notFound("Preset not found: " + id);
        }

        TextDatasetReader reader = new TextDatasetReader(dataDir);
        MiningInput input = reader.read(dsPath.getFileName().toString());

        Map<String, Object> json = JsonMapper.inputToJson(input);
        return ApiResponse.ok(json);
    }
}
