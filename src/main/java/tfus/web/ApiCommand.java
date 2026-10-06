package tfus.web;

/**
 * Command design pattern interface cho các endpoint xử lý API.
 */
@FunctionalInterface
public interface ApiCommand {
    ApiResponse execute(ApiRequest request) throws Exception;
}
