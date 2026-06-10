package com.kevin.agents.vision.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetInfo {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private String label;

    public int getWidth() {
        return x2 - x1;
    }

    public int getHeight() {
        return y2 - y1;
    }
}
