package joe.xkcd;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;


public class FullImageActivity extends ActionBarActivity {

    AQuery aq;
    com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        getSupportActionBar().hide();

        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(R.id.fullImage);
        imageView.setMaxScale(10);
        aq = new AQuery(this);
        image = (com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView)findViewById(R.id.fullImage);
        String imageUrl = getIntent().getExtras().getString("image");
        Log.d("D", "RETREIVING URL IN FULL IMAGE ACTIVITY = " + imageUrl);
        aq.id(R.id.fullImageRegular).image(imageUrl, true, true, 0, 0, new BitmapAjaxCallback() {

            @Override
            public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                Log.d("D", "RETREIVING URL IN CALLBACK = " + status.getCode());

                imageView.setImage(ImageSource.bitmap(bm));

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
