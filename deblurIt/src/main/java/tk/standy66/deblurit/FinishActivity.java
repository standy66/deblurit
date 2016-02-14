package tk.standy66.deblurit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tk.standy66.deblurit.tools.GlobalSettings;
import tk.standy66.deblurit.tools.Utils;

public class FinishActivity extends AppCompatActivity {
    public static final String IMAGE_URI = "tk.standy66.deblurit.IMAGE_URI";

    private final static int REQUEST_WRITE_INTERNAL_AND_SAVE = 1;
    private final static int REQUEST_WRITE_INTERNAL_AND_SAVE_AND_OPEN = 0;

    private String processedImagePathInternal;
    private Uri savedImageUri = null;
    private Bitmap fullBmp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        ImageView iv = (ImageView)findViewById(R.id.finish_preview);
        if (getIntent() != null) {
            processedImagePathInternal = getIntent().getStringExtra(IMAGE_URI);
            if (processedImagePathInternal != null) {
                fullBmp = BitmapFactory.decodeFile(processedImagePathInternal);
                iv.setImageBitmap(fullBmp);

                iv.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(FinishActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(FinishActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_WRITE_INTERNAL_AND_SAVE_AND_OPEN);
                        } else {
                            if (savedImageUri == null) {
                                savedImageUri = saveToGallery();
                                Toast.makeText(FinishActivity.this, R.string.finish_saved_toast, Toast.LENGTH_LONG).show();
                            }
                            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                            viewIntent.setDataAndType(savedImageUri, "image/*");
                            startActivity(viewIntent);
                        }
                    }
                });
            }
        }
    }


    private Uri saveToGallery() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DeblurIt");
        dir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        GlobalSettings gs = new GlobalSettings();
        File f = new File(dir, imageFileName + (gs.getFormat().equals("JPEG") ? ".jpg" : ".png"));
        try {
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            fullBmp.compress(gs.getFormat().equals("JPEG") ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        return contentUri;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_INTERNAL_AND_SAVE_AND_OPEN: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (savedImageUri == null) {
                        savedImageUri = saveToGallery();
                        Toast.makeText(this, R.string.finish_saved_toast, Toast.LENGTH_LONG).show();
                    }
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setDataAndType(savedImageUri, "image/*");
                    startActivity(viewIntent);
                } else {
                    Toast.makeText(this, R.string.finish_save_permission_denied_toast, Toast.LENGTH_LONG).show();
                }
                return;
            }

            case REQUEST_WRITE_INTERNAL_AND_SAVE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (savedImageUri == null) {
                        savedImageUri = saveToGallery();
                        Toast.makeText(this, R.string.finish_saved_toast, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.finish_save_permission_denied_toast, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_finish, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.analyticsLogScreenChange(getApplication(), "Finish");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_save:
            if (ContextCompat.checkSelfPermission(FinishActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(FinishActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_INTERNAL_AND_SAVE);
            } else {
                if (savedImageUri == null) {
                    savedImageUri = saveToGallery();
                    Toast.makeText(FinishActivity.this, R.string.finish_saved_toast, Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
