package joe.xkcd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    SharedPreferences mPrefs;
    ArrayList<Comic> mListData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        String searchQuery = getIntent().getExtras().getString("searchQuery");
        // Do something with value!
        //Toast.makeText(this, "Search = " + searchQuery, Toast.LENGTH_LONG).show();

        getSupportActionBar().setTitle("Results");

        initVars();

        mListData = searchForComics(searchQuery);

        if(mListData.size() == 0){
            Toast.makeText(this, "No results found",Toast.LENGTH_SHORT).show();
        }else{
            // specify an adapter (see also next example)
            SearchResultsActivityAdapter mAdapter = new SearchResultsActivityAdapter(this, mListData);
            ListView mListView = (ListView)findViewById(R.id.searchResultsList);
            mListView.setAdapter(mAdapter);
        }


    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_results, menu);
        return true;
    }*/

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

/*    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("comic", mListData.get(position));
        this.startActivity(i);

    }*/

    public ArrayList<Comic> searchForComics(String query){
        ArrayList<Comic> toReturn = new ArrayList<>();

        try {
            DB mDb = DBFactory.open(this, "comics"); //create or open an existing databse using the default name
            Log.d("D","IN SEARCH DEBUG AFTER OPENING DB" );
            int highestAmount = Integer.parseInt(mPrefs.getString("highestNum", "1500"));
            Log.d("D","IN SEARCH DEBUG with highestAmount = " + highestAmount );
            for(int i=0; i <= highestAmount; i++){

                if(mDb.exists("comic_"+i)){
                    String currentComic = mDb.get("comic_" + i);
                    if(currentComic.contains(query)){
                        Comic c = parseComic(currentComic);
                        toReturn.add(c);
                    }
                    Log.d("D","IN SEARCH DEBUG WITH CURRENT COMIC = " + currentComic);

                }

            }

            mDb.close();

        } catch (SnappydbException e) {
            Toast.makeText(this, "Something went wrong with favorites ", Toast.LENGTH_SHORT).show();
            Log.d("D","SEARCH DEBUG EXCEPTION E = " + e.getMessage());
        }

        return toReturn;
    }

    public void initVars(){
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
            toReturn.setJsonRepresentation(commentAsString);
        }catch (Exception e){

        }


        return toReturn;
    }
}
