package com.juanmsl.googlebase.logic;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public abstract class FBAuth {

    private Activity activity;
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authenticationListener;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseUser user;

    public FBAuth(final Activity activity) {
        this.activity = activity;
        authentication = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        authenticationListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(isAnUserSignedIn()) {
                    onSuccess();
                }
            }
        };
    }

    public void start() {
        authentication.addAuthStateListener(authenticationListener);
        if(isAnUserSignedIn()) {
            onSuccess();
        }
    }

    public void stop() {
        if (authenticationListener != null) {
            authentication.removeAuthStateListener(authenticationListener);
        }
    }

    public void signInWithEmailAndPassword(String email, String password) {
        authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            user = authentication.getCurrentUser();
            if (task.isSuccessful()) {
                onSuccess();
            } else {
                Snackbar.make(activity.getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
            }
        });
    }

    public void createUserWithEmailAndPassword(final String email, String password, final String displayName, final Uri image) {
        authentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                user = task.getResult().getUser();
                UserProfileChangeRequest.Builder userBuilder = new UserProfileChangeRequest.Builder();
                userBuilder.setDisplayName(displayName);
                user.updateProfile(userBuilder.build()).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            uploadFile(image, email);
                        } else {
                            Snackbar.make(activity.getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Snackbar.make(activity.getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
            }
        });
    }

    public void uploadFile(Uri uri, String email) {
        StorageReference imagesStorage = storageReference.child("images/" + email);
        imagesStorage.putFile(uri).addOnCompleteListener(activity, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    final Uri imageUri = task.getResult().getDownloadUrl();
                    UserProfileChangeRequest.Builder userBuilder = new UserProfileChangeRequest.Builder();
                    userBuilder.setPhotoUri(imageUri);

                    user.updateProfile(userBuilder.build()).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                FBAuth.this.onSuccess();
                            } else {
                                Snackbar.make(activity.getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Snackbar.make(activity.getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void downloadFile(String email) {
        try {
            final File localFile = File.createTempFile("images", "jpg");
            StorageReference imagesStorage = storageReference.child("images/" + email);
            imagesStorage.getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        onDownloadSuccess(localFile);
                    } else {
                        Snackbar.make(activity.getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        } catch (IOException e) {
            Snackbar.make(activity.getCurrentFocus(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    public FirebaseUser getUser() {
        return user;
    }

    public boolean isAnUserSignedIn() {
        return user != null;
    }

    public void signOut() {
        authentication.signOut();
    }

    public void onSuccess(){}

    public void onDownloadSuccess(File file){}
}
