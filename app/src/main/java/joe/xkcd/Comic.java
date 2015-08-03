package joe.xkcd;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joe on 7/15/2015.
 */
public class Comic implements Parcelable {
    String month;
    String num;
    String link;
    String year;
    String news;
    String alt;
    String safeTitle;
    String transcript;
    String img;
    String title;
    String day;
    String jsonRepresentation;

    private Comic (Parcel in){
        month = in.readString();
        num = in.readString();
        link = in.readString();
        year = in.readString();
        news = in.readString();
        alt = in.readString();
        safeTitle = in.readString();
        transcript = in.readString();
        img = in.readString();
        title = in.readString();
        day = in.readString();
        jsonRepresentation = in.readString();
    }

    public Comic(){

    }

    public static final Parcelable.Creator<Comic> CREATOR = new Parcelable.Creator<Comic>() {
        public Comic createFromParcel(Parcel in) {
            return new Comic(in);
        }

        public Comic[] newArray(int size) {
            return new Comic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(month);
        dest.writeString(num);
        dest.writeString(link);
        dest.writeString(year);
        dest.writeString(news);
        dest.writeString(alt);
        dest.writeString(safeTitle);
        dest.writeString(transcript);
        dest.writeString(img);
        dest.writeString(title);
        dest.writeString(day);
        dest.writeString(jsonRepresentation);


    }

    public String getJsonRepresentation() {
        return jsonRepresentation;
    }

    public void setJsonRepresentation(String jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }

    public String getSafeTitle() {
        return safeTitle;
    }

    public void setSafeTitle(String safeTitle) {
        this.safeTitle = safeTitle;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
