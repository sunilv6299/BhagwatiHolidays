package com.rutvik.bhagwatiholidays;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;

import gcm.RegistrationIntentService;
import lwg.LoginWithGoogle;
import model.User;

public class initial extends LoginWithGoogle {

    public final static String TAG = "BWT " + initial.class.getSimpleName();

    SignInButton btnSignIn;


    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private App app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        app=(App) getApplication();

        app.trackScreenView("Login Screen");

        btnSignIn = (SignInButton) findViewById(R.id.btn_signIn);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    @Override
    protected void loggedIn(GoogleSignInAccount account) {

        App app = (App) getApplication();
        if (account.getPhotoUrl() == null) {
            app.setUser(new User(account.getDisplayName(), account.getEmail(), "https://lh3.googleusercontent.com/-Wlkp-_tMv-Y/AAAAAAAAAAI/AAAAAAAAAAA/NbvcGT31kjM/s144-p-k-rw-no/photo.jpg"));
        } else {
            app.setUser(new User(account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString()));
        }

        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra("name", account.getDisplayName());
        intent.putExtra("email", account.getEmail());
        startService(intent);

        Intent i = new Intent(this, SwipeTabActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    protected void loggedOut() {

    }

    @Override
    protected void revokedAccess() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);


        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }

    }


    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((initial) getActivity()).onDialogDismissed();
        }
    }

/*    @Override
    public void onConnected(Bundle bundle) {

        //User has logged in successfully (do something)

        App app=(App) getApplication();

        app.setUser(myLoginWithGoogle.getProfileInformation());

        Intent i=new Intent(this,SwipeTabActivity.class);

        startActivity(i);


    }

    @Override
    public void onConnectionSuspended(int status) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG,"request code: ("+requestCode+") result code: ("+resultCode+")");

    }*/
}
