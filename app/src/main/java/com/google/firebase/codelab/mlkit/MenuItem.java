package com.google.firebase.codelab.mlkit;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class MenuItem {

	public FirebaseVisionText.Line title;
	public FirebaseVisionText.Line[] descriptions;

	public MenuItem(FirebaseVisionText.Line title, FirebaseVisionText.Line[] descriptions) {
		this.title = title;
		this.descriptions = descriptions;
	}

}