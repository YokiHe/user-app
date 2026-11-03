package com.example;

public class Result {
    private int code;
    private String message;
    private Object data; // Object：既能装列表，也能装单个对象

    public Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功信封：code 200，data 装数据
    public static Result ok(Object data) {
        return new Result(200, "success", data);
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
