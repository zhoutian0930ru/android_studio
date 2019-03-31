package com.rutgers.pocketwallet;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.rutgers.pocketwallet.ui.expenses.MyDatabaseHelper;
import com.rutgers.pocketwallet.ui.expenses.databaseManager;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Create a colored map overlay that visualises many points of weighted importance/intensity, with
 * different colors representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsActivity extends HM_BaseActivity {

    private SQLiteDatabase dbWrite,dbRead;
    private MyDatabaseHelper db;
    private databaseManager dbManager;
    private Cursor cursor;

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final HM_Gradient ALT_HEATMAP_HM_GRADIENT = new HM_Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private HashMap<String, DataSet> mLists = new HashMap<String, DataSet>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_heatmaps;
    }

    @Override
    public void showHeatmap() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40, -74), 5));

        // Set up the spinner/dropdown list
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerActivity());
        try {
            mLists.put(getString(R.string.user_locations), new DataSet(readItems(R.raw.defaultlocation), //****** 于此处定义json文件的名称，放在raw文件夹下 ******
                    getString(R.string.user_locations_url)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //mLists.put(getString(R.string.user_locations), new DataSet(readItems(), getString(R.string.user_locations_url)));

        // Make the handler deal with the map
        // Input: list of WeightedLatLngs, minimum and maximum zoom levels to calculate custom
        // intensity from, and the map to draw the heatmap on
        // radius, gradient and opacity not specified, so defaultlocation are used
    }

    public void changeRadius(View view) {
        if (mDefaultRadius) {
            mProvider.setRadius(ALT_HEATMAP_RADIUS);
        } else {
            mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
        }
        mOverlay.clearTileCache();
        mDefaultRadius = !mDefaultRadius;
    }

    public void changeGradient(View view) {
        if (mDefaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_HM_GRADIENT);
        } else {
            mProvider.setGradient(HeatmapTileProvider.DEFAULT_HM_GRADIENT);
        }
        mOverlay.clearTileCache();
        mDefaultGradient = !mDefaultGradient;
    }

    public void changeOpacity(View view) {
        if (mDefaultOpacity) {
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
        } else {
            mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
        }
        mOverlay.clearTileCache();
        mDefaultOpacity = !mDefaultOpacity;
    }

    // Dealing with spinner choices
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();

            // Check if need to instantiate (avoid setData etc twice)
            if (mProvider == null) {
                mProvider = new HeatmapTileProvider.Builder().data( mLists.get(getString(R.string.user_locations)).getData()).build();
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                // Render links
            } else {
                mProvider.setData(mLists.get(dataset).getData());
                mOverlay.clearTileCache();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems(int resource) throws JSONException {

        ArrayList<LatLng> list = new ArrayList<LatLng>();
//        InputStream inputStream = getResources().openRawResource(resource);
//        String json = new Scanner(inputStream).useDelimiter("\\A").next();
//        JSONArray array = new JSONArray(json);
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.getJSONObject(i);
//            double lat = object.getDouble("lat");
//            double lng = object.getDouble("lng");
//            list.add(new LatLng(lat, lng));
//        }
        //double lat=40.5216;
        //double lng=-74.4715;

        //list.add(new LatLng(lat,lng));

        db = new MyDatabaseHelper(HeatmapsActivity.this,"contact.db",null,1);


        dbRead = db.getWritableDatabase();
        String sql="SELECT _id,latitude,altitude FROM position";
        dbManager = new databaseManager(this);
        cursor=dbManager.executeSql(sql,null);
        cursor.moveToFirst();

        while(cursor.moveToNext()){
            String latitude=cursor.getString(cursor.getColumnIndex("latitude"));
            double value_latitude=Double.valueOf(latitude);
            String altitude=cursor.getString(cursor.getColumnIndex("altitude"));
            double value_altitude=Double.valueOf(altitude);
            list.add(new LatLng(value_latitude, value_altitude));
        }

        dbRead.close();
        if(list==null){
            double de_latitude = 40.5216;
            double de_longitude = -74.4715;
            list.add(new LatLng(de_latitude,de_longitude));
        }

        return list;
    }

    /**
     * Helper class - stores data sets and sources.
     */
    private class DataSet {
        private ArrayList<LatLng> mDataset;
        private String mUrl;

        public DataSet(ArrayList<LatLng> dataSet, String url) {
            this.mDataset = dataSet;
            this.mUrl = url;
        }

        public ArrayList<LatLng> getData() {
            return mDataset;
        }

        public String getUrl() {
            return mUrl;
        }
    }

}
