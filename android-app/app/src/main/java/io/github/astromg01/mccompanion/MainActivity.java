package io.github.astromg01.mccompanion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_NOTIFICATIONS = 2002;
    private EditText keyInput;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        requestNotificationPermissionIfNeeded();
        refreshStatus();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(20));
        root.setBackgroundColor(Color.rgb(11, 16, 21));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);

        content.addView(text("MC COMPANION AI", 25, Color.WHITE, true));
        gap(content, 5);
        content.addView(text("Seu parceiro de conversa enquanto você joga.", 14, 0xFFB8C5CC, false));
        gap(content, 25);
        content.addView(text("CONEXÃO OPENROUTER", 13, 0xFF39D6C3, true));
        gap(content, 8);
        content.addView(text("Digite uma chave NOVA diretamente aqui. Ela será cifrada no Android Keystore; não envie a chave pelo chat.", 13, 0xFFD0D8DD, false));

        keyInput = new EditText(this);
        keyInput.setSingleLine(true);
        keyInput.setHint("sk-or-v1-…");
        keyInput.setHintTextColor(0xFF7E8B92);
        keyInput.setTextColor(Color.WHITE);
        keyInput.setTextSize(14);
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        keyInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        keyInput.setBackground(roundRect(0xFF18232B, 12));
        LinearLayout.LayoutParams keyParams = new LinearLayout.LayoutParams(-1, dp(52));
        keyParams.topMargin = dp(12);
        content.addView(keyInput, keyParams);

        Button save = button("Salvar chave com segurança");
        save.setOnClickListener(v -> saveKey());
        gap(content, 10);
        content.addView(save, new LinearLayout.LayoutParams(-1, dp(48)));

        gap(content, 14);
        content.addView(text("Modelo: openrouter/free", 14, 0xFF39D6C3, true));
        gap(content, 5);
        content.addView(text("O roteador escolhe um modelo gratuito disponível. Limites e disponibilidade dependem dos provedores.", 12, 0xFFB8C5CC, false));

        gap(content, 23);
        status = text("Status: verificando…", 13, 0xFFD0D8DD, false);
        content.addView(status);

        Button permission = button("Permitir janela flutuante");
        permission.setOnClickListener(v -> requestOverlayPermission());
        gap(content, 12);
        content.addView(permission, new LinearLayout.LayoutParams(-1, dp(48)));

        Button start = button("Iniciar Companion sobre o Minecraft");
        start.setOnClickListener(v -> startOverlay());
        gap(content, 8);
        content.addView(start, new LinearLayout.LayoutParams(-1, dp(52)));

        Button stop = button("Parar janela flutuante");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, CompanionOverlayService.class));
            Toast.makeText(this, "Companion encerrado.", Toast.LENGTH_SHORT).show();
        });
        gap(content, 8);
        content.addView(stop, new LinearLayout.LayoutParams(-1, dp(44)));

        Button delete = button("Apagar chave salva");
        delete.setOnClickListener(v -> {
            SecureKeyStore.clear(this);
            keyInput.setText("");
            refreshStatus();
            Toast.makeText(this, "Chave local apagada.", Toast.LENGTH_SHORT).show();
        });
        gap(content, 8);
        content.addView(delete, new LinearLayout.LayoutParams(-1, dp(44)));

        gap(content, 23);
        content.addView(text(
                "IMPORTANTE\nO app conversa com você em uma sobreposição. Ele não lê o mundo do Minecraft nem controla o NPC. O serviço em primeiro plano ajuda a manter o próprio app auxiliar ativo, mas não impede o Android de descarregar o Minecraft.",
                12, 0xFF9EADB5, false));

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void saveKey() {
        String value = keyInput.getText().toString().trim();
        if (!value.startsWith("sk-or-v1-") || value.length() < 24) {
            Toast.makeText(this, "Confira a chave OpenRouter. Ela deve começar com sk-or-v1-.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            SecureKeyStore.save(this, value);
            keyInput.setText("");
            refreshStatus();
            Toast.makeText(this, "Chave cifrada e salva neste aparelho.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível proteger a chave: " + safeMessage(e), Toast.LENGTH_LONG).show();
        }
    }

    private void requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Permissão já concedida.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void startOverlay() {
        try {
            String key = SecureKeyStore.read(this);
            if (key == null || key.isEmpty()) {
                Toast.makeText(this, "Salve sua chave OpenRouter primeiro.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Não consegui ler a chave protegida. Salve novamente.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            return;
        }
        Intent intent = new Intent(this, CompanionOverlayService.class);
        intent.setAction(CompanionOverlayService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
        Toast.makeText(this, "Janela flutuante iniciada. Abra o Minecraft.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void refreshStatus() {
        if (status == null) return;
        try {
            String key = SecureKeyStore.read(this);
            boolean overlay = Settings.canDrawOverlays(this);
            status.setText("Chave: " + (key == null ? "não configurada" : "salva e protegida")
                    + "\nJanela flutuante: " + (overlay ? "permitida" : "precisa de permissão"));
        } catch (Exception e) {
            status.setText("Chave: erro ao ler armazenamento seguro. Salve novamente.");
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setBackground(roundRect(0xFF176B66, 12));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setLineSpacing(dp(3), 1f);
        return t;
    }

    private void gap(LinearLayout parent, int height) {
        parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height)));
    }

    private android.graphics.drawable.GradientDrawable roundRect(int color, int radius) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null ? "erro interno" : message;
    }
}
