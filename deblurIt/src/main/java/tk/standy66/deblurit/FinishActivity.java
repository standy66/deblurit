package tk.standy66.deblurit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import tk.standy66.deblurit.tools.CapturePhotoUtils;
import tk.standy66.deblurit.tools.Utils;

public class FinishActivity extends AppCompatActivity {

    public static final String IMAGE_URI = "tk.standy66.deblurit.IMAGE_URI";

    private String image;
    private Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        ImageView iv = (ImageView)findViewById(R.id.finish_preview);
        Intent i = getIntent();
        if (i != null) {
            image = i.getStringExtra(IMAGE_URI);
            if (image != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(image, opts);
                int reqWidth = 512;
                int reqHeight = 512;

                int scaling = Math.min(opts.outWidth / reqWidth, opts.outHeight / reqHeight);
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = scaling;
                uri = Uri.parse("file://" + image);
                Log.i("FinishActivity", uri.toString());
                Bitmap bmp = BitmapFactory.decodeFile(image, opts);
                iv.setImageBitmap(bmp);

                String result = CapturePhotoUtils.insertImage(getContentResolver(), bmp, uri.getLastPathSegment(), "DeblurIt result");

                Log.i("FinishActivity", result);
                iv.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(uri, "image/*");
                        startActivity(viewIntent);
                    }
                });
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
        case R.id.menu_delete:
            Log.i("FinishActivity", "Deleting file: " + image);
            File f = new File(image);
            boolean deleted = f.delete();
            if (!deleted)
                Toast.makeText(FinishActivity.this, getResources().getString(R.string.toast_error_deleting_file), Toast.LENGTH_LONG).show();
            else
                finish();
            break;
        case R.id.menu_save:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
