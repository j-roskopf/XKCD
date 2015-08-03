package joe.xkcd;

/**
 * Created by Joe on 7/17/2015.
 */
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.davemorrissey.labs.subscaleview.ImageSource;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MenuAdapter extends ArrayAdapter<Comic> {
    private final Activity context;
    private final ArrayList<Comic> comics;
    AQuery aq;
    SimpleDateFormat fmt = new SimpleDateFormat("E, MMM dd");


    static class ViewHolder {
        public TextView title;
        public ImageView image;
        public TextView number;
        public Bitmap bitmap;
    }

    public MenuAdapter(Activity context, ArrayList<Comic> comics) {
        super(context, R.layout.menu_item, comics);
        this.context = context;
        this.comics = comics;
        aq = new AQuery(context);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.menu_item, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.menu_comic_title);
            viewHolder.number = (TextView) rowView.findViewById(R.id.menu_comic_number);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.menu_icon);
            rowView.setTag(viewHolder);
        }

        // fill data
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final Comic c = comics.get(position);
        holder.title.setText(c.getTitle());
        Log.d("D", "POPULATE MENU DEBUG WITH TITLE IN ADAPTER = " + c.getTitle());
        holder.number.setText(c.getNum() + " " + fmt.format(new Date(c.getYear()+"/"+c.getMonth()+"/"+c.getDay())));
        //aq.id(holder.image).image(c.getImg(), true, true, 0, 0, null, 0, 1.0f);

        aq.id(holder.image).image(c.getImg(), true, true, 0, 0, new BitmapAjaxCallback(){

            @Override
            public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status){

                Log.d("D", "IN CALLBACK OF MENU ADAPTER FOR FETCHING IMAGE");
                iv.setImageBitmap(bm);
                holder.bitmap = bm;


            }

        });

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("comic",c);
                context.startActivity(i);
                context.finish();
            }
        });







        return rowView;
    }

}
