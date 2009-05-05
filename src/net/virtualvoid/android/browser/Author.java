package net.virtualvoid.android.browser;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class Author implements Formattable{
    Home home;
    public Author(Home home) {
        this.home = home;
    }
    private Spanned colorize(CharSequence seq){
        Spannable sp = Spannable.Factory.getInstance().newSpannable(seq);
        int len = seq.length();
        for (int i=0;i<len;i++)
            sp.setSpan(new ForegroundColorSpan(Color.HSVToColor(new float[]{i*360/len,.8f,.8f})), i, i+1,0);
        return sp;
    }
    public Spanned getAuthor(){
        return colorize("Johannes Rudolph");
    }
    public String getSourceURL(){
        return "<a href=\"http://github.com/jrudolph/object-browser\">http://github.com/jrudolph/object-browser</a>";
    }
    public Drawable getVisualAppearance(){
        return home.getApplication().getResources().getDrawable(R.drawable.yoodoo);
    }
    public Drawable getHomeTown(){
        return home.getApplication().getResources().getDrawable(R.drawable.atnight);
    }
    @Override
    public CharSequence formatted() {
        return getAuthor();
    }
}
