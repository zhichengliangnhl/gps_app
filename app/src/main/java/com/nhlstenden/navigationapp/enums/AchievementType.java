// AchievementType.java (Enum)
package com.nhlstenden.navigationapp.enums;

import android.content.Context;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.AchievementManager;
import com.nhlstenden.navigationapp.helpers.CoinManager;
import com.nhlstenden.navigationapp.models.AchievementProgress;

public enum AchievementType
{
    FIRST_STEPS("First steps", "Create a waypoint", Difficulty.ONE_STAR, 100)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = AchievementManager.getFirstStepsProgress(context);
                    return new AchievementProgress(progress, 1, progress >= 1);
                }
            },
    RUNNER_I("Runner I", "Complete a waypoint", Difficulty.ONE_STAR, 200)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = AchievementManager.getRunnerIProgress(context);
                    return new AchievementProgress(progress, 1, progress >= 1);
                }
            },
    RUNNER_II("Runner II", "Complete 5 waypoints", Difficulty.TWO_STAR, 1000)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = AchievementManager.getRunnerIIProgress(context);
                    return new AchievementProgress(progress, 5, progress >= 5);
                }
            },
    RUNNER_III("Runner III", "Complete 10 waypoints", Difficulty.THREE_STAR, 5000)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = AchievementManager.getRunnerIIIProgress(context);
                    return new AchievementProgress(progress, 10, progress >= 10);
                }
            },
    GRINDER_I("Grinder I", "Earn 1000 coins", Difficulty.ONE_STAR, 500)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = CoinManager.getCoins(context);
                    return new AchievementProgress(progress, 1000, progress >= 1000);
                }
            },
    GRINDER_II("Grinder II", "Earn 10.000 coins", Difficulty.TWO_STAR, 2000)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = CoinManager.getCoins(context);
                    return new AchievementProgress(progress, 10000, progress >= 10000);
                }
            },
    GRINDER_III("Grinder III", "Earn 100.000 coins", Difficulty.THREE_STAR, 10000)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = CoinManager.getCoins(context);
                    return new AchievementProgress(progress, 100000, progress >= 100000);
                }
            },
    COLLECTIONISTA("Collectionista", "Collect all achievements", Difficulty.THREE_STAR, 10000)
            {
                @Override
                public AchievementProgress getProgress(Context context)
                {
                    int progress = AchievementManager.getCollectionistaProgress(context);
                    return new AchievementProgress(progress, 7, progress >= 7);
                }
            };

    private final String title;
    private final String description;
    private final Difficulty difficulty;
    private final int reward;

    AchievementType(String title, String description, Difficulty difficulty, int reward)
    {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.reward = reward;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public Difficulty getDifficulty()
    {
        return this.difficulty;
    }

    public int getReward()
    {
        return this.reward;
    }

    public abstract AchievementProgress getProgress(Context context);
}
