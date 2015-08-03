package joe.xkcd;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.etsy.android.grid.util.DynamicHeightImageView;
public class FavoritesAdapter extends ArrayAdapter<Comic> {

    private static final String TAG = "SampleAdapter";
    AQuery aq;
    private final LayoutInflater mLayoutInflater;
    private final Random mRandom;
    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

    public FavoritesAdapter(Context context, int textViewResourceId,
                         ArrayList<Comic> objects) {
        super(context, textViewResourceId, objects);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRandom = new Random();
        aq = new AQuery(context);
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.staggered_grid_view,
                    parent, false);
            vh = new ViewHolder();
            vh.imgView = (DynamicHeightImageView) convertView
                    .findViewById(R.id.imgView);
            vh.title = (TextView)convertView.findViewById(R.id.comitTitleFavorite);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.imgView.setHeightRatio(getRandomHeightRatio());


        //ImageLoader.getInstance().displayImage(getItem(position), vh.imgView);
        aq.id(vh.imgView).image(getItem(position).getImg(), true, true, 0, 0, new BitmapAjaxCallback(){

            @Override
            public void callback(String url, final ImageView iv, final Bitmap bm, AjaxStatus status){

                iv.setImageBitmap(bm);

                iv.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("D", "FAVORITE ADAPTER WITH BM HEIGHT AND IV HEIGHT " + bm.getHeight() + " " + iv.getHeight());

                    }
                });



            }

        });

        vh.title.setText(getItem(position).title);
        return convertView;
    }

    static class ViewHolder {
        DynamicHeightImageView imgView;
        TextView title;
    }

/*    private double getPositionRatio(final int position, final int height) {
        double ratio = sPositionHeightRatios.get(position, (double)height);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
            Log.d(TAG, "getPositionRatio:" + position + " ratio:" + ratio);
        }
        return ratio;
    }*/

    private double getRandomHeightRatio() {
        return (mRandom.nextDouble() / 2.0) + 1.0; // height will be 1.0 - 1.5
        //return 1.0;
        // the width
    }
}