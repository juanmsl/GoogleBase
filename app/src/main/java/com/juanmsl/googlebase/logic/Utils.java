package com.juanmsl.googlebase.logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.juanmsl.googlebase.R;
import com.juanmsl.googlebase.pathTracking.DownloadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static void drawPathBetween(LatLng origin, LatLng destiny, GoogleMap googleMap) {
        String url = obtenerDireccionesURL(origin, destiny);
        DownloadTask downloadTask = new DownloadTask(googleMap);
        downloadTask.execute(url);
    }

    public static String obtenerDireccionesURL(LatLng origin, LatLng destiny) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destiny.latitude + "," + destiny.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.i("Call Rest service", url);
        return url;
    }

    public static String downloadUrl(String strUrl) {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        }catch(Exception e){
            Log.e("Exception", e.toString());
        }finally{
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }
        return data;
    }

    public static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String imageFileName = "googleBase_" + timeStamp;
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }
}
