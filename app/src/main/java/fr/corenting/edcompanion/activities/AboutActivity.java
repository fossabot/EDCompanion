package fr.corenting.edcompanion.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import fr.corenting.edcompanion.R;
import fr.corenting.edcompanion.utils.ThemeUtils;


public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //Action bar setup
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView versionTextView = findViewById(R.id.versionTextView);
        TextView libsContentTextView = findViewById(R.id.librariesContentTextView);
        TextView iconsContentTextView = findViewById(R.id.iconsContentTextView);
        TextView contactTextView = findViewById(R.id.contactTextView);

        PackageManager manager = getPackageManager();
        PackageInfo info;
        String version = " version ";
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
            version += info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionTextView.setVisibility(View.GONE);
        }
        versionTextView.setText(version);
        setHtmlTextView(libsContentTextView, getString(R.string.about_libs));
        setHtmlTextView(iconsContentTextView, getString(R.string.about_icons));
        setHtmlTextView(contactTextView, getString(R.string.about_contact_me));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void setHtmlTextView(TextView textView, String text) {
        textView.setText(Html.fromHtml(text));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
