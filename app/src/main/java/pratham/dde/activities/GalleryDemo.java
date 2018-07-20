package pratham.dde.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.customViews.ChooseImageDialog;

public class GalleryDemo extends AppCompatActivity {
    @BindView(R.id.img)
    ImageView img;

    @BindView(R.id.btn)
    Button btn;

    public static final int PICK_IMAGE_FROM_GALLERY = 1;
    public static final int CAPTURE_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_demo);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn)
    public void change() {
        final ChooseImageDialog chooseImageDialog = new ChooseImageDialog(this);
        chooseImageDialog.btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageDialog.cancel();
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            }
        });

        chooseImageDialog.btn_choose_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageDialog.cancel();
                Intent intent = new Intent();
                intent.setType("image/");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_FROM_GALLERY);
            }
        });

        chooseImageDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("codes", String.valueOf(requestCode) + resultCode);
        try {
            if (requestCode == PICK_IMAGE_FROM_GALLERY) {
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                createDirectoryAndSaveFile(bitmap,"g");

            } else if (requestCode == CAPTURE_IMAGE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                img.setImageBitmap(photo);
                // String selectedImagePath = getPath(photo);
                createDirectoryAndSaveFile(photo, "c");
            }
        } catch (Exception e) {
        }
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/DDEImages");

        if (!direct.exists()) {
            File imagesDirectory = new File("/sdcard/DDEImages/");
            imagesDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/DDEImages/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
