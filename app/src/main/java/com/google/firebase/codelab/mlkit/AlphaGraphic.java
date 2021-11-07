// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.codelab.mlkit;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.google.firebase.codelab.mlkit.GraphicOverlay.Graphic;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class AlphaGraphic extends Graphic {

    private static final String TAG = "AlphaGraphic";

    private final Paint rectPaint;
    private final FirebaseVisionText.Line line;

    AlphaGraphic(GraphicOverlay overlay, FirebaseVisionText.Line line, String colorStr) {
        super(overlay);

        this.line = line;

        rectPaint = new Paint();
        rectPaint.setColor(Color.parseColor(colorStr));
        rectPaint.setStyle(Paint.Style.FILL);

        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "on draw text graphic");
        if (line == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(line.getBoundingBox());
        canvas.drawRect(rect, rectPaint);
    }
}
