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

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    // Used to load the 'myclient' library on application startup.
    static {
        System.loadLibrary("myclient");
        System.loadLibrary("mbedcrypto");
        LogUsingJNI("All libraries successfully loaded");
    }

    private ActivityMainBinding binding;
    private String pin;
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
//                            String pin;
                            if (data == null) {
                                LogErrorUsingJNI("Failed to retrieve data from activity result");
                                return;
                            } else {

                                // обработка результата
                                pin = data.getStringExtra("pin");
                            }

                            LogUsingJNI("Pin: " + pin);
//                            Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                            synchronized (MainActivity.this) {
                                MainActivity.this.notifyAll();
                            }
                        } else {
                            LogErrorUsingJNI("Activity failed with code: " + result.getResultCode());
                        }
                    }
                });
    }

    public void onButtonClick(View v)
    {
        LogUsingJNI("Button clicked");
        byte[] trd = stringToHex("9F0206000000000100");
        boolean ok = transaction(trd);

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

    @Override
    public String enterPin(int ptc, String amount) {
        pin = "";
        Intent it = new Intent(MainActivity.this, Pinpad.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                LogErrorUsingJNI("Error in enterPin: " + ex);
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(()-> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
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

    public native boolean transaction(byte[] trd);
}