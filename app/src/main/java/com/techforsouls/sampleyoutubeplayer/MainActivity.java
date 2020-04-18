package com.techforsouls.sampleyoutubeplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.techforsouls.myyoutubelibrary.acitivity.YoutubeActivity;
import com.techforsouls.sampleyoutubeplayer.helper.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button singleYoutubePlayerButton = findViewById(R.id.singleYoutubePlayer);

        singleYoutubePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoId = "3AtDnEC4zak";
                String url = "http://demos.webmproject.org/exoplayer/glass.mp4";
                Intent intent = new Intent(MainActivity.this, YoutubeActivity.class);
                intent.putExtra("apiKey", Constants.API_KEY);
                // intent.putExtra("videoId", "3AtDnEC4zak");
                intent.putExtra("videoId", videoId);
                intent.putExtra("webUrl", url);
                startActivity(intent);
            }
        });

    }
}
