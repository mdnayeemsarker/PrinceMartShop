package com.princemartbd.shop.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.princemartbd.shop.R;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;

public class SplashActivity extends Activity {
    Session session;
    Activity activity;
    final int SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = SplashActivity.this;
        session = new Session(activity);
        session.setBoolean("update_skip", false);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));

        setContentView(R.layout.activity_splash);

//        createShortcut(activity);

        Uri data = this.getIntent().getData();
        if (data == null) {
            if (!session.getBoolean("is_first_time")) {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            } else {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }
        } else if (data.isHierarchical()) {
            switch (data.getPath().split("/")[data.getPath().split("/").length - 2]) {
                case "product": // Handle the item detail deep link
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constant.ID, data.getPath().split("/")[2]);
                    intent.putExtra(Constant.FROM, "share");
                    intent.putExtra(Constant.VARIANT_POSITION, 0);
                    startActivity(intent);
                    finish();
                    break;
                case "refer": // Handle the refer deep link
                    if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                        Constant.FRIEND_CODE_VALUE = data.getPath().split("/")[2];
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", Constant.FRIEND_CODE_VALUE);
                        assert clipboard != null;
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(SplashActivity.this, R.string.refer_code_copied, Toast.LENGTH_LONG).show();
                        Intent referIntent = new Intent(this, LoginActivity.class);
                        referIntent.putExtra(Constant.FROM, "refer");
                        startActivity(referIntent);
                        finish();
                    } else {
                        new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
                        Toast.makeText(activity, activity.getString(R.string.msg_refer), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }
        }
    }
//    @TargetApi(25)
//    private void createShortcut(Activity activity) {
//        ShortcutManager sM = getSystemService(ShortcutManager.class);
//
//        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
//        intent1.setAction(Intent.ACTION_VIEW);
//
//        PersistableBundle persistableBundle = new PersistableBundle();
//        persistableBundle.putString("target", "cart");
//        ShortcutInfo shortcut1 = new ShortcutInfo.Builder(this, getString(R.string.cart))
//                .setIntent(intent1)
//                .setShortLabel(getString(R.string.cart))
//                .setLongLabel(getString(R.string.cart))
//                .setShortLabel(getString(R.string.cart))
//                .setDisabledMessage(getString(R.string.cart))
//                .setIcon(Icon.createWithResource(activity, R.drawable.ic_cart))
//                .setExtras(persistableBundle)
//                .build();
//
//        ShortcutInfo shortcut2 = new ShortcutInfo.Builder(this, getString(R.string.app_name))
//                .setIntent(intent1)
//                .setShortLabel(getString(R.string.app_name))
//                .setLongLabel(getString(R.string.app_name))
//                .setShortLabel(getString(R.string.app_name))
//                .setDisabledMessage(getString(R.string.app_name))
//                .setIcon(Icon.createWithResource(activity, R.drawable.ic_cart))
//                .build();
//
//        sM.setDynamicShortcuts(Arrays.asList(shortcut1, shortcut1, shortcut2));
//    }
}
