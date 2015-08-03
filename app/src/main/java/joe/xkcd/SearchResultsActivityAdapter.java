package joe.xkcd;

import android.graphics.Bitmap;


import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchResultsActivityAdapter extends ArrayAdapter<Comic> {
    private final Activity context;
    private final ArrayList<Comic> names;
    AQuery aq;
    Context mContext;

    static class ViewHolder {
        public TextView title;
        public ImageView image;
    }

    public SearchResultsActivityAdapter(Activity context, ArrayList<Comic> names) {
        super(context, R.layout.card, names);
        this.context = context;
        this.names = names;
        aq = new AQuery(context);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.card, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.searchTitle);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.searchImage);
            rowView.setTag(viewHolder);
        }



        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Comic c = names.get(position);
        holder.title.setText(c.getTitle());

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, MainActivity.class);
                Log.d("D", "VIEW ANY COMIC DEBUG WITH JSON OF C = " + c.getJsonRepresentation());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("comic", c);
                mContext.startActivity(i);
            }
        });



        aq.id(holder.image).image(c.getImg(), true, true, 0, 0, new BitmapAjaxCallback() {

            @Override
            public void callback(String url, final ImageView iv, final Bitmap bm, AjaxStatus status) {
                iv.setImageBitmap(bm);
            }

        });



        return rowView;
    }
}