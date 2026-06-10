package com.kevin.agents.dto;

import lombok.Data;

/**
 * @author Kevin
 * @since 2026/6/3
 */
@Data
public class StreamEvent {

    /**
     * 事件类型：chunk。done，error
     */
    private String type;

    /**
     * 内容（chunk 时为当前 token，error 时为错误信息）
     */
    private String content;

    /**
     * 时间戳(毫秒)
     */
    private long timestamp;

    public StreamEvent() {}

    public StreamEvent(String type, String content, long timestamp) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static StreamEvent chunk(String content) {
        return new StreamEvent("chunk", content, System.currentTimeMillis());
    }

    public static StreamEvent done() {
        return new StreamEvent("done", "", System.currentTimeMillis());
    }

    public static StreamEvent error(String error) {
        return new StreamEvent("error", error, System.currentTimeMillis());
    }
}
