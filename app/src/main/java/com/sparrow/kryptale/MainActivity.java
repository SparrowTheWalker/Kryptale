package com.sparrow.kryptale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message.RecipientType;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText message;
    EditText recipientEmail;
    EditText recipientPhoneNumber;
    String inputPhoneNumber;
    String sentOtp = "";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.InputMessage);
        recipientEmail = findViewById(R.id.InputEmail);
        recipientPhoneNumber = findViewById(R.id.InputPhoneNumber);

        textView = (TextView) findViewById(R.id.decrypt_text);
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, decrypt.class);
                startActivity(intent);
            }
        });
    }


    public char[] generateOtp(int len)
        {
            // Length of your otp as I have chosen
            // here to be 8
            int length = 10;


            String Capital_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String Small_chars = "abcdefghijklmnopqrstuvwxyz";
            String numbers = "0123456789";
            String symbols = "!@#$%^&*_=+-/.?<>)";

            String values = Capital_chars + Small_chars +
                    numbers + symbols;

            // Using random method
            Random rndm_method = new Random();

            char[] sentOtp = new char[len];

            for (int i = 0; i < len; i++) {
                // Use of charAt() method : to get character value
                // Use of nextInt() as it is scanning the value as int
                sentOtp[i] =
                        values.charAt(rndm_method.nextInt(values.length()));

            }
            return sentOtp;

        }


    public void sendSMSMessage(String phoneNumber){

        sentOtp = String.valueOf(generateOtp(10));

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        //Getting intent and PendingIntent instance
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(getApplicationContext(), 0, intent,0);
        //Get the SmsManager instance and call the sendTextMessage method to send message
        SmsManager sms=SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, sentOtp, pi,null);
        Toast.makeText(getApplicationContext(), "Message Sent successfully!", Toast.LENGTH_LONG).show();




    }
    public byte[] encrypt(String message, String phoneNumber) throws Exception {

       sendSMSMessage(phoneNumber);

        //Use OTP as private key to encrypt
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(sentOtp
                .getBytes("utf-8"));
        Log.d("DecryptError", "otp:" + sentOtp);
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        final byte[] plainTextBytes = message.getBytes("utf-8");
        // final String encodedCipherText = new sun.misc.BASE64Encoder()
        // .encode(cipherText);

        return cipher.doFinal(plainTextBytes);
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


    public void buttonSend(View view){
        try {
            String stringSenderEmail = "shaistaparpia@gmail.com";
            String stringPasswordSenderEmail = "zrbpionardaomjos";

            String inputMessage = message.getText().toString();
            inputPhoneNumber = recipientPhoneNumber.getText().toString();
            String inputEmail = recipientEmail.getText().toString();

            String stringHost = "smtp.gmail.com";
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port","465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            MimeMessage miMessage = new MimeMessage(session);

            miMessage.addRecipient(RecipientType.TO, new InternetAddress(inputEmail));
            byte[] encryptedMessage = encrypt(inputMessage, inputPhoneNumber);

            String encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage);

            Log.d("DecryptError", "" + encryptedMessage + " ; " + encryptedMessageString);
            Log.d("Decry16", new String(encryptedMessage, StandardCharsets.UTF_16));
            Log.d("Decry16", new String(encryptedMessage, StandardCharsets.ISO_8859_1));

            Log.d("Decry16", "Compare");
//            Log.d("Decry16", String.valueOf(Arrays.equals(encryptedMessage, encryptedMessageString.getBytes(StandardCharsets.UTF_16))));
            byte[] newBytes = Base64.getDecoder().decode(encryptedMessageString);
            Log.d("Decry16", String.valueOf(Arrays.equals(encryptedMessage, newBytes)));

            String message = null;
            try {
                message = decryptText(encryptedMessage, sentOtp);
            } catch (Exception e) {
                Log.d("DecryptError", e.getMessage());
                //Toast.makeText(this, "Could not decrypt message!", Toast.LENGTH_SHORT).show();
            }

//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            Log.d("DecryptError", encryptedMessageString);
            miMessage.setSubject("Encrypted Text");
            miMessage.setText(encryptedMessageString);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(miMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(inputPhoneNumber, null, sentOtp, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }
    }



