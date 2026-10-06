package tfus.web.command;

import tfus.service.PresetCatalog;
import tfus.web.ApiCommand;
import tfus.web.ApiRequest;
import tfus.web.ApiResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý GET /api/presets
 * Trả danh sách các preset khả dụng kèm cờ có file hand-run hay không.
 */
public final class ListPresetsCommand implements ApiCommand {

    private final PresetCatalog catalog;

    public ListPresetsCommand(PresetCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public ApiResponse execute(ApiRequest request) {
        List<String> ids = catalog.listPresetIds();
        List<Map<String, Object>> res = new ArrayList<>();
        for (String id : ids) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("hasHandRun", catalog.hasHandRun(id));
            res.add(m);
        }
        return ApiResponse.ok(res);
    }
}
