package com.omerfaruk.syncdown;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class TransferFromClient extends AppCompatActivity {

    private final String TAG = "TransferFromClient";

    private String root;
    private String currentPath;

    private ArrayList<String> targets;
    private ArrayList<String> paths;

    private File targetFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_from_client);

        root = String.valueOf(Client.getContext().getFilesDir() + File.separator + "/80GundeDevriAlem.txt"); //Contentlerin listelendği files klasörü yolunu girdik database yolu da olabilir
        currentPath = root;

        targets = null;
        targetFile = null;
        paths = null;

        showDir(currentPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_transfer_from_client,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectDirectoryClient(View view){
        File f = new File(currentPath);
        targetFile = f;
        returnTarget();
    }

    private void showDir(String targetDirectory){

        targets = new ArrayList<String>();
        paths = new ArrayList<String>();

        File f = new File(targetDirectory);
        File[] directoryContents = f.listFiles();

        if (directoryContents != null){
            targets.add(root);
            paths.add(root);
            targets.add("../");
            paths.add(f.getParent());
        }

        for(File target : directoryContents) {
            paths.add(target.getPath());
            if (target.isDirectory()){
                targets.add(target.getName()+ "/");
            }else {
                targets.add(target.getName());
            }
        }

        ListView file_browser_listview = (ListView) findViewById(R.id.file_browser_listview_client);
        ArrayAdapter<String> directoryData = new ArrayAdapter<String>(TransferFromClient.this, android.R.layout.simple_list_item_1, targets);
        file_browser_listview.setAdapter(directoryData);

        file_browser_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                File f = new File(paths.get(pos));

                if (f.isFile()){
                    targetFile = f;
                    returnTarget();
                }else {
                    if (f.canRead()){
                        currentPath = paths.get(pos);
                        showDir(paths.get(pos));
                    }
                }
            }
        });
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("WiFi Direct file Transfer");
    }

    public void returnTarget(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("file",targetFile);
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}
