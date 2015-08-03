package joe.xkcd;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.json.JSONObject;

public class FavoritesActivity extends Activity implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener{

    private static final String TAG = "StaggeredGridActivity";
    public static final String SAVED_DATA_KEY = "SAVED_DATA";

    private StaggeredGridView mGridView;
    private boolean mHasRequestedMore;
    private FavoritesAdapter mAdapter;
    private ArrayList<Comic> mListData;
    private Activity mContext;

    private ArrayList<String> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mContext = this;

        setTitle("TechnoTalkative - SGV Demo");
        mGridView = (StaggeredGridView) findViewById(R.id.grid_view);
        mAdapter = new FavoritesAdapter(this,android.R.layout.simple_list_item_1, generateData());
        // do we have saved data?
        if (savedInstanceState != null) {
            mData = savedInstanceState.getStringArrayList(SAVED_DATA_KEY);
        }



        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(this);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SAVED_DATA_KEY, mData);
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        Log.d(TAG, "onScrollStateChanged:" + scrollState);
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        Log.d(TAG, "onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);
        // our handling
/*        if (!mHasRequestedMore) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                Log.d(TAG, "onScroll lastInScreen - so load more");
                mHasRequestedMore = true;
                onLoadMoreItems();
            }
        }*/
    }

/*    private void onLoadMoreItems() {
        final ArrayList<Comic> sampleData = generateData();
        for (String data : sampleData) {
            mAdapter.add(data);
        }
        // stash all the data in our backing store
        mData.addAll(sampleData);
        // notify the adapter that we can update now
        mAdapter.notifyDataSetChanged();
        mHasRequestedMore = false;
    }*/

    private ArrayList<Comic> generateData() {
        mListData = new ArrayList<Comic>();
/*        listData.add("http://imgs.xkcd.com/comics/woodpecker.png");
        listData.add("http://imgs.xkcd.com/comics/avoidance.png");
        listData.add("http://imgs.xkcd.com/comics/lease.png");
        listData.add("http://imgs.xkcd.com/comics/understocked.png");
        listData.add("http://imgs.xkcd.com/comics/asteroid.png");
        listData.add("http://imgs.xkcd.com/comics/supported_features.png");
        listData.add("http://imgs.xkcd.com/comics/wings.png");*/

        try {
            DB mDb = DBFactory.open(this, "comics"); //create or open an existing databse using the default name
            int placeToInsert =0;
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int highestAmount = Integer.parseInt(mPrefs.getString("highestNum", "1500"));
            for(int i=0; i <= highestAmount; i++){
                if(mDb.exists("favoriteComic_"+i)){
                    Log.d("D","SEARCHING THROUGH FAV DEBUG AND FAV FOUND WITH I = " + i);
                    String favoriteComic = mDb.get("favoriteComic_"+i);
                    Comic parsedComic = parseComic(favoriteComic);
                    mListData.add(parsedComic);

                }

            }

            mDb.close();

        } catch (SnappydbException e) {
            Toast.makeText(this, "Something went wrong with favorites ", Toast.LENGTH_SHORT).show();
        }

        return mListData;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        Intent i = new Intent(mContext,MainActivity.class);
        i.putExtra("comic",mListData.get(position));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(i);
        mContext.finish();

    }


    public Comic parseComic(String commentAsString){
        Comic toReturn = new Comic();
        try{
            JSONObject comic = new JSONObject(commentAsString);
            Log.d("D","POPULATE MENU DEBUG WITH COMIC = " + comic.toString());

            String month = comic.getString("month");
            String num = comic.getString("num");
            String link = comic.getString("link");
            String year = comic.getString("year");
            String news = comic.getString("news");
            String safe_title = comic.getString("safe_title");
            String transcript = comic.getString("transcript");
            String alt = comic.getString("alt");
            String img = comic.getString("img");
            String title = comic.getString("title");
            String day = comic.getString("day");


            toReturn.setMonth(month);
            toReturn.setNews(news);
            toReturn.setNum(num);
            toReturn.setLink(link);
            toReturn.setYear(year);
            toReturn.setSafeTitle(safe_title);
            toReturn.setTranscript(transcript);
            toReturn.setAlt(alt);
            toReturn.setImg(img);
            toReturn.setTitle(title);
            toReturn.setDay(day);
            toReturn.setJsonRepresentation(comic.toString());
        }catch (Exception e){

        }


        return toReturn;
    }
}
