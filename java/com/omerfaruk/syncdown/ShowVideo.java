package com.omerfaruk.syncdown;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class ShowVideo extends Activity {

    VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        video = (VideoView) findViewById(R.id.video_view);
        String adres = (String) getIntent().getExtras().get("url");
        Uri adress = Uri.parse(adres);       //getApplicationContext().getFilesDir() + File.separator + "Indirilen_Dosya_1522759620966"
        video.setVideoURI(adress);
        MediaController controller = new MediaController(this);
        video.setMediaController(controller);
        video.requestFocus();
        video.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
/*
/data/data/com.omerfaruk.syncdown/files/Indirilen_Dosya_1522759620966
 */