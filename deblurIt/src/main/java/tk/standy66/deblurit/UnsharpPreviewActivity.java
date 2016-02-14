package tk.standy66.deblurit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import tk.standy66.deblurit.filtering.Pipeline;
import tk.standy66.deblurit.filtering.ProcessingContext;
import tk.standy66.deblurit.filtering.blur.Blur;
import tk.standy66.deblurit.filtering.blur.BlurType;
import tk.standy66.deblurit.filtering.blur.GaussianBlur;
import tk.standy66.deblurit.filtering.blur.MotionBlur;
import tk.standy66.deblurit.filtering.blur.OutOfFocusBlur;
import tk.standy66.deblurit.filtering.filters.Filter;
import tk.standy66.deblurit.filtering.filters.SharpenFilter;
import tk.standy66.deblurit.tools.Utils;

public class UnsharpPreviewActivity extends PreviewActivity {
    private BlurType blurType;
    private SeekBar strengthSeekbar, radiusSeekbar, angleSeekbar, lengthSeekbar;
    private TextView strengthValue, radiusValue, angleValue, lengthValue;
    private Button previewButton, processButton;


    public UnsharpPreviewActivity() {
        super();
        layoutId = R.layout.preview_activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        curPosition = 0;
        getSupportActionBar().setSelectedNavigationItem(curPosition);
        blurType = BlurType.GaussianBlur;
        reloadView();
    }

    private void postChangesToPreveiw() {

    }

    private void reloadView() {
        if (blurType == BlurType.MotionBlur)
            setContentView(R.layout.preview_activity_motionblur);
        else
            setContentView(R.layout.preview_activity);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.blur_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner)findViewById(R.id.preview_deconvolution_spinner);
        spinner.setAdapter(adapter);


        switch (blurType) {
        case OutOfFocusBlur:
            spinner.setSelection(0);
            break;
        case GaussianBlur:
            spinner.setSelection(1);
            break;
        case MotionBlur:
            spinner.setSelection(2);
            break;
        }

        ((TextView)findViewById(R.id.strength_label)).setText(getResources().getString(R.string.preview_label_strength));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                BlurType newBlurType = null;
                switch(position) {
                case 0:
                    newBlurType = BlurType.OutOfFocusBlur;
                    break;
                case 1:
                    newBlurType = BlurType.GaussianBlur;
                    break;
                case 2:
                    newBlurType = BlurType.MotionBlur;
                    break;
                }
                if (newBlurType == blurType)
                    return;
                blurType = newBlurType;

                reloadView();
            }

            public void onNothingSelected(AdapterView<?> parent) {


            }
        });

        strengthSeekbar = (SeekBar)findViewById(R.id.strength_seekbar);
        strengthValue = (TextView)findViewById(R.id.strength_value);
        strengthSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {}
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                strengthValue.setText(String.valueOf(progress + 1));
            }
        });
        strengthSeekbar.setProgress(70);

        if (blurType == blurType.MotionBlur) {
            angleSeekbar = (SeekBar)findViewById(R.id.angle_seekbar);
            angleValue = (TextView)findViewById(R.id.angle_value);
            angleSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {}
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    angleValue.setText(String.valueOf(progress));
                    postChangesToPreveiw();
                }
            });

            lengthSeekbar = (SeekBar)findViewById(R.id.length_seekbar);
            lengthSeekbar.setMax(300);
            lengthValue = (TextView)findViewById(R.id.length_value);
            lengthSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {}
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    lengthValue.setText(String.valueOf((float)progress / 10));
                    postChangesToPreveiw();
                }
            });
        } else {
            radiusSeekbar = (SeekBar)findViewById(R.id.radius_seekbar);
            radiusSeekbar.setMax(300);
            radiusValue = (TextView)findViewById(R.id.radius_value);
            radiusSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {}
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    radiusValue.setText(String.valueOf((float)progress / 10));
                }
            });
            radiusSeekbar.setProgress(80);
        }

        previewButton = (Button)findViewById(R.id.preview_button);
        processButton = (Button)findViewById(R.id.process_button);
        previewButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!imageSelected)
                    return;
                setSupportProgressBarIndeterminateVisibility(true);
                Blur b = null;
                float strength = ((float)Integer.parseInt(strengthValue.getText().toString()) + 1) / 100;
                switch (blurType) {
                case OutOfFocusBlur:
                    float radius = Float.parseFloat(radiusValue.getText().toString());
                    if (previewScaleFactor > 1)
                        radius /= previewScaleFactor;
                    b = new OutOfFocusBlur(radius);
                    break;

                case GaussianBlur:
                    radius = Float.parseFloat(radiusValue.getText().toString());
                    if (previewScaleFactor > 1)
                        radius /= previewScaleFactor;
                    b = new GaussianBlur(radius);
                    break;

                case MotionBlur:
                    float angle = (float) Math.toRadians(Integer.parseInt(angleValue.getText().toString()));
                    float length = Float.parseFloat(lengthValue.getText().toString());
                    if (previewScaleFactor > 1)
                        length /= previewScaleFactor;
                    b = new MotionBlur(angle, length);
                    break;
                }
                if (b == null)
                    return;
                Filter f = new SharpenFilter(b, strength);

                ProcessingContext processingContext = new ProcessingContext(true);
                Pipeline p = new Pipeline(scaledBitmapUri, f, processingContext);

                new DeconvolutionAsyncTask(UnsharpPreviewActivity.this).execute(p);
            }
        });

        previewImage = (ImageView)findViewById(R.id.preview_image);

        processButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!imageSelected)
                    return;
                Blur b = null;
                float strength = ((float)Integer.parseInt(strengthValue.getText().toString()) + 1) / 100;
                switch (blurType) {
                case OutOfFocusBlur:
                    float radius = Float.parseFloat(radiusValue.getText().toString());
                    b = new OutOfFocusBlur(radius);
                    break;

                case GaussianBlur:
                    radius = Float.parseFloat(radiusValue.getText().toString());
                    b = new GaussianBlur(radius);
                    break;

                case MotionBlur:
                    float angle = (float) Math.toRadians(Integer.parseInt(angleValue.getText().toString()));
                    float length = Float.parseFloat(lengthValue.getText().toString());
                    b = new MotionBlur(angle, length);
                    break;
                }
                if (b == null)
                    return;
                Filter f = new SharpenFilter(b, strength);

                ProcessingContext processingContext = new ProcessingContext(false);
                Pipeline p = new Pipeline(choosedBitmapUri, f, processingContext);
                startService(new Intent(UnsharpPreviewActivity.this, ProcessingService.class).putExtra("pipeline", p));
                Intent processActivityIntent = new Intent(UnsharpPreviewActivity.this, ProgressActivity.class);
                startActivity(processActivityIntent);
            }
        });
        if (scaledBitmap != null)
            scaledBitmap.recycle();
        initializePreview(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.analyticsLogScreenChange(getApplication(), "Sharpen");
    }

}
