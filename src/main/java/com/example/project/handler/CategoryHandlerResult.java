package com.example.project.handler;

/**
 * Kết quả trả về từ CategoryRequestHandler.
 * Chứa thông tin redirect và trạng thái success/failure.
 */
public class CategoryHandlerResult {
    private String redirectUrl;
    private boolean success;

    private CategoryHandlerResult(String redirectUrl, boolean success) {
        this.redirectUrl = redirectUrl;
        this.success = success;
    }

    public static CategoryHandlerResult redirect(String url, boolean success) {
        return new CategoryHandlerResult(url, success);
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "CategoryHandlerResult{" +
                "redirectUrl='" + redirectUrl + '\'' +
                ", success=" + success +
                '}';
    }
}
