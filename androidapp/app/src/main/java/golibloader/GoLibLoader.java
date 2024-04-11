package golibloader;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GoLibLoader {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static void decode(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            byte[] bytes = hexStringToByteArray(line);
            out.write(bytes);
        }
    }

    static public void load(Context context) {
        AssetManager assetManager = context.getAssets();
        String abi = Build.SUPPORTED_ABIS[0];
        String jniAbiName = "armeabi-v7a";
        if (abi.contains("arm64"))  jniAbiName = "arm64-v8a";
        else if (abi.contains("x86_64"))  jniAbiName = "x86_64";
        else if (abi.contains("x86"))  jniAbiName = "x86";

        try {
            String assetName ="golib/libgojni-" + jniAbiName + ".txt";
            InputStream in = assetManager.open(assetName);
            File outFile = new File(context.getFilesDir(), "libgojni.bin");
            OutputStream out = new FileOutputStream(outFile);
            Log.i("GoLibLoader", "Decoding " + assetName + " to " + outFile.getAbsolutePath());
            decode(in, out);
            System.load(outFile.getAbsolutePath());
            if (!outFile.delete()) {
                Log.i("GoLibLoader", "Failed to delete " + outFile.getAbsolutePath());
            }
        } catch (IOException e) {}
    }
}
