package com.nhlstenden.navigationapp.enums;

import com.nhlstenden.navigationapp.R;

public enum Difficulty
{
    ONE_STAR(R.drawable.star1),
    TWO_STAR(R.drawable.star2),
    THREE_STAR(R.drawable.star3);
    public final int starResId;

    Difficulty(int starResId)
    {
        this.starResId = starResId;
    }
}