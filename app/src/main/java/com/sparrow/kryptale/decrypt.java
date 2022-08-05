package com.sparrow.kryptale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class decrypt extends AppCompatActivity {

    TextView textView;
    EditText message;
    EditText otp;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decryption);

        message = findViewById(R.id.InputDecrMessage);
        otp = findViewById(R.id.InputOtp);

        textView = (TextView) findViewById(R.id.encrypt_text);
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(decrypt.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void decryptMessage(View view) {
        byte[] encryptedMessage = Base64.getDecoder().decode(message.getText().toString());
//        Log.d("DecryptError", "" + encryptedMessage);
        Log.d("DecryptError", message.getText().toString());
        Log.d("DecryptError", otp.getText().toString());
        String inputOtp = otp.getText().toString();
        //Validation - check if message and otp is set

        String message = null;
        try {
            message = decryptText(encryptedMessage, inputOtp);
        } catch (Exception e) {
            Log.d("DecryptError", e.getMessage());
            button = findViewById(R.id.decrypt);
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup)findViewById(R.id.toast_layout));

            Toast.makeText(this,
                    "Could not decrypt message!",
                    Toast.LENGTH_SHORT
//                    Gravity.CENTER_VERTICAL,
//                    0,
//                    0,
//                    layout,
                    ).show();
//            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.setView(layout);


        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public String decryptText(byte[] message, String otp) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(otp
                .getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        Log.d("DecryptError", keyBytes.toString());

        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }
        Log.d("DecryptError", keyBytes.toString());

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        decipher.init(Cipher.DECRYPT_MODE, key, iv);

        // final byte[] encData = new
        // sun.misc.BASE64Decoder().decodeBuffer(message);
        final byte[] plainText = decipher.doFinal(message);

        return new String(plainText, "utf-8");
    }

   }
