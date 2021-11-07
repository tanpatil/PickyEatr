package com.google.firebase.codelab.mlkit;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class DietVector {

    public static enum EnumDiet {
        MEAT("meat", new HashSet<String>(Arrays.asList(
                "meat", "chicken", "pork", "duck", "beef", "ham",
                "hamburger", "salami", "sausage", "dog", "prosciutto",
                "pepperoni", "lamb", "mutton", "veal", "turkey", "venison",
                "bologna", "bacon", "steak", "filet", "mignon", "ribs", "porc", "meatball",
                "meatballs"
        ))),
        DAIRY("dairy", new HashSet<String>(Arrays.asList(
                "cheese", "cream", "milk", "cheesy", "queso", "creamy", "dairy", "yogurt", "cheeses",
                "parmesan", "ricotta", "mozzarella", "asiago", "pepperjack", "gorgonzola"
        ))),
        NUT("nut", new HashSet<String>(Arrays.asList(
                "nut", "peanut", "almond", "cashew", "pecan", "pistachio", "chestnuts", "chestnut"
        ))),
        SEAFOOD("seafood", new HashSet<String>(Arrays.asList(
                "fish", "eel", "salmon", "tilapia", "mahi",
                "snapper", "shrimp", "crab", "lobster", "seafood", "oyster", "clam",
                "tuna", "crayfish", "swordfish", "octopus", "kalamari", "calamari", "squid",
                "flounder", "grouper", "halibut", "bass", "mussel", "scallop", "scallopini"
        ))),
        VEGETARIAN("vegetarian", new HashSet<String>(Arrays.asList(
                "vegetarian", "vegetable", "meatless", "tofu", "vegan", "impossible burger", "black bean burger",
                "bean", "tempeh", "seitan", "lettuce", "tortelloni"
        ))),
        ANIMAL_PROD("animal_prod", new HashSet<String>(Arrays.asList(
                "butter", "egg", "eggs"
        ))),
        GLUTEN("gluten", new HashSet<String>(Arrays.asList(
                "bread", "toast", "flour", "wonton", "noodle", "pasta", "fettuccine", "linguini",
                "spaghetti", "penne"
        ))),
        ;

        public final String name;
        public final Set<String> keywords;
        private EnumDiet(String name, Set<String> keywords) {
            this.name = name;
            this.keywords = keywords;
        }
    }

    private boolean[] dietVec;

    public DietVector() {
        this.dietVec = new boolean[EnumDiet.values().length];
    }

    public DietVector(MenuItem item) {
        this();
        for(String s : item.title.getText().split(" ")) {
            this.update(s);
        }
        for(FirebaseVisionText.Line line : item.descriptions) {
            for(String s : line.getText().split(" ")) {
                this.update(s);
            }
        }
    }

    public void update(String s) {
        if(s.isEmpty()) return;
        s = s.toLowerCase();
        for(int i = 0; i < EnumDiet.values().length; ++i) {
            this.dietVec[i] |= EnumDiet.values()[i].keywords.contains(s);
        }
        if (s.charAt(s.length() - 1) == 's') {
            this.update(s.substring(0, s.length() - 1));
        }
    }

    public boolean getFlag(EnumDiet diet) {
        return this.dietVec[diet.ordinal()];
    }

    public boolean isVegan() {
        return !(getFlag(EnumDiet.MEAT) || getFlag(EnumDiet.DAIRY) || getFlag(EnumDiet.SEAFOOD)
                || getFlag(EnumDiet.ANIMAL_PROD));
    }

    public boolean isPescatarian() {
        return !(getFlag(EnumDiet.MEAT));
    }

    public boolean isVegetarian() {
        return !(getFlag(EnumDiet.MEAT) || getFlag(EnumDiet.SEAFOOD));
    }

    public boolean hasNuts() {
        return getFlag(EnumDiet.NUT);
    }

    public boolean hasGluten() {
        return getFlag(EnumDiet.GLUTEN);
    }
}
