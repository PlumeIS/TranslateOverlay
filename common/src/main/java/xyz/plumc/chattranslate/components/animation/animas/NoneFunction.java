package xyz.plumc.chattranslate.components.animation.animas;

import xyz.plumc.chattranslate.components.animation.AnimaFunc;

public class NoneFunction extends AnimaFunc {
    @Override
    public double calc(double t) {
        return 1;
    }
}
