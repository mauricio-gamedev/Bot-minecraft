package io.github.astromg01.mccompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypted on-device conversation history. */
public final class CompanionMemoryStore {
    private static final String KEY_ALIAS = "mc_companion_memory_aes";
    private static final String PREFS = "companion_memory";
    private static final String DATA = "ciphertext";
    private static final String IV = "iv";
    private static final int MAX_MESSAGES = 16;

    private CompanionMemoryStore() { }

    public static synchronized void save(Context context, List<OpenRouterClient.ChatMessage> messages) throws Exception {
        JSONArray array = new JSONArray();
        int start = Math.max(0, messages.size() - MAX_MESSAGES);
        for (int i = start; i < messages.size(); i++) {
            OpenRouterClient.ChatMessage message = messages.get(i);
            array.put(new JSONObject().put("role", message.role).put("content", message.content));
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(array.toString().getBytes(StandardCharsets.UTF_8));
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(DATA, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)).apply();
    }

    public static synchronized List<OpenRouterClient.ChatMessage> load(Context context) throws Exception {
        List<OpenRouterClient.ChatMessage> result = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String data = prefs.getString(DATA, null);
        String encodedIv = prefs.getString(IV, null);
        if (data == null || encodedIv == null) return result;
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (!store.containsAlias(KEY_ALIAS)) throw new IllegalStateException("A chave segura da memória não está disponível.");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(),
                new GCMParameterSpec(128, Base64.decode(encodedIv, Base64.NO_WRAP)));
        byte[] clear = cipher.doFinal(Base64.decode(data, Base64.NO_WRAP));
        JSONArray array = new JSONArray(new String(clear, StandardCharsets.UTF_8));
        int start = Math.max(0, array.length() - MAX_MESSAGES);
        for (int i = start; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            String role = item.optString("role");
            String text = item.optString("content");
            if (("user".equals(role) || "assistant".equals(role)) && !text.isEmpty()) {
                result.add(new OpenRouterClient.ChatMessage(role, text));
            }
        }
        return result;
    }

    public static synchronized void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
        try {
            KeyStore store = KeyStore.getInstance("AndroidKeyStore");
            store.load(null);
            if (store.containsAlias(KEY_ALIAS)) store.deleteEntry(KEY_ALIAS);
        } catch (Exception ignored) { }
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) store.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build());
        return generator.generateKey();
    }
}
