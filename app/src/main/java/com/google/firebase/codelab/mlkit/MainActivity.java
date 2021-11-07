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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.codelab.mlkit.GraphicOverlay.Graphic;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";

    private ImageView mImageView;
    // private Button mButton;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;
    private FloatingActionButton cameraButton;
    private TextView defaultText;

    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    //     0            1         2       3     4
    // Vegetarian, Pescatarian, Vegan, Gluten, Nut
    ArrayList<Integer> restrictionsSelected;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);
        defaultText = findViewById(R.id.textView);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        cameraButton = findViewById(R.id.floatingActionButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        selectRestrictionsAlert();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            // mSelectedImage = rotateBitmap((Bitmap) extras.get("data"),90);
            mSelectedImage = rotateBitmap((Bitmap) extras.get("data"),90);
            // mImageView.setImageBitmap(mSelectedImage);

            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
            defaultText.setText("");

            runTextRecognition();
        }
    }

    private void selectRestrictionsAlert() {
        Dialog dialog;
        final String[] items = {"Vegetarian", "Pescatarian", "Vegan", "Gluten Allergy", "Nut Allergy"};
        final ArrayList itemsSelected = new ArrayList();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Dietary Restrictions:");
        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedItemId,
                                        boolean isSelected) {
                        if (isSelected) {
                            itemsSelected.add(selectedItemId);
                        } else if (itemsSelected.contains(selectedItemId)) {
                            itemsSelected.remove(Integer.valueOf(selectedItemId));
                        }
                    }
                })
                .setPositiveButton("Done!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        restrictionsSelected = itemsSelected;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        restrictionsSelected = itemsSelected;
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    private void alert(String alertText) {
        AlertDialog.Builder builder1  = new AlertDialog.Builder(this);
        builder1.setMessage(alertText);

        AlertDialog alert = builder1.create();
        alert.show();
    }

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
//        mButton.setEnabled(false);
        detector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
//                                mButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
//                                mButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No menu text found");
            return;
        }
        mGraphicOverlay.clear();
        List<FirebaseVisionText.Line> lines = new ArrayList<FirebaseVisionText.Line>();
        for(FirebaseVisionText.TextBlock block : blocks) {
            for(FirebaseVisionText.Line line : block.getLines()) {
                lines.add(line);
            }
        }
        FirebaseVisionText.Line[] lineArr =
                lines.toArray(new FirebaseVisionText.Line[lines.size()]);
        List<MenuItem> items = TextCleaning.itemsFromText(lineArr);

        for(MenuItem chosen : items) {
            DietVector dv = new DietVector(chosen);
            boolean flag = true;
            outer: for (int num : restrictionsSelected) {
                switch(num) {
                    case 0:
                        if (!dv.isVegetarian()) {
                            flag = false;
                            break outer;
                        }
                        break;
                    case 1:
                        if (!dv.isPescatarian()) {
                            flag = false;
                            break outer;
                        }
                        break;
                    case 2:
                        if (!dv.isVegan()) {
                            flag = false;
                            break outer;
                        }
                        break;
                    case 3:
                        if (dv.hasGluten()) {
                            flag = false;
                            break outer;
                        }
                        break;
                    case 4:
                        if (dv.hasNuts()) {
                            flag = false;
                            break outer;
                        }
                        break;
                }
            }

            if (flag) {
                Graphic alphaGraphic = new AlphaGraphic(mGraphicOverlay, chosen.title, "#A000FF00");
                mGraphicOverlay.add(alphaGraphic);

                for(FirebaseVisionText.Line line : chosen.descriptions) {
                    alphaGraphic = new AlphaGraphic(mGraphicOverlay, line, "#A000FF00");
                    mGraphicOverlay.add(alphaGraphic);
                }
            } else {
                Graphic alphaGraphic = new AlphaGraphic(mGraphicOverlay, chosen.title, "#A0FF0000");
                mGraphicOverlay.add(alphaGraphic);

                for(FirebaseVisionText.Line line : chosen.descriptions) {
                    alphaGraphic = new AlphaGraphic(mGraphicOverlay, line, "#A0FF0000");
                    mGraphicOverlay.add(alphaGraphic);
                }
            }
        }
//        List<MenuItem> items = TextCleaning.itemsFromText(lineArr);
//        showToast(items.size() + "");
//        Random r = new Random();
//        int i = r.nextInt(items.size());
//        MenuItem chosen = items.get(i);
//        Graphic alphaGraphic = new AlphaGraphic(mGraphicOverlay, chosen.title, "#9000FF00");
//        mGraphicOverlay.add(alphaGraphic);
//        for(FirebaseVisionText.Line line : chosen.descriptions) {
//            alphaGraphic = new AlphaGraphic(mGraphicOverlay, line, "#90FF0000");
//            mGraphicOverlay.add(alphaGraphic);
//        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Functions for loading images from app assets.

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mGraphicOverlay.clear();
        switch (position) {
            case 0:
                mSelectedImage = getBitmapFromAsset(this, "Please_walk_on_the_grass.jpg");

                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                mSelectedImage = getBitmapFromAsset(this, "non-latin.jpg");
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "nl2.jpg");
                break;
        }
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
