package com.limsphere.pe.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;

import com.limsphere.pe.R;

import java.util.ArrayList;
import java.util.List;

public class ColorUtils {
    /**
     * Loads solid colors from resources
     *
     * @param context Context
     * @return List of solid colors as Integers
     */
    public static List<Integer> loadSolidColors(Context context) {
        List<Integer> solidColors = new ArrayList<>();
        TypedArray colorsArray = context.getResources().obtainTypedArray(R.array.solid_color_list);

        for (int i = 0; i < colorsArray.length(); i++) {
            solidColors.add(colorsArray.getColor(i, Color.BLACK));
        }

        colorsArray.recycle();
        return solidColors;
    }

    /**
     * Loads gradient color pairs from resources
     *
     * @param context Context
     * @return List of gradient color pairs as int arrays
     */
    public static List<int[]> loadGradientColors(Context context) {
        List<int[]> gradients = new ArrayList<>();

        try {
            // Get the master array of gradients
            TypedArray gradientArrays = context.getResources().obtainTypedArray(R.array.gradient_colors);

            for (int i = 0; i < gradientArrays.length(); i++) {
                // Get reference to each individual gradient array
                int gradientResId = gradientArrays.getResourceId(i, -1);
                if (gradientResId != -1) {
                    try {
                        TypedArray gradient = context.getResources().obtainTypedArray(gradientResId);
                        int[] colors = new int[gradient.length()];

                        for (int j = 0; j < gradient.length(); j++) {
                            colors[j] = gradient.getColor(j, Color.BLACK); // Default to black if color not found
                        }

                        gradients.add(colors);
                        gradient.recycle();
                    } catch (Resources.NotFoundException e) {
                        Log.e("GradientLoader", "Gradient not found at position " + i, e);
                    }
                }
            }

            gradientArrays.recycle();
        } catch (Resources.NotFoundException e) {
            Log.e("GradientLoader", "Master gradient array not found", e);
            // Fallback to programmatic gradients if resource loading fails
            gradients.add(new int[]{Color.RED, Color.YELLOW});
            gradients.add(new int[]{Color.GREEN, Color.BLUE});
            // Add more default gradients as needed
        }

        return gradients;
    }

    /**
     * Combines solid colors and gradients into a single list
     *
     * @param solidColors    List of solid colors
     * @param gradientColors List of gradient color pairs
     * @return Combined list with both types
     */
    public static List<Object> combineColorItems(List<Integer> solidColors, List<int[]> gradientColors) {
        List<Object> combined = new ArrayList<>();
        combined.addAll(solidColors);
        combined.addAll(gradientColors);
        return combined;
    }
}