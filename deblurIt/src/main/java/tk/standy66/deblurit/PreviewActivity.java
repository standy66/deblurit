package tk.standy66.deblurit;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.GlobalSettings;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.ImageUtils;
import tk.standy66.deblurit.tools.MutableDouble;
import tk.standy66.deblurit.tools.SessionSettings;
import tk.standy66.deblurit.tools.Utils;
import tk.standy66.helper.ScrollablePreview;


public class PreviewActivity extends AppCompatActivity implements ActionBar.OnNavigationListener {
    ImageView previewImage;

    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int TAKEPHOTO_RESULT_CODE = 2;
    public static final int GET_CONTENT_OPEN_FILE = 1;
    public static final int GET_CONTENT_TAKE_PHOTO = 2;
    protected String choosedBitmapUri;
    protected String scaledBitmapUri;
    protected Bitmap scaledBitmap;
    protected ScrollablePreview previewLayout;
    protected int curPosition;
    protected float previewScaleFactor;
    private int previewSampling;
    protected SessionSettings sessionSettings;
    protected GlobalSettings globalSettings;
    protected int layoutId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        App.setApplicationContext(getApplicationContext());
        
        if (layoutId != 0)
            setContentView(layoutId);
        String[] firsts = getResources().getStringArray(R.array.modes);
        String[] seconds = getResources().getStringArray(R.array.mode_descriptions);
        int n = Math.max(firsts.length, seconds.length);
        Pair<String, String> values[] = (Pair<String, String>[]) Array.newInstance(Pair.class, n);
        for (int i = 0; i < n; i++)
            values[i] = new Pair<String, String>(firsts[i], seconds[i]);

        NavigationSpinnerAdapter list = new NavigationSpinnerAdapter(getSupportActionBar().getThemedContext(), R.layout.navigation_spinner, values);
        list.setDropDownViewResource(R.layout.navigation_spinner_dropdown);
        
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        setTitle("");

        globalSettings = new GlobalSettings();
        sessionSettings = new SessionSettings();
        readSessionSettings();
        int mode = getIntent().getIntExtra(getResources().getString(R.string.intent_content_method), -1);
        
        if (mode != -1 && App.locker) { 
            postGetContent(mode);
            App.locker = false;
        } else {    
            if (choosedBitmapUri == null)
                postGetContent();
            else
                initializePreview(false);
        }
        setSupportProgressBarIndeterminateVisibility(false);
    }
    
    @Override
    public void onDestroy() {
        if (scaledBitmap != null)
            scaledBitmap.recycle();
        //clearSharedPreferences();
        super.onDestroy();
    }
    
   
    
    void saveSessionSettings() {
        sessionSettings.setChoosedBitmap(choosedBitmapUri);
        sessionSettings.setScaledBitmap(scaledBitmapUri);
        sessionSettings.setScaleFactor(previewScaleFactor);
    }
    
    void readSessionSettings() {
        choosedBitmapUri = sessionSettings.getChoosedBitmap();
        scaledBitmapUri = sessionSettings.getScaledBitmap();
        previewScaleFactor = sessionSettings.getScaleFactor();
    }
    
    
    private void postGetContent() {
        postGetContent(GET_CONTENT_OPEN_FILE);
    }
    
    private File capturedImage;
    
    private void postGetContent(int mode) {
        switch (mode) {
        case GET_CONTENT_OPEN_FILE:
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.preview_chooser_title)), PICKFILE_RESULT_CODE);

            break;

        case GET_CONTENT_TAKE_PHOTO:
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            /*File storageDir = new File (
                    Environment.getExternalStorageDirectory()
                    + Environment.DIRECTORY_PICTURES
            );*/
            //storageDir.mkdirs();
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String path = globalSettings.getSavePath();

            try {
                File f = new File(path + "/temp/");
                f.mkdirs();
                f = new File(f, timeStamp + ".jpg");
                f.createNewFile();
                capturedImage = f;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(capturedImage));
            } catch (IOException e) {
                Toast.makeText(this, R.string.toast_error_creating_file, Toast.LENGTH_LONG).show();
            }
            startActivityForResult(takePictureIntent, TAKEPHOTO_RESULT_CODE);
            break;
        }
    }
    
    private String writeToTempFile() throws IOException {
        File f = new File(getExternalCacheDir().toString(), "preview.png");
        if (!f.exists())
            f.createNewFile();
    
        FileOutputStream fos = new FileOutputStream(f);
        scaledBitmap.compress(CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        return "file://" + f.getAbsolutePath();
    }
    
    protected boolean imageSelected = false;
    
    protected void initializePreview(boolean recreateScaledBitmap) {
        if (choosedBitmapUri == null) {
            imageSelected = false;
            return;
        }
        imageSelected = true;
        previewImage = (ImageView)findViewById(R.id.preview_image);

        try {
            if (scaledBitmapUri == null || recreateScaledBitmap) {
                MutableDouble outScaling = new MutableDouble(1);
                scaledBitmap = ImageUtils.decodeFileScaled(Uri.parse(choosedBitmapUri), 512, true, outScaling);
                previewScaleFactor = (float)outScaling.value;
                scaledBitmapUri = writeToTempFile();
            } else {
                InputStream is = getContentResolver().openInputStream(Uri.parse(scaledBitmapUri));
                scaledBitmap = BitmapFactory.decodeStream(is);

            }
            previewImage.setImageBitmap(scaledBitmap);
        } catch (Throwable t) {
            if (t instanceof FileNotFoundException) {
                Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_LONG).show();
                postGetContent();
            } else
                throw new RuntimeException(t);
        }

    }
    
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Log.i("PreviewActivity", "Got item position: " + itemPosition);

        if (curPosition == -1) {
            curPosition = itemPosition;
            return true;
        }
        if (curPosition == itemPosition)
            return true;

        Intent i = null;
        switch (itemPosition) {
        case 0:
            i = new Intent(this, UnsharpPreviewActivity.class);
            break;
        case 1:
            i = new Intent(this, DeconvolutionPreviewActivity.class);
            break;
        }
        startActivity(i);
        finish();

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menu_settings:
            startActivity(new Intent(this, GlobalPreferenceActivity.class));
            break;
           
            
        case R.id.menu_about:
            startActivity(new Intent(this, AboutActivity.class));
            break;


        case R.id.menu_open:
            postGetContent(GET_CONTENT_OPEN_FILE);
            break;

        case R.id.menu_takephoto:
            postGetContent(GET_CONTENT_TAKE_PHOTO);
            break;

        case R.id.menu_rateapp:
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "http://play.google.com/store/apps/details?id=tk.standy66.deblurit"));
                startActivity(marketIntent);		
            break;

        case R.id.menu_help:
            Intent i = new Intent(this, HelpActivity.class);
            startActivity(i);
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void onAsyncTaskResult(DeconvolutionAsyncTask task, Image result) {
        Log.i("Preview activity", "Bitmap must be set now");
        setSupportProgressBarIndeterminateVisibility(false);
        previewImage.setImageBitmap(result.toBitmap());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, R.string.toast_error_opening_file, Toast.LENGTH_LONG).show();
            return;
        }
        File f = null;
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (data.getData() != null)
                    Log.i("PreviewActivity", Utils.getRealPathFromURI(data.getData()));
                    f = new File(Utils.getRealPathFromURI(data.getData()));
                break;
            case TAKEPHOTO_RESULT_CODE:
                f = capturedImage;
                break;
        }
        if (f != null && f.exists()) {
            choosedBitmapUri = "file://" + f.getAbsolutePath();
            Log.i("PrevieActivity", choosedBitmapUri);
            try {
                initializePreview(true);
                saveSessionSettings();
            } catch (NullPointerException e) {
                Toast.makeText(this, R.string.toast_error_opening_file, Toast.LENGTH_LONG).show();
            }
        }
        else
            Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_LONG).show();
    }

    
}
