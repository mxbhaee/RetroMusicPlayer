package code.name.monkey.retromusic.activities;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.activities.base.AbsBaseActivity;
import code.name.monkey.retromusic.util.PreferenceUtil;
import com.google.android.material.appbar.AppBarLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class WhatsNewActivity extends AbsBaseActivity {

    AppBarLayout appBarLayout;

    Toolbar toolbar;

    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setDrawUnderStatusBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_new);
        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        webView = findViewById(R.id.webView);
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);

        toolbar.setBackgroundColor(ATHUtil.INSTANCE.resolveColor(this, R.attr.colorSurface));
        //setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        ToolbarContentTintHelper.colorBackButton(toolbar);
        NestedScrollView nestedScrollView = findViewById(R.id.container);

        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = getAssets().open("retro-changelog.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();

            // Inject color values for WebView body background and links
            final boolean isDark = ATHUtil.INSTANCE.isWindowBackgroundDark(this);
            final String backgroundColor = colorToCSS(ATHUtil.INSTANCE.resolveColor(this, R.attr.colorSurface,
                    Color.parseColor(isDark ? "#424242" : "#ffffff")));
            final String contentColor = colorToCSS(Color.parseColor(isDark ? "#ffffff" : "#000000"));
            final String changeLog = buf.toString()
                    .replace("{style-placeholder}",
                            String.format("body { background-color: %s; color: %s; }", backgroundColor, contentColor))
                    .replace("{link-color}", colorToCSS(ThemeStore.Companion.accentColor(this)))
                    .replace("{link-color-active}",
                            colorToCSS(ColorUtil.INSTANCE.lightenColor(ThemeStore.Companion.accentColor(this))));

            webView.loadData(changeLog, "text/html", "UTF-8");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
        }
        setChangelogRead(this);
    }

    private static String colorToCSS(int color) {
        return String.format("rgb(%d, %d, %d)", Color.red(color), Color.green(color),
                Color.blue(color)); // on API 29, WebView doesn't load with hex colors
    }

    private static void setChangelogRead(@NonNull Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            PreferenceUtil.getInstance(context).setLastChangeLogVersion(currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}