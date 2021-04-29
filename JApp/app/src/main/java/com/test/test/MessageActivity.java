package com.test.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MessageActivity extends AppCompatActivity {
    private Client client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        client = new Client("82.161.237.86", 7777, getIntent().getExtras().getString("Name")) {
            public void parse(final String message) {
                if (message == null) {
                    return;
                }
                if (message.equals("ping")) {
                    send("pong");
                } else if (message.equals("/close")) {
                    try {
                        close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = ((TextView)findViewById(R.id.messages));
                            textView.setText(textView.getText() + message + "\n");
                        }
                    });
                }
            }
        };

        client.connect();

        int timer = 0;
        while (!client.connectionEstablished()) {
            if (timer >= 3000) {
                Toast.makeText(getApplicationContext(), "Timed out (3 sec), please try again", Toast.LENGTH_LONG).show();
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client = null;
                finish();
                return;
            }
            timer+=10;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final EditText textField = findViewById(R.id.message);
        Button send = findViewById(R.id.sendmessage);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                client.send(textField.getText().toString());
            }
        });

        new Thread(new Runnable() {
            public void run() {
                while (client.isOpenAndWell()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }
        }).start();
    }

    public void onPause() {
        super.onPause();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}