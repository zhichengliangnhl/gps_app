package com.nhlstenden.navigationapp.models;

public class AchievementProgress
{
    public final int current;
    public final int max;
    public final boolean isCompleted;

    public AchievementProgress(int current, int max, boolean isCompleted)
    {
        this.current = current;
        this.max = max;
        this.isCompleted = isCompleted;
    }

    public String getProgressText()
    {
        return String.format("Progress: %d/%d", current, max);
    }
}