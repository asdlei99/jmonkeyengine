/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.input.android;

import android.view.MotionEvent;
import android.view.View;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector2f;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AndroidTouchHandler14 is an extension of AndroidTouchHander that adds the
 * Android touch event functionality between Android rev 9 (Android 2.3) and 
 * Android rev 14 (Android 4.0).
 * 
 * @author iwgeric
 */
public class AndroidTouchHandler14 extends AndroidTouchHandler implements 
        View.OnHoverListener {
    private static final Logger logger = Logger.getLogger(AndroidTouchHandler14.class.getName());
    final private HashMap<Integer, Vector2f> lastHoverPositions = new HashMap<Integer, Vector2f>();
    
    public AndroidTouchHandler14(AndroidInputHandler androidInput, AndroidGestureHandler gestureHandler) {
        super(androidInput, gestureHandler);
    }

    @Override
    public void setView(View view) {
        if (view != null) {
            view.setOnHoverListener(this);
        } else {
            androidInput.getView().setOnHoverListener(null);
        }
        super.setView(view);
    }
    
    public boolean onHover(View view, MotionEvent event) {
        if (view == null || view != androidInput.getView()) {
            return false;
        }
        
        boolean consumed = false;
        int action = getAction(event);
        int pointerId = getPointerId(event);
        int pointerIndex = getPointerIndex(event);
        Vector2f lastPos = lastHoverPositions.get(pointerId);
        float jmeX;
        float jmeY;
        
        numPointers = event.getPointerCount();
        
        logger.log(Level.INFO, "onHover pointerId: {0}, action: {1}, x: {2}, y: {3}, numPointers: {4}", 
                new Object[]{pointerId, action, event.getX(), event.getY(), event.getPointerCount()});

        TouchEvent touchEvent;
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                jmeX = androidInput.getJmeX(event.getX(pointerIndex));
                jmeY = androidInput.invertY(androidInput.getJmeY(event.getY(pointerIndex)));
                touchEvent = androidInput.getFreeTouchEvent();
                touchEvent.set(TouchEvent.Type.HOVER_START, jmeX, jmeY, 0, 0);
                touchEvent.setPointerId(pointerId);
                touchEvent.setTime(event.getEventTime());
                touchEvent.setPressure(event.getPressure(pointerIndex));
                
                lastPos = new Vector2f(jmeX, jmeY);
                lastHoverPositions.put(pointerId, lastPos);
                
                processEvent(touchEvent);
                consumed = true;
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                // Convert all pointers into events
                for (int p = 0; p < event.getPointerCount(); p++) {
                    jmeX = androidInput.getJmeX(event.getX(p));
                    jmeY = androidInput.invertY(androidInput.getJmeY(event.getY(p)));
                    lastPos = lastHoverPositions.get(event.getPointerId(p));
                    if (lastPos == null) {
                        lastPos = new Vector2f(jmeX, jmeY);
                        lastHoverPositions.put(event.getPointerId(p), lastPos);
                    }

                    float dX = jmeX - lastPos.x;
                    float dY = jmeY - lastPos.y;
                    if (dX != 0 || dY != 0) {
                        touchEvent = androidInput.getFreeTouchEvent();
                        touchEvent.set(TouchEvent.Type.HOVER_MOVE, jmeX, jmeY, dX, dY);
                        touchEvent.setPointerId(event.getPointerId(p));
                        touchEvent.setTime(event.getEventTime());
                        touchEvent.setPressure(event.getPressure(p));
                        lastPos.set(jmeX, jmeY);

                        processEvent(touchEvent);

                    }
                }
                consumed = true;
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                jmeX = androidInput.getJmeX(event.getX(pointerIndex));
                jmeY = androidInput.invertY(androidInput.getJmeY(event.getY(pointerIndex)));
                touchEvent = androidInput.getFreeTouchEvent();
                touchEvent.set(TouchEvent.Type.HOVER_END, jmeX, jmeY, 0, 0);
                touchEvent.setPointerId(pointerId);
                touchEvent.setTime(event.getEventTime());
                touchEvent.setPressure(event.getPressure(pointerIndex));
                lastHoverPositions.remove(pointerId);

                processEvent(touchEvent);
                consumed = true;
                break;
            default:
                consumed = false;
                break;
        }
        
        return consumed;
    }
    
}
