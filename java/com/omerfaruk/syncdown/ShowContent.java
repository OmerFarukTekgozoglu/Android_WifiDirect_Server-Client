package com.omerfaruk.syncdown;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/*
    Client'in kendisine ait contentlerin .txt dosyalarini sakladigi dizindeki icerigi gostermektedir!
 */

public class ShowContent extends AppCompatActivity {

    ListView lvListe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_content);

        lvListe = (ListView) findViewById(R.id.lvListe);

        ArrayList<String> liste2 = getIntent().getStringArrayListExtra("liste");
        ListAdapter listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,liste2);
        lvListe.setAdapter(listAdapter);
    }
}
