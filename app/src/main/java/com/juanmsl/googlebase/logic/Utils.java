package com.juanmsl.googlebase.logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.juanmsl.googlebase.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Utils {

    private static final double RADIUS_OF_EARTH_KM = 6371;
    private static int checkType = 0;
    private static int checkStyle = 0;

    public static Bitmap cropImage(Bitmap image) {
        int width = Math.min(image.getWidth(), image.getHeight());
        int height = width;

        int x = (image.getWidth() - width) / 2;
        int y = 0;

        return Bitmap.createBitmap(image, x, y, width, height);
    }

    public static Bitmap getImageFormUri(Activity activity, Uri uri) {
        InputStream imageStream = null;
        try {
            imageStream = activity.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(imageStream);
    }

    public static void showMapTypeSelectorDialog(Activity activity, final GoogleMap mMap) {
        CharSequence[] MAP_TYPE_ITEMS = {"Road Map", "Satellite", "Hybrid"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Escoja el estilo del mapa");
        builder.setSingleChoiceItems(MAP_TYPE_ITEMS, checkType, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        default:
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1:
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2:
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            break;
                    }
                    dialog.dismiss();
                    checkType = item;
                }
            }
        );

        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    public static void showMapStyleSelectorDialog(final Activity activity, final GoogleMap mMap) {
        CharSequence[] MAP_TYPE_ITEMS = {"Diurno", "Nocturno", "Retro"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Escoja el tema para el road map");
        builder.setSingleChoiceItems(MAP_TYPE_ITEMS, checkStyle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        default:
                            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_day));
                            break;
                        case 1:
                            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_night));
                            break;
                        case 2:
                            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_retro));
                            break;
                    }
                    dialog.dismiss();
                    checkStyle = item;
                }
            }
        );

        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    public static double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))  * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a),  Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }
}
