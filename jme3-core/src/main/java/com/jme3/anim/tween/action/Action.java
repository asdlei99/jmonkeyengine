package com.jme3.anim.tween.action;

import com.jme3.anim.tween.Tween;

public abstract class Action implements Tween {

    protected Action[] actions;
    protected float weight = 1;
    protected double length;

    protected Action(Tween... tweens) {
        this.actions = new Action[tweens.length];
        for (int i = 0; i < tweens.length; i++) {
            Tween tween = tweens[i];
            if (tween instanceof Action) {
                this.actions[i] = (Action) tween;
            } else {
                this.actions[i] = new BaseAction(tween);
            }
        }
    }

    @Override
    public double getLength() {
        return length;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

}