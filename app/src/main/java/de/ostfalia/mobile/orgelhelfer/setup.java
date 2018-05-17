package de.ostfalia.mobile.orgelhelfer;

import android.app.ListActivity;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class setup extends ListActivity {

    private EditText et_dateipfad;
    private ImageButton btn_dateipfad;
    private List<String> fileList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        et_dateipfad = (findViewById(R.id.dateipfad));
        btn_dateipfad = (findViewById(R.id.ib_dateipfad));

        btn_dateipfad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                listDir(root);
            }
        });



    }

    private void listDir (File f) {
        File[] files = f.listFiles();
        fileList.clear();
        for (File file : files) {
            fileList.add(file.getPath());
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        setListAdapter(directoryList);
    }
}
