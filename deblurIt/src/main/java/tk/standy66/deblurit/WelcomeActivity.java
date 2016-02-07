package tk.standy66.deblurit;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import tk.standy66.deblurit.filtering.Pipeline;
import tk.standy66.deblurit.filtering.ProcessingContext;
import tk.standy66.deblurit.filtering.blur.Blur;
import tk.standy66.deblurit.filtering.blur.GaussianBlur;
import tk.standy66.deblurit.filtering.filters.Filter;
import tk.standy66.deblurit.filtering.filters.SharpenFilter;
import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.GlobalSettings;
import tk.standy66.deblurit.tools.Utils;

public class WelcomeActivity extends AppCompatActivity {

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAimIy2EAQ4mXpHexUpR3V4GEQn2z9rEb2pN+JXEuc2jPToyLL5/X8hmYqIOrPE5Hba9I1IrJjfYaAH6IZ7NJ1SFpt5B1caT97qsVuGzAXzBVHXfVAjYPpXzSO6WrbiJrP2aBe6krQFjQiIirO1mmfLsWZ459tCmPVQ3zcn+f0Svi0K16oIztX4aADeHO9iytstpZm4WSeM5JCoJVCIVMvECTZa7qw2AR9SDu4EYP/IdliajZyPH0gAJknC8ZLWa7oZ2wODGVOoeAA8orHwctmt/K5pzxu7OsDY8AW6ZgoJrg1GNql4UZU/zbva1piEssE7kn6jjVy2Fa6+hRrZpWxGwIDAQAB";
    
    private final static int RESULT_ALLOW = 1;
    private final static int RESULT_DONT_ALLOW = 2;
    private final static int RESULT_RETRY = 3;
    private final static int RESULT_ERROR = 4;
    
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int TAKEPHOTO_RESULT_CODE = 2;
    public static final int GET_CONTENT_OPEN_FILE = 1;
    public static final int GET_CONTENT_TAKE_PHOTO = 2;
    
    // Generate your own 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[] {
        -42, 1, 98, 31, -128, 127, 13, 66, 10, 48, 1, 34, 4, -117, -9, -113, 0, 32, -64,
        89
    };
    
    boolean blocker = false;

    Handler handler;
    GlobalSettings gs;
    
    

    @Override
    protected void onResume() {
        App.locker = true;
        super.onResume();
        Utils.analyticsLogScreenChange(getApplication(), "Welcome");

    }
    
    File capturedImage;
    String choosedBitmapUri;
    
    private void postGetContent(int mode) {
        switch (mode) {
        case GET_CONTENT_OPEN_FILE:
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.preview_chooser_title)), PICKFILE_RESULT_CODE);

            break;

        case GET_CONTENT_TAKE_PHOTO:
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String path = gs.getSavePath();

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
                    f = new File(Utils.getRealPathFromURI(this, data.getData()));
                break;
            case TAKEPHOTO_RESULT_CODE:
                f = capturedImage;
                break;
        }
        if (f != null && f.exists()) {
            try {
                choosedBitmapUri = "file://" + f.getAbsolutePath();
                Log.i("PrevieActivity", choosedBitmapUri);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                InputStream is = App.getApplicationContext().getContentResolver().openInputStream(Uri.parse(choosedBitmapUri));
                BitmapFactory.decodeStream(is, null, opts);

                Log.i("WelcomeActivity", String.format("Opened image with bounds: %d %d", opts.outWidth, opts.outHeight));
                float radius = Math.min(opts.outWidth, opts.outHeight);
                radius /= 300;
                radius *= 7;
                Log.i("WelcomeActivity", String.format("Automatic radius: %f", radius));
                Blur b = new GaussianBlur(radius);
                ProcessingContext processingContext = new ProcessingContext(false);
                Filter filter = new SharpenFilter(b, 0.7);
                Pipeline p = new Pipeline(choosedBitmapUri, filter, processingContext);
                startService(new Intent(WelcomeActivity.this, ProcessingService.class).putExtra("pipeline", p));
                Intent processActivityIntent = new Intent(WelcomeActivity.this, ProgressActivity.class);
                startActivity(processActivityIntent);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_LONG).show();
            }
        }
        else
            Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_LONG).show();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        App.setApplicationContext(getApplicationContext());
        super.onCreate(savedInstanceState);
        gs = new GlobalSettings();
        setContentView(R.layout.activity_welcome);
        TextView takePhoto = (TextView)findViewById(R.id.take_photo_button);
        TextView openFile = (TextView)findViewById(R.id.open_file_button);
//        TextView previous = (TextView)findViewById(R.id.previous_button);
        
        takePhoto.setOnClickListener(new OnClickListener() {			
            public void onClick(View v) {
                if (blocker)
                    return;
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    final Intent i = new Intent(WelcomeActivity.this, DeconvolutionPreviewActivity.class);
                    i.putExtra(getResources().getString(R.string.intent_content_method), PreviewActivity.GET_CONTENT_TAKE_PHOTO);
                    Runnable r1 = new Runnable() {

                        public void run() {
                            startActivity(i);
                        }
                    };

                    Runnable r2 = new Runnable() {

                        public void run() {
                            postGetContent(GET_CONTENT_TAKE_PHOTO);

                        }
                    };
                    gs = new GlobalSettings();
                    if (gs.getMode().equals("Manual")) {
                        r1.run();
                    } else if (gs.getMode().equals("Auto")) {
                        r2.run();
                    } else {
                        buildModeDialog(r2, r1);
                    }
                } else
                    Toast.makeText(WelcomeActivity.this, R.string.toast_camera_not_supported, Toast.LENGTH_LONG).show();

            }
        });
        openFile.setOnClickListener(new OnClickListener() {			
            public void onClick(View v) {
                if (blocker)
                    return;
                final Intent i = new Intent(WelcomeActivity.this, DeconvolutionPreviewActivity.class);
                i.putExtra(getResources().getString(R.string.intent_content_method), PreviewActivity.GET_CONTENT_OPEN_FILE);
                Runnable r1 = new Runnable() {

                    public void run() {
                        startActivity(i);
                    }
                };

                Runnable r2 = new Runnable() {

                    public void run() {
                        postGetContent(GET_CONTENT_OPEN_FILE);

                    }
                };
                gs = new GlobalSettings();
                Log.i("WelcomeActivity", gs.getMode());
                if (gs.getMode().equals("Manual")) {
                    r1.run();
                } else if (gs.getMode().equals("Auto")) {
                    r2.run();
                } else {
                    buildModeDialog(r2, r1);
                }
            }
        });

//        previous.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (blocker)
//                    return;
//                Intent i = new Intent(WelcomeActivity.this, DeconvolutionPreviewActivity.class);
//                startActivity(i);
//            }
//        });
    }    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.activity_welcome, menu);
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
    
    void buildModeDialog(final Runnable positiveRunnable, final Runnable negativeRunnable) {
        Log.i("WelcomeActivity", "Creating mode dialog");

        new AlertDialog.Builder(WelcomeActivity.this).setMessage(getResources().getString(R.string.mode_dialog_text))
        .setPositiveButton(getResources().getString(R.string.mode_dialog_auto), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                positiveRunnable.run();
                String modeLocalized = getResources().getStringArray(R.array.modes_localized)[2];
                gs.setMode("Auto");
            }
        }).setNegativeButton(getResources().getString(R.string.mode_dialog_manual), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                negativeRunnable.run();
                String modeLocalized = getResources().getStringArray(R.array.modes_localized)[1];
                gs.setMode("Manual");
            }
        }).create().show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
}
