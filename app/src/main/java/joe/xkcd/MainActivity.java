package joe.xkcd;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    private ImageView mImageView;
    private View mOverlayView;
    private ObservableScrollView mScrollView;
    private TextView mTitleView;
    private View mFab;
    private com.github.clans.fab.FloatingActionMenu mOtherFab;
    private int mActionBarSize;
    private com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout mSwipeRefreshLayout;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private ListView mMenuListView;
    private int mFabMargin;
    private me.grantland.widget.AutofitTextView mComicTranscript;
    private me.grantland.widget.AutofitTextView mComicAlt;
    private me.grantland.widget.AutofitTextView mComicTitle;
    private me.grantland.widget.AutofitTextView mComicDate;
    private com.github.clans.fab.FloatingActionButton mFabShare;
    private com.github.clans.fab.FloatingActionButton mFabDownload;
    private com.github.clans.fab.FloatingActionButton mFabBrowser;
    private com.github.clans.fab.FloatingActionButton mFabExplanation;
    private boolean mFabIsShown;
    private boolean mFabIsShownOther;
    AQuery aqCurrent;
    AQuery aq;
    SimpleDateFormat fmt;
    Context mContext;
    Comic mComic;
    SlidingMenu mMenu;
    ImageView mFavoriteHeart;
    SharedPreferences mPrefs;
    DB mDb;
    com.github.lzyzsd.circleprogress.DonutProgress mProgress;
    ArrayList<Comic> allComics;
    ArrayList<Comic> currentComics;
    int currentOffset;
    final int AMOUNT_TO_OFFSET_BY = 50;
    private ActionBarDrawerToggle mDrawerToggle;
    Bitmap mCurrentBitmap;
    final String CURRENT_COMIC_URL = "http://xkcd.com/info.0.json";
    final String ANY_COMIC_URL_FIRST = "http://xkcd.com/";
    final String ANY_COMIC_URL_SECOND = "/info.0.json";
    final String EXPLAIN_URL = "http://www.explainxkcd.com/wiki/index.php/";
    final String XKCD_URL = "http://xkcd.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

        setupFabMenu();



        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int highestAmount = Integer.parseInt(mPrefs.getString("highestNum", "1500"));
                //find first available favorite
                try {
                    mDb = DBFactory.open(mContext, "comics"); //create or open an existing databse using the default name
                    int placeToInsert;
                    String comicToInsert = mPrefs.getString("currentComic", "None selected?");
                    for (int i = 0; i <= highestAmount; i++) {
                        if (mDb.exists("favoriteComic_" + i)) {
                            String favoriteComic = mDb.get("favoriteComic_" + i);
                            if (favoriteComic.equals(comicToInsert)) {
                                mDb.del("favoriteComic_" + i);
                                mFavoriteHeart.setVisibility(View.INVISIBLE);
                                break;
                            }

                        }
                        if (!mDb.exists("favoriteComic_" + i)) {
                            placeToInsert = i;
                            mDb.put("favoriteComic_" + placeToInsert, comicToInsert);
                            mDb.close();
                            mFavoriteHeart.setColorFilter(Color.RED);
                            mFavoriteHeart.setVisibility(View.VISIBLE);
                            break;
                        }
                    }


                } catch (SnappydbException e) {
                    Toast.makeText(MainActivity.this, "Something went wrong with favorites ", Toast.LENGTH_SHORT).show();

                }

            }
        });
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);




        Bundle b = getIntent().getExtras();

        if( b != null && b.containsKey("comic")){
            Log.d("D","VIEW ANY COMIC DEBUG");
            final Comic passedComic = b.getParcelable("comic");
            Log.d("D", "VIEW ANY COMIC DEBUG WITH JSON OF C = " + passedComic.getJsonRepresentation());


            getAllComics(mPrefs.getString("highestNum", "1500"));

            displayComic(passedComic);


        }else{

            //only open the slider drawer once after oncreate is called
            mPrefs.edit().putString("openSlider","yes").apply();

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setInverseBackgroundForced(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setTitle("Fetching latest...");

            String url = CURRENT_COMIC_URL;
            aq.progress(dialog).ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    dialog.dismiss();
                    Log.d("D", "FIRST IMAGE DEBUG WITH JSON = " + json.toString());
                    Log.d("D", "FIRST IMAGE DEBUG WITH URL = " + url);
                    try {
                        final String imageUrl = json.getString("img");

                        String month = json.getString("month");
                        String num = json.getString("num");
                        String link = json.getString("link");
                        String year = json.getString("year");
                        String news = json.getString("news");
                        String safe_title = json.getString("safe_title");
                        String transcript = json.getString("transcript");
                        String alt = json.getString("alt");
                        String img = json.getString("img");
                        String title = json.getString("title");
                        String day = json.getString("day");

                        mComic.setMonth(month);
                        mComic.setNews(news);
                        mComic.setNum(num);
                        mComic.setLink(link);
                        mComic.setYear(year);
                        mComic.setSafeTitle(safe_title);
                        mComic.setTranscript(transcript);
                        mComic.setAlt(alt);
                        mComic.setImg(img);
                        mComic.setTitle(title);
                        mComic.setDay(day);
                        mComic.setJsonRepresentation(json.toString());

                        displayComic(mComic);

                        mPrefs.edit().putString("highestNum",json.getString("num")).apply();

                        getAllComics(json.getString("num"));


                    } catch (Exception e) {
                    }
                }

            });
        }





    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate overlay and image
        Log.d("D","IN FIRST SCROLL DEBUG AND FIRSTSCROLL = " + firstScroll + " " + scrollY + " " + dragging);

        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        ViewHelper.setPivotX(mTitleView, 0);
        ViewHelper.setPivotY(mTitleView, 0);
        ViewHelper.setScaleX(mTitleView, scale);
        ViewHelper.setScaleY(mTitleView, scale);

        // Translate title text
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);
        int titleTranslationY = maxTitleTranslationY - scrollY;
        ViewHelper.setTranslationY(mTitleView, titleTranslationY);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mFab.getHeight() / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - mFab.getHeight() / 2,
                mActionBarSize - mFab.getHeight() / 2,
                maxFabTranslationY);

        Log.d("D","CREATING MAXFABTRANSLATION Y WITH SCROLLY = " + scrollY );
        Log.d("D","CREATING MAXFABTRANSLATION Y WITH mFlexibleSpaceImageHeight = " + mFlexibleSpaceImageHeight );
        Log.d("D","CREATING MAXFABTRANSLATION Y WITH mFab.getHeight() = " + mFab.getHeight() );
        Log.d("D","CREATING MAXFABTRANSLATION Y WITH mActionBarSize = " + scrollY );
        Log.d("D","CREATING MAXFABTRANSLATION Y WITH maxFabTranslationY = " + maxFabTranslationY );
        Log.d("D","CREATING MAXFABTRANSLATION Y WITH fabTranslationY = " + fabTranslationY );
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // On pre-honeycomb, ViewHelper.setTranslationX/Y does not set margin,
            // which causes FAB's OnClickListener not working.
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFab.getLayoutParams();
            lp.leftMargin = mOverlayView.getWidth() - mFabMargin - mFab.getWidth();
            lp.topMargin = (int) fabTranslationY;
            mFab.requestLayout();
        } else {
            ViewHelper.setTranslationX(mFab, mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
            ViewHelper.setTranslationY(mFab, fabTranslationY);
        }

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
            showOtherFab();
        } else {
            showFab();
            hideOtherFab();
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            ViewPropertyAnimator.animate(mFavoriteHeart).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    private void showOtherFab() {
        if (!mFabIsShownOther) {
            ViewPropertyAnimator.animate(mOtherFab).cancel();
            ViewPropertyAnimator.animate(mOtherFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShownOther = true;
        }
    }

    private void hideOtherFab() {
        if (mFabIsShownOther) {
            ViewPropertyAnimator.animate(mOtherFab).cancel();
            ViewPropertyAnimator.animate(mOtherFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShownOther = false;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            ViewPropertyAnimator.animate(mFavoriteHeart).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mMenu.toggle(true);
                return true;

            case R.id.action_search:
                displayPopup();
                return true;

            case R.id.action_number:
                displayNumberPopup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initVars() {
        currentOffset = 50;
        allComics = new ArrayList<>();
        currentComics = new ArrayList<>();
        mComic = new Comic();
        aq = new AQuery(this);
        aqCurrent = new AQuery(this);
        fmt = new SimpleDateFormat("E, MMM dd");
        mContext = this;
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mActionBarSize = getActionBarSize();
        mTitleView = (TextView) findViewById(R.id.title);
        mImageView = (ImageView) findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mScrollView.setScrollViewCallbacks(this);
        mFab = findViewById(R.id.fab);
        mOtherFab = (com.github.clans.fab.FloatingActionMenu)findViewById(R.id.mOtherFab);
        mFavoriteHeart = (ImageView)findViewById(R.id.favoriteHeart);
        mComicTranscript = (me.grantland.widget.AutofitTextView) findViewById(R.id.comicTranscript);
        mComicAlt = (me.grantland.widget.AutofitTextView) findViewById(R.id.comicAlt);
        mComicTitle = (me.grantland.widget.AutofitTextView) findViewById(R.id.comicTitle);
        mComicDate = (me.grantland.widget.AutofitTextView) findViewById(R.id.comicDate);
        mFabShare = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.fabShare);
        mFabDownload = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.fabDownload);
        mFabBrowser = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.fabBrowser);
        mFabExplanation = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.fabExplanation);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMenu = new SlidingMenu(this);
        mMenu.setMode(SlidingMenu.LEFT);
        mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mMenu.setShadowDrawable(R.drawable.shadow);
        mMenu.setFadeDegree(0.35f);
        mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mMenu.setBehindWidthRes(R.dimen.navigation_drawer_width);
        mMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mMenu.setMenu(R.layout.menu);
        mMenuListView = (ListView) mMenu.findViewById(R.id.menu_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSwipeRefreshLayout = (com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout) mMenu.findViewById(R.id.activity_main_swipe_refresh_layout);
        mProgress = (com.github.lzyzsd.circleprogress.DonutProgress) mMenu.findViewById(R.id.donut_progress);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void getAllComics(final String currentNum) {
        boolean foundAny = false;
        final Counter amountThereWere = new Counter();
        final Counter amountFound = new Counter();
        try {
            mDb = DBFactory.open(mContext, "comics"); //create or open an existing databse using the default name

            final int highestNumber = Integer.parseInt(currentNum);

            Log.d("D","IN GET ALL COMICS DEBUG WITH HIGHEST NUMBER = " + highestNumber);

            for (int i = 1; i <= highestNumber; i++) {
                final int currentComicNumber = i;
                if (!mDb.exists("comic_" + i)) {
                    foundAny = true;
                    amountThereWere.setCounter(amountThereWere.getCounter() + 1);
                    foundAny = true;
                    String url = ANY_COMIC_URL_FIRST + i + ANY_COMIC_URL_SECOND;
                    aqCurrent.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

                        @Override
                        public void callback(String url, JSONObject json, AjaxStatus status) {
                            amountFound.setCounter(amountFound.getCounter() + 1);

                            if (status.getCode() == 200 && json != null) {
                                Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, STATUS 200 AND JSON != NULL");
                                try {
                                    if (!mDb.isOpen()) {
                                        mDb = DBFactory.open(mContext, "comics"); //create or open an existing databse using the default name
                                    }
                                    mDb.put("comic_" + currentComicNumber, json.toString());
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, URL CALLED = " + url);
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, STATUS = " + status.getCode());
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, JSON = " + json.toString());
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, CURRENT COMIC NUMBER CALLBACK = " + currentComicNumber);
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, HIGHEST COMIC NUMBER CALLBACK = " + highestNumber);
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, PROGRESS  = " + (int) ((float) currentComicNumber / (float) highestNumber * 100));
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, AMOUNT FOUND = " + amountFound.getCounter());
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, AMOUNT THERE WERE = " + amountThereWere.getCounter());
                                } catch (SnappydbException e) {
                                    Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, SNAPPYDB EXCEPTION AND E = " + e.getMessage());
                                }

                            } else {
                                Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, ERROR ERROR STATUS CODE = " + status.getCode());
                                Log.d("D", "IN PROGRESS OF GETTING ALL COMICS, ERROR ERROR URL = " + url);
                            }


                            mProgress.setProgress((int) ((float) amountFound.getCounter() / (float) amountThereWere.getCounter() * 100));
                            if (amountFound.getCounter() == amountThereWere.getCounter()) {
                                mProgress.setVisibility(View.GONE);
                                populateMenu(currentNum);
                                toggleMenu();

                            }


                        }

                    });

                }
            }

            mDb.close();
        } catch (SnappydbException e) {
            mProgress.setVisibility(View.GONE);
            populateMenu(currentNum);
            toggleMenu();

        }

        if (!foundAny) {
            mProgress.setVisibility(View.GONE);
            populateMenu(currentNum);
            toggleMenu();

        }
    }


    public void populateMenu(String numToGoto) {
        final int highestNumber = Integer.parseInt(numToGoto);

        try {
            mDb = DBFactory.open(mContext, "comics"); //create or open an existing databse using the default name

            for(int i=highestNumber; i > 0; i--){
                if(mDb.exists("comic_"+i)){
                    Comic currentComic = new Comic();
                    JSONObject comic = new JSONObject(mDb.get("comic_"+i));
                    Log.d("D","POPULATE MENU DEBUG WITH I = " + i);
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


                    currentComic.setMonth(month);
                    currentComic.setNews(news);
                    currentComic.setNum(num);
                    currentComic.setLink(link);
                    currentComic.setYear(year);
                    currentComic.setSafeTitle(safe_title);
                    currentComic.setTranscript(transcript);
                    currentComic.setAlt(alt);
                    currentComic.setImg(img);
                    currentComic.setTitle(title);
                    currentComic.setDay(day);
                    currentComic.setJsonRepresentation(comic.toString());


                    allComics.add(currentComic);
                }
            }

            for(int i=highestNumber; i >= highestNumber-50; i--){
                if(mDb.exists("comic_"+i)){
                    Comic currentComic = new Comic();
                    JSONObject comic = new JSONObject(mDb.get("comic_"+i));
                    Log.d("D","POPULATE MENU DEBUG WITH I IN CURRENTCOMICS = " + i);
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

                    currentComic.setMonth(month);
                    currentComic.setNews(news);
                    currentComic.setNum(num);
                    currentComic.setLink(link);
                    currentComic.setYear(year);
                    currentComic.setSafeTitle(safe_title);
                    currentComic.setTranscript(transcript);
                    currentComic.setAlt(alt);
                    currentComic.setImg(img);
                    currentComic.setTitle(title);
                    Log.d("D", "POPULATE MENU DEBUG WITH TITLE = " + currentComic.getTitle());
                    currentComic.setDay(day);
                    currentComic.setJsonRepresentation(comic.toString());

                    currentComics.add(currentComic);
                }
            }

            mDb.close();
        } catch (SnappydbException e) {
            Log.d("D","SNAPPY DB EXCEPTION SOMETHING WENT WRONG!!!!!!!!!");
        }catch (Exception e) {
            Log.d("D", "EXCEPTION SOMETHING WENT WRONG!!!!!!!!!");
        }

        Log.d("D","DISPLAYING MENU ALLCOMICS.SIZE AND CURRENTCOMICS.SIZE = " + allComics.size() + " " +currentComics.size() );
        if(allComics.size() > 0 && currentComics.size() > 0){
            final MenuAdapter adapter = new MenuAdapter(this, currentComics);
            mMenuListView.setAdapter(adapter);

            LayoutInflater inflater = getLayoutInflater();
            ViewGroup header = (ViewGroup)inflater.inflate(R.layout.menu_header, mMenuListView, false);
            mMenuListView.addHeaderView(header, null, false);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, FavoritesActivity.class);
                    startActivity(i);

                }
            });
            mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh(SwipyRefreshLayoutDirection direction) {


                    ArrayList<Comic> toAppend = getNextAmountOfComics();

                    currentOffset = currentOffset + 50;

                    currentComics.addAll(toAppend);
                    adapter.notifyDataSetChanged();

                    mSwipeRefreshLayout.setRefreshing(false);


                }
            });
        }



    }

    public ArrayList<Comic> getNextAmountOfComics(){
        ArrayList<Comic> toReturn = new ArrayList<>();

        Log.d("D","GETNEXTAMOUNTOFCOMICS DEBUG WITH ALL COMICS SIZE = " + allComics.size());
        Log.d("D", "GETNEXTAMOUNTOFCOMICS DEBUG WITH ALL currentOffset = " + currentOffset);
        for(int i=currentOffset; i < AMOUNT_TO_OFFSET_BY + currentOffset; i++ ){
            Log.d("D","GETNEXTAMOUNTOFCOMICS DEBUG WITH I = " + i);
            toReturn.add(allComics.get(i));
        }

        return toReturn;


    }

    public void displayComic(final Comic c){

        //mComic will always represent the dispalyed comic
        mComic = c;

        mPrefs.edit().putString("currentComic", c.getJsonRepresentation()).apply();


        aq.id(mImageView).image(c.getImg(), true, true, 0, 0, new BitmapAjaxCallback() {

            @Override
            public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {

                iv.setImageBitmap(bm);

                //save bitmap if user wants to download it
                mCurrentBitmap = bm;

                ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.scrollTo(0, mFlexibleSpaceImageHeight - mActionBarSize);

                        // If you'd like to start from scrollY == 0, don't write like this:
                        mScrollView.scrollTo(0, 0);

                        // The initial scrollY is 0, so it won't invoke onScrollChanged().
                        // To do this, use the following:
                        onScrollChanged(0, false, false);

                        // You can also achieve it with the following codes.
                        // This causes scroll change from 1 to 0.
                        mScrollView.scrollTo(0, 1);
                        mScrollView.scrollTo(0, 0);
                    }
                });


            }

        });

        View scrollViewImage = findViewById(R.id.scrollViewImage);
        scrollViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, FullImageActivity.class);
                i.putExtra("image", c.getImg());
                startActivity(i);
            }
        });
        mTitleView.setText(c.getTitle());
        Log.d("D", "VIEW ANY COMIC DEBUG AND TITLE = " + c.getTitle());
        Log.d("D", "VIEW ANY COMIC DEBUG PUTTING HIGHEST NUM INTO PREFS " + mPrefs.getString("highesNum", "1500"));
        if(c.getTranscript().equals("")){
            mComicTranscript.setVisibility(View.GONE);
        }else{
            mComicTranscript.setText(c.getTranscript());
        }

        if(c.getTitle().equals("")){
            mComicTitle.setVisibility(View.GONE);
        }else{
            mComicTitle.setText(c.getTitle());
        }

        if(c.getAlt().equals("")){
            mComicAlt.setVisibility(View.GONE);
        }else{
            mComicAlt.setText(c.getAlt());
        }

        mComicDate.setText(fmt.format(new Date(c.getYear() + "/" + c.getMonth() + "/" + c.getDay())));

        boolean favorited = checkIfFav(c);

        if(favorited){
            mFavoriteHeart.setColorFilter(Color.RED);
            mFavoriteHeart.setVisibility(View.VISIBLE);
            //mFab.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
        }

    }

    public boolean checkIfFav(Comic c){
        boolean toReturn = false;
        try {
            DB mDb = DBFactory.open(this, "comics"); //create or open an existing databse using the default name
            int highestAmount = Integer.parseInt(mPrefs.getString("highestNum", "1500"));
            for(int i=0; i <= highestAmount; i++){
                if(mDb.exists("favoriteComic_"+i)){
                    Log.d("D","SEARCHING THROUGH FAV DEBUG AND FAV FOUND WITH I = " + i);
                    String favoriteComic = mDb.get("favoriteComic_" + i);
                    if(c.getJsonRepresentation().equals(favoriteComic)){
                        toReturn = true;
                        break;
                    }
                }

            }

            mDb.close();

        } catch (SnappydbException e) {
            Toast.makeText(this, "Something went wrong with favorites ", Toast.LENGTH_SHORT).show();
        }

        return toReturn;
    }

    public void setupFabMenu(){
        mFabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("D", "Download button clicked. downloading url  = " + mComic.getImg());

                aq.download(mComic.getImg(),new File(Environment.getExternalStorageDirectory().toString() + "/xkcd_comics", mComic.getNum() + ".png"),new AjaxCallback<File>(){

                    @Override
                    public void callback(String url, File f ,AjaxStatus status) {


                        Toast.makeText(mContext,"Saved to " + f.getAbsolutePath(),Toast.LENGTH_LONG).show();


                    }

                });
            }
        });

        mFabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = XKCD_URL + mComic.getNum();

                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, "XKCD " + mComic.getNum());
                share.putExtra(Intent.EXTRA_TEXT, url);

                startActivity(Intent.createChooser(share, "Share link!"));
            }
        });

        mFabBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = XKCD_URL + mComic.getNum();

                Log.d("D","Browser button clicked. url = " + url);


                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        mFabExplanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = EXPLAIN_URL + mComic.getNum();

                Log.d("D","Explanation button clicked. url = " + url);


                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }


    public void toggleMenu(){
        //open so the user knows about the drawer
        if(mPrefs.getString("openSlider","no").equals("yes")){
            mPrefs.edit().putString("openSlider","no").apply();
            mMenu.toggle(true);
        }

    }

    public void displayPopup(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Enter search query");
        //alert.setMessage("Message");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if(!input.getText().toString().equals("")){
                    Intent i = new Intent(mContext, SearchResultsActivity.class);
                    i.putExtra("searchQuery",input.getText().toString());
                    startActivity(i);
                }else{
                    Toast.makeText(mContext,"Please enter a search query",Toast.LENGTH_SHORT).show();
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.dismiss();
            }
        });

        alert.show();
    }


    public void displayNumberPopup(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Go to comic number");
        //alert.setMessage("Message");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if(!input.getText().toString().equals("") && isNumber(input.getText().toString())){
                    Comic toDisplay =  numberInDatabase(input.getText().toString());
                    if(toDisplay != null){
                        Intent i = new Intent(mContext,MainActivity.class);
                        i.putExtra("comic",toDisplay);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(mContext,"Comic number " + input.getText().toString() + " not found",Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(mContext,"Please enter a valid number",Toast.LENGTH_SHORT).show();
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public boolean isNumber(String toCheck){
        try{
            Integer.parseInt(toCheck);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public Comic numberInDatabase(String toCheck){
        Comic toReturn = null;

        try {
            DB mDb = DBFactory.open(this, "comics"); //create or open an existing databse using the default name
            Log.d("D","IN numberInDatabase DEBUG AFTER OPENING DB" );

            if(mDb.exists("comic_"+toCheck)){
                toReturn = parseComic(mDb.get("comic_"+toCheck));
            }

            mDb.close();

        } catch (SnappydbException e) {
            Toast.makeText(this, "Something went wrong with search ", Toast.LENGTH_SHORT).show();
            Log.d("D","SEARCH DEBUG EXCEPTION E = " + e.getMessage());
            toReturn = null;
        }

        return toReturn;
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