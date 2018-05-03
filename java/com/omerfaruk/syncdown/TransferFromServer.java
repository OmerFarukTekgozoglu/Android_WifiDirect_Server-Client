package com.omerfaruk.syncdown;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
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
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class TransferFromServer extends Activity {

    private String root;
    private String currentPath;

    private ArrayList<String> targets;
    private ArrayList<String> paths;

    private File targetFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_from_server);
       // getActionBar().setDisplayHomeAsUpEnabled(true);

        root = String.valueOf(Client.getContext().getFilesDir());         //Server dosya adresi - şimdilik tabletin files klasörü!!!
        currentPath = root;

        targets = null;
        paths = null;

        targetFile = null;

        showDir(currentPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_transfer_from_server, menu);
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

    public void selectDirectory(View view){
        File f = new File(currentPath);
        targetFile = f;

        returnTarget();
    }

    public void setCurrentPathText(String message){
        TextView fileTransferStatusText = (TextView) findViewById(R.id.current_path);
        fileTransferStatusText.setText(message);
    }

    private void showDir(String targetDirectory){
        setCurrentPathText("Current directory: " + currentPath);

        targets = new ArrayList<String>();
        paths = new ArrayList<String>();

        File f = new File(targetDirectory);
        File[] directoryContents = f.listFiles();

        if (!targetDirectory.equals(root)){
            targets.add(root);
            paths.add(root);
            targets.add("../");
            paths.add(f.getParent());
        }

        for(File target : directoryContents){
            paths.add(target.getPath());

            if (target.isDirectory()){
                targets.add(target.getName()+ "/");
            }else {
                targets.add(target.getName());
            }
        }

        ListView fileBrowserListView = (ListView) findViewById(R.id.file_browser_listview);

        ArrayAdapter<String> directoryData = new ArrayAdapter<String>(TransferFromServer.this, android.R.layout.simple_list_item_1, targets);
        fileBrowserListView.setAdapter(directoryData);


        fileBrowserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
