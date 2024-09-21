package cn.plumc.translateoverlay.components.animation.animas;

import cn.plumc.translateoverlay.components.animation.AnimaFunc;

public class NoneFunction extends AnimaFunc {
    @Override
    public double calc(double t) {
        return 1;
    }
}
