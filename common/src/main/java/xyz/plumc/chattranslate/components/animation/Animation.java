package xyz.plumc.chattranslate.components.animation;

import net.minecraft.client.MinecraftClient;

public class Animation {
    public record Time(float second, float deltaTime){}
    public static Time getTime(float second){return new Time(second,(1000f/ MinecraftClient.getInstance().getCurrentFps())/50);}

    public float start;
    public float end;
    public int step;
    public int current;
    public AnimaFunc func;
    public float last;

    public Animation(float start, float end, AnimaFunc func, int step) {
        this.start = start;
        this.end = end;
        this.func = func;
        this.step = step;
        this.current = 1;
    }

    public Animation(float start, float end, AnimaFunc func, Time time) {
        this.start = start;
        this.end = end;
        this.func = func;
        this.step = (int)(time.second * 20 / time.deltaTime);
        this.current = 1;
    }

    public float step(){
        if (current >= step) return end;
        double calced = func.calc(current/(double)step);
        current ++;
        last = (float) (start + (end - start) * calced);
        return last;
    }

    public String toString(){
        return "Animation<%s>[current=%s step=%s end=%s last=%s]".formatted(func.getClass().getSimpleName(), String.valueOf(current), String.valueOf(step), String.valueOf(end), String.valueOf(last));
    }
}
