package com.juanmsl.googlebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.juanmsl.googlebase.constants.Permissions;
import com.juanmsl.googlebase.logic.FBAuth;
import com.juanmsl.googlebase.logic.FieldValidator;
import com.juanmsl.googlebase.logic.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SignupActivity extends AppCompatActivity {

    private static final int GALLERY_INTENT = 1;
    private static final int CAMERA_INTENT = 2;

    private FBAuth firebaseAuthentication;
    private Intent homeIntent;

    private ImageView photo;
    private ImageButton btnCamera;
    private ImageButton btnGallery;
    private TextInputLayout nameLayout;
    private TextInputLayout surnameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText nameInput;
    private TextInputEditText surnameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button btnSignup;
    private Uri imageUri;

    private Intent galleryIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuthentication = new FBAuth(this) {
            @Override
            public void onSuccess() {
                startActivity(homeIntent);
            }
        };
        homeIntent = new Intent(this, HomeActivity.class);

        photo = (ImageView) findViewById(R.id.signup_photo);
        btnCamera = (ImageButton) findViewById(R.id.btn_camera);
        btnGallery = (ImageButton) findViewById(R.id.btn_gallery);

        nameLayout =  (TextInputLayout) findViewById(R.id.signup_name_layout);
        surnameLayout =  (TextInputLayout) findViewById(R.id.signup_surname_layout);
        emailLayout =  (TextInputLayout) findViewById(R.id.signup_email_layout);
        passwordLayout =  (TextInputLayout) findViewById(R.id.signup_password_layout);

        nameInput = (TextInputEditText) findViewById(R.id.signup_name);
        surnameInput = (TextInputEditText) findViewById(R.id.signup_surname);
        emailInput = (TextInputEditText) findViewById(R.id.signup_email);
        passwordInput = (TextInputEditText) findViewById(R.id.signup_password);

        btnSignup = (Button) findViewById(R.id.btn_signup);
        imageUri = null;

        galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupAction();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraAction();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryAction();
            }
        });
    }

    private void galleryAction() {
        if(Permissions.askPermission(this, Permissions.READ_EXTERNAL_STORAGE, "Debes conceder los permisos para poder acceder a la galeria")) {
            startActivityForResult(galleryIntent, GALLERY_INTENT);
        }
    }

    private void cameraAction() {
        if(Permissions.askPermission(this, Permissions.CAMERA, "Debes conceder los permisos para poder acceder a la camara")) {
            if(Permissions.askPermission(this, Permissions.WRITE_EXTERNAL_STORAGE, "Dejanos guardar tus fotos")) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;
                try {
                    photoFile = Utils.createImageFile(this);
                } catch (IOException ex) { }

                if(photoFile != null) {
                    imageUri = FileProvider.getUriForFile(this, "com.juanmsl.googlebase.fileprovider", photoFile);
                    Log.i("Image", imageUri.toString());
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(cameraIntent, CAMERA_INTENT);
                }
            }
        }
    }

    protected void signupAction() {
        boolean validFields = true;
        String name = nameInput.getText().toString();
        String surname = surnameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        nameLayout.setErrorEnabled(false);
        surnameLayout.setErrorEnabled(false);
        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);

        if(!FieldValidator.validateText(name)) {
            nameLayout.setError("Ingrese un nombre");
            nameLayout.setErrorEnabled(true);
            validFields = false;
        }

        if(!FieldValidator.validateText(surname)) {
            surnameLayout.setError("Ingrese un apellido");
            surnameLayout.setErrorEnabled(true);
            validFields = false;
        }

        if(!FieldValidator.validateEmail(email)) {
            emailLayout.setError("Ingrese un correo valido");
            emailLayout.setErrorEnabled(true);
            validFields = false;
        }

        if(!FieldValidator.validatePassword(password)) {
            passwordLayout.setError("La contraseña debe ser de minimo 6 caracteres y maximo 20");
            passwordLayout.setErrorEnabled(true);
            validFields = false;
        }

        if(validFields) {
            firebaseAuthentication.createUserWithEmailAndPassword(email, password, name + " " + surname, imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(!Permissions.permissionGranted(requestCode, permissions, grantResults)) {
            return;
        }
        switch (requestCode) {
            case Permissions.CAMERA:
                cameraAction();
                break;
            case Permissions.READ_EXTERNAL_STORAGE:
                galleryAction();
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case GALLERY_INTENT:
                this.imageUri = data.getData();
                Bitmap selectedImage = Utils.getImageFormUri(this, imageUri);
                photo.setImageBitmap(Utils.cropImage(selectedImage));
                break;
            case CAMERA_INTENT:
                try {
                    Bitmap image = Utils.getImageFormUri(this, imageUri);
                    Bitmap imageBitmap = Utils.cropImage(image);
                    photo.setImageBitmap(imageBitmap);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
