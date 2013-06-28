/*
 *  Copyright (c) 2012, The Linux Foundation. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *      * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 *  ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 *  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package com.mogoweb.browser.views;

import java.lang.ref.WeakReference;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

public class AnimationUtils {

    public interface Callbacks {
        void onAnimationEnded(View view);
    }

    public static void playXmlAnimation(View view, int animResId, Context ctx, Callbacks callbacks) {
        try {
            Animation animation = android.view.animation.AnimationUtils.loadAnimation(ctx, animResId);
            if (animation == null)
                return;
            view.clearAnimation();
            if (callbacks != null)
                installAnimationCallbacks(animation, view, callbacks);
            view.startAnimation(animation);
        } catch (Exception e) {}
    }

    public static ObjectAnimator fadeOut(View view, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0);
        animator.setDuration(duration);

        addGoneAtAnimationEnd(view, animator);

        // go for it
        animator.start();

        return animator;
    }

    public static ObjectAnimator fadeIn(View view, int duration, int delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f);
        animator.setDuration(duration);
        if (delay > 0)
            animator.setStartDelay(delay);

        // hide the view (and reset alpha) if visible
        view.setAlpha(0.0f);
        if (view.getVisibility() == View.VISIBLE)
            view.setVisibility(View.INVISIBLE);

        // show the view upon start
        final WeakReference<View> viewRef = new WeakReference<View>(view);
        animator.addListener(new AnimatorListenerStub() {
            @Override
            public void onAnimationStart(Animator animation) {
                View view = viewRef.get();
                if (view != null)
                    view.setVisibility(View.VISIBLE);
            }
        });

        // go for it
        animator.start();

        return animator;
    }

    public static ObjectAnimator animateTranslateXAndHide(final View view, float initialX, float finalX, int duration, int delay) {
        ObjectAnimator animator = animateTranslateX(view, initialX, finalX, duration, delay);

        addGoneAtAnimationEnd(view, animator);

        return animator;
    }

    public static ObjectAnimator animateTranslateX(final View view, float initialX, float finalX, int duration, int delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "TranslationX", initialX, finalX);
        animator.setDuration(duration);
        if (delay > 0)
            animator.setStartDelay(delay);

        // go for it
        animator.start();

        return animator;
    }

    public static void spinOnceX(View view, int cameraDistance, int duration, boolean forward) {
        view.setCameraDistance(cameraDistance);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationX", 0.0f, forward ? 360.0f : -360.0f);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setDuration(duration);
        animator.start();
    }

    public static void spinForever(View view, int cameraDistance, int duration) {
        view.setCameraDistance(cameraDistance);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0.0f, 360.0f);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setDuration(duration);
        animator.start();
    }

    private static void addGoneAtAnimationEnd(View view, ObjectAnimator animator) {
        final WeakReference<View> viewRef = new WeakReference<View>(view);
        animator.addListener(new AnimatorListenerStub() {
            @Override
            public void onAnimationEnd(Animator animation) {
                View view = viewRef.get();
                if (view != null)
                    view.setVisibility(View.GONE);
            }
        });
    }

    public static class AnimatorListenerStub implements Animator.AnimatorListener {
        @Override
        public void onAnimationCancel(Animator animation) {}

        @Override
        public void onAnimationEnd(Animator animation) {}

        @Override
        public void onAnimationRepeat(Animator animation) {}

        @Override
        public void onAnimationStart(Animator animation) {}
    }

    private static void installAnimationCallbacks(Animation animation, View view, final Callbacks callbacks) {
        final WeakReference<View> viewRef = new WeakReference<View>(view);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                View view = viewRef.get();
                if (view != null)
                    callbacks.onAnimationEnded(view);
            }
        });
    }


}
