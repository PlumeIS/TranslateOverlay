package cn.plumc.translateoverlay.components.animation.animas;

import cn.plumc.translateoverlay.components.animation.AnimaFunc;

public class SemFunction extends AnimaFunc {
    @Override
    public double calc(double t) {
        return Math.pow(1-Math.pow(t-1, 2D), 0.5D);
    }
}
