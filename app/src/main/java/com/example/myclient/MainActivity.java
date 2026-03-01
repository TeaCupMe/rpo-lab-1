package com.example.myclient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myclient.databinding.ActivityMainBinding;

import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'myclient' library on application startup.
    static {
        System.loadLibrary("myclient");
        System.loadLibrary("mbedcrypto");
        LogUsingJNI("All libraries successfully loaded");
    }

    private ActivityMainBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int res = initRng();
        byte[] key = randomBytes(16);
        byte[] data = {10, 12, 32, 14};
        LogUsingJNI("Data: " + Arrays.toString(data));
        LogUsingJNI("Generated key: " + Arrays.toString(key));

        byte[] encrypted = encrypt(key, data);
        LogUsingJNI("Data encrypted with 3DES: " + Arrays.toString(encrypted));
        byte[] decrypted = decrypt(key, encrypted);
        LogUsingJNI("Data decrypted: " + Arrays.toString(decrypted));

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());

        activityResultLauncher  = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback() {
                    @Override
                    public void onActivityResult(Object o) {
                        ActivityResult result = (ActivityResult) o;
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            LogUsingJNI("Activity ended with result code RESULT_OK");
                            Intent data = result.getData();
                            String pin;
                            if (data == null) {
                                LogErrorUsingJNI("Failed to retrieve data from activity result");
                                return;
                            } else {

                                // обработка результата
                                pin = data.getStringExtra("pin");
                            }

                            LogUsingJNI("Pin: " + pin);
                            Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                        } else {
                            LogErrorUsingJNI("Activity failed with code: " + result.getResultCode());
                        }
                    }
                });
    }

    public void onButtonClick(View v)
    {
        LogUsingJNI("Button clicked");
//        byte[] key = stringToHex("0123456789ABCDEF0123456789ABCDE0");
//        byte[] enc = encrypt(key, stringToHex("000000000000000102"));
//        byte[] dec = decrypt(key, enc);
//        String s = new String(Hex.encodeHex(dec)).toUpperCase();
//        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        Intent it = new Intent(this, Pinpad.class);
//        startActivity(it);
        LogUsingJNI("Launching Pinpad activity");
        activityResultLauncher.launch(it);
    }
    public static byte[] stringToHex(String s)
    {
        byte[] hex;
        try
        {
            hex = Hex.decodeHex(s.toCharArray());
        }
        catch (DecoderException ex)
        {
            hex = null;
        }
        return hex;
    }
    /**
     * A native method that is implemented by the 'myclient' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native void LogUsingJNI(String str);
    public static native void LogErrorUsingJNI(String str);
    public static native int initRng();
    public static native byte[] randomBytes(int no);

    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);
}