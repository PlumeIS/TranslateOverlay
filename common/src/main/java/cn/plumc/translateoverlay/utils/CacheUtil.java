package cn.plumc.translateoverlay.utils;

import org.jetbrains.annotations.NotNull;

public class CacheUtil {
    public static class IntCounter implements Comparable<IntCounter> {
        private int value;
        public IntCounter(){
            this.value = 0;
        }
        public IntCounter(int value){
            this.value = value;
        }
        public int get(){
            return value;
        }
        public void add(){
            this.value++;
        }
        public void add(int value){
            this.value += value;
        }
        public void set(int value){
            this.value = value;
        }

        @Override
        public int compareTo(@NotNull CacheUtil.IntCounter o) {
            return this.value - o.value;
        }

        @Override
        public String toString() {
            return "IntCounter[%d]".formatted(value);
        }

        @Override
        public IntCounter clone() {
            return new IntCounter(value);
        }
    }
}
