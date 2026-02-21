package com.example.myclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.myclient.databinding.ActivityMainBinding;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'myclient' library on application startup.
    static {
        System.loadLibrary("myclient");
        System.loadLibrary("mbedcrypto");
        LogUsingJNI("All libraries successfully loaded");
    }

    private ActivityMainBinding binding;

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
    }

    /**
     * A native method that is implemented by the 'myclient' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native void LogUsingJNI(String str);

    public static native int initRng();
    public static native byte[] randomBytes(int no);

    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);
}