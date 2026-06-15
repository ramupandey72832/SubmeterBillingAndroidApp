package com.github.devfrogora.service.utils;

public class OperationResult<T> {
    private final boolean isSuccess;
    private final T data;
    private final String message;

    private OperationResult(boolean isSuccess, T data, String message) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.message = message;
    }

    public static <T> OperationResult<T> success(T data, String message) {
        return new OperationResult<>(true, data, message);
    }

    public static <T> OperationResult<T> failure(String message) {
        return new OperationResult<>(false, null, message);
    }

    public boolean isSuccess() { return isSuccess; }
    public T getData() { return data; }
    public String getMessage() { return message; }
}
