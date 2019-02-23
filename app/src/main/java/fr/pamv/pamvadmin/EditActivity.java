package fr.pamv.pamvadmin;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.pamv.pamvadmin.entities.ApiError;
import fr.pamv.pamvadmin.entities.Article;
import fr.pamv.pamvadmin.entities.Categories;
import fr.pamv.pamvadmin.network.ApiService;
import fr.pamv.pamvadmin.network.RetrofitBuilder;
import jp.wasabeef.richeditor.RichEditor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class EditActivity extends AppCompatActivity {

    @BindView(R.id.edit_title)
    TextInputLayout title;

    @BindView(R.id.edit_content)
    RichEditor content;

    @BindView(R.id.edit_image)
    ImageView image;

    @BindView(R.id.edit_intro)
    TextInputLayout intro;

    @BindView(R.id.edit_spinner)
    Spinner spinner;

    private Article article;
    private TokenManager tokenManager;
    private ApiService service;
    private Categories categories;
    private boolean imageHasChanged = false;
    private String mCurrentPhotoPath;
    private Uri photoURI;

    private static final String TAG = "EditActivity";
    static final int REQUEST_TAKE_PHOTO = 0;
    static final int REQUEST_PICK_PHOTO = 1;

    static final int PERMISSION_REQUEST_CAMERA = 0;
    static final int PERMISSION_REQUEST_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        article = (Article) intent.getParcelableExtra("article");

        title.getEditText().setText(article.getTitle());

        intro.getEditText().setText(article.getIntro());

        content.setHtml(article.getContent());
        Picasso.get().load(Utils.URL_SITE + article.getImage())
                .fit().centerInside()
                .into(image);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        final Context context = this;

        Call<Categories> categoriesCall = service.categories();
        categoriesCall.enqueue(new Callback<Categories>() {
            @Override
            public void onResponse(Call<Categories> call, Response<Categories> response) {
                if (response.isSuccessful())
                {
                    categories = response.body();
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categories.getCategoriesName());
                    spinner.setAdapter(dataAdapter);
                    String selectedCategory = categories.getCategory(article.getCategory_id());
                    spinner.setSelection(dataAdapter.getPosition(selectedCategory));
                }
            }

            @Override
            public void onFailure(Call<Categories> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.edit_valide)
    public void validate()
    {
        Call<Void> update;
        int category_id = categories.getIdFromCategorie((String) (spinner.getSelectedItem()));

        if (imageHasChanged)
        {
            File imageFile = new File(mCurrentPhotoPath);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), imageFile));

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", title.getEditText().getText().toString())
                    .addFormDataPart("_method", "PUT")
                    .addFormDataPart("content", content.getHtml())
                    .addFormDataPart("category_id", "" + category_id)
                    .addFormDataPart("intro", intro.getEditText().getText().toString())
                    .addPart(imagePart)
                    .build();

            update = service.updateImage(article.getId(),
                    requestBody);
        }
        else
        {
            update = service.update(article.getId(),
                    title.getEditText().getText().toString(),
                    content.getHtml(),
                    intro.getEditText().getText().toString(),
                    category_id);
        }
        update.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful())
                {
                    startActivity(new Intent(EditActivity.this, ShowActivity.class));
                    finish();
                }
                else if(response.code() == 422)
                    handleErrors(response.errorBody());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void handleErrors(ResponseBody response)
    {
        ApiError apiError = Utils.convertErrors(response);

        for (Map.Entry<String, List<String>> error : apiError.getErrors().entrySet())
        {
            if (error.getKey().equals("title"))
                title.setError(error.getValue().get(0));
            if (error.getKey().equals("intro"))
                intro.setError(error.getValue().get(0));
        }
    }

    @OnClick(R.id.action_undo)
    public void undo()
    {
        content.undo();
    }

    @OnClick(R.id.action_redo)
    public void redo()
    {
        content.redo();
    }

    @OnClick(R.id.action_bold)
    public void bold()
    {
        content.setBold();
    }

    @OnClick(R.id.action_italic)
    public void italic()
    {
        content.setItalic();
    }

    @OnClick(R.id.action_underline)
    public void underline()
    {
        content.setUnderline();
    }

    @OnClick(R.id.action_align_left)
    public void left()
    {
        content.setAlignLeft();
    }

    @OnClick(R.id.action_align_center)
    public void center()
    {
        content.setAlignCenter();
    }

    @OnClick(R.id.action_align_right)
    public void right()
    {
        content.setAlignRight();
    }

    @OnClick(R.id.action_insert_bullets)
    public void bullets()
    {
        content.setBullets();
    }

    @OnClick(R.id.action_insert_numbers)
    public void numbers()
    {
        content.setNumbers();
    }

    @OnClick(R.id.edit_image)
    public void image()
    {
        final CharSequence[] items = { getString(R.string.TakePhoto), getString(R.string.PickPhoto), getString(R.string.Cancel) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.AddPhoto));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals(getString(R.string.TakePhoto)))
                {
                    if (haveCameraPermission() && haveStoragePermission())
                        takePicture();
                }
                else if (items[which].equals(getString(R.string.PickPhoto)))
                {
                    if (haveStoragePermission())
                        pickPicture();
                }
                else
                    dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CAMERA:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    image();
            }

            case PERMISSION_REQUEST_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    image();
            }
        }
    }

    private void takePicture()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "image: " + ex.getMessage());
            }
            // Continue only if the File was successfully created

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "fr.pamv.pamvadmin.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void pickPicture()
    {
        Intent pickPicture = new Intent(Intent.ACTION_PICK);
        if (pickPicture.resolveActivity(getPackageManager()) != null)
        {
            pickPicture.setType("image/*");
            startActivityForResult(pickPicture, REQUEST_PICK_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK)
                {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                    mediaScanIntent.setData(photoURI);
                    this.sendBroadcast(mediaScanIntent);
                    setPic();
                }
                else
                {
                    File imageFile = new File(mCurrentPhotoPath);
                    imageFile.delete();
                }
                break;

            case REQUEST_PICK_PHOTO:
                if (resultCode == RESULT_OK)
                {
                    photoURI = data.getData();
                    setPic();
                    mCurrentPhotoPath = getPath(photoURI);
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        Picasso.get().load(photoURI)
                .fit().centerInside()
                .into(image);

        imageHasChanged = true;
    }

    private boolean haveCameraPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
            return false;
        }
        else
            return true;
    }

    private boolean haveStoragePermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
            return false;
        }
        else
            return true;
    }

    //TODO Remove deprecated methode
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
