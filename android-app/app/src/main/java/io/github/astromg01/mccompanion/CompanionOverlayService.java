package io.github.astromg01.mccompanion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompanionOverlayService extends Service {
    public static final String ACTION_START = "io.github.astromg01.mccompanion.START";
    private static final String CHANNEL_ID = "mc_companion_overlay";
    private static final int NOTIFICATION_ID = 731;

    private WindowManager windowManager;
    private View bubble;
    private View panel;
    private TextView conversation;
    private EditText input;
    private ScrollView chatScroll;
    private boolean expanded = false;
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private final List<OpenRouterClient.ChatMessage> history = new ArrayList<>();

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        try {
            history.addAll(CompanionMemoryStore.load(this));
        } catch (Exception ignored) {
            history.clear();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || ACTION_START.equals(intent.getAction())) showBubble();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("MC Companion AI ativo")
                .setContentText("O chat flutuante está pronto durante o jogo.")
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "MC Companion AI", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Mantém o serviço do app auxiliar em primeiro plano.");
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private void showBubble() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Permita a sobreposição nas configurações do app.", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }
        if (bubble != null) return;
        TextView icon = new TextView(this);
        icon.setText("MC");
        icon.setTextColor(Color.WHITE);
        icon.setTextSize(15);
        icon.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(circle(0xFF147D74));
        bubble = icon;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dp(54), dp(54), overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dp(12);
        params.y = dp(180);
        icon.setOnTouchListener(new View.OnTouchListener() {
            private float downX, downY;
            private int startX, startY;
            private boolean moved;
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startX = params.x;
                        startY = params.y;
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (downX - event.getRawX());
                        int dy = (int) (event.getRawY() - downY);
                        if (Math.abs(dx) + Math.abs(dy) > dp(5)) moved = true;
                        params.x = Math.max(0, startX + dx);
                        params.y = Math.max(dp(20), startY + dy);
                        try { windowManager.updateViewLayout(view, params); } catch (Exception ignored) { }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) expandPanel();
                        return true;
                }
                return false;
            }
        });
        try {
            windowManager.addView(icon, params);
        } catch (Exception e) {
            bubble = null;
            Toast.makeText(this, "Não consegui abrir a janela flutuante.", Toast.LENGTH_LONG).show();
        }
    }

    private void expandPanel() {
        if (expanded) return;
        expanded = true;
        if (bubble != null) {
            try { windowManager.removeView(bubble); } catch (Exception ignored) { }
            bubble = null;
        }
        panel = createPanel();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                Math.min(dp(340), getResources().getDisplayMetrics().widthPixels - dp(24)),
                dp(440), overlayType(),
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dp(8);
        params.y = dp(70);
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        try {
            windowManager.addView(panel, params);
        } catch (Exception e) {
            panel = null;
            expanded = false;
            showBubble();
        }
    }

    private View createPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.setBackground(roundRect(0xF0121B22, 18));
        root.setElevation(dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(label("COMPANION AI", 14, 0xFF39D6C3, true),
                new LinearLayout.LayoutParams(0, -2, 1));
        Button close = smallButton("×");
        close.setOnClickListener(v -> collapsePanel());
        header.addView(close, new LinearLayout.LayoutParams(dp(38), dp(36)));
        root.addView(header);

        root.addView(label("OpenRouter · openrouter/free · memória local cifrada", 10, 0xFFAAB8C0, false));
        chatScroll = new ScrollView(this);
        conversation = label("Estou aqui com você.\n\nPosso conversar e ajudar com ideias, receitas e planos. Ainda não vejo o mundo do jogo; me conte o que está acontecendo.\n",
                13, Color.WHITE, false);
        conversation.setPadding(dp(9), dp(10), dp(9), dp(10));
        for (OpenRouterClient.ChatMessage item : history) {
            conversation.append(("user".equals(item.role) ? "Você: " : "MC Companion: ")
                    + item.content + "\\n\\n");
        }
        chatScroll.addView(conversation);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1);
        scrollParams.topMargin = dp(8);
        scrollParams.bottomMargin = dp(8);
        root.addView(chatScroll, scrollParams);

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.BOTTOM);
        input = new EditText(this);
        input.setHint("Fala comigo…");
        input.setHintTextColor(0xFF819099);
        input.setTextColor(Color.WHITE);
        input.setTextSize(13);
        input.setMinLines(1);
        input.setMaxLines(3);
        input.setPadding(dp(10), dp(9), dp(10), dp(9));
        input.setBackground(roundRect(0xFF202C34, 12));
        composer.addView(input, new LinearLayout.LayoutParams(0, -2, 1));
        Button send = smallButton("➤");
        send.setOnClickListener(v -> sendMessage());
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(dp(46), dp(46));
        sendParams.leftMargin = dp(7);
        composer.addView(send, sendParams);
        root.addView(composer);
        TextView footer = label("Toque em × para recolher. Arraste o botão MC para mover.", 10, 0xFF82919A, false);
        LinearLayout.LayoutParams foot = new LinearLayout.LayoutParams(-1, -2);
        foot.topMargin = dp(5);
        root.addView(footer, foot);
        return root;
    }

    private void sendMessage() {
        if (input == null || conversation == null) return;
        String message = input.getText().toString().trim();
        if (message.isEmpty()) return;
        input.setText("");
        append("Você", message);
        append("MC Companion", "Pensando…");
        input.setEnabled(false);
        network.execute(() -> {
            String answer;
            try {
                String key = SecureKeyStore.read(this);
                if (key == null) throw new IllegalStateException("Chave OpenRouter não configurada. Abra o app e salve uma chave nova.");
                answer = OpenRouterClient.chat(key, history, message);
                history.add(new OpenRouterClient.ChatMessage("user", message));
                history.add(new OpenRouterClient.ChatMessage("assistant", answer));
                while (history.size() > 16) history.remove(0);
                try {
                    CompanionMemoryStore.save(this, history);
                } catch (Exception ignored) { }
            } catch (OpenRouterClient.ApiException e) {
                answer = explainApiError(e.status, e.getMessage());
            } catch (Exception e) {
                answer = "Não consegui falar com a IA: " + safeMessage(e);
            }
            final String finalAnswer = answer;
            mainThread(() -> {
                if (conversation != null) {
                    String current = conversation.getText().toString();
                    int idx = current.lastIndexOf("MC Companion: Pensando…");
                    if (idx >= 0) current = current.substring(0, idx);
                    conversation.setText(current + "MC Companion: " + finalAnswer + "\n\n");
                    if (chatScroll != null) chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
                }
                if (input != null) input.setEnabled(true);
            });
        });
    }

    private void append(String who, String message) {
        conversation.append(who + ": " + message + "\n\n");
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private String explainApiError(int status, String detail) {
        if (status == 401) return "A chave foi recusada (401). Confira ou substitua a chave no app.";
        if (status == 402) return "O provedor recusou a solicitação por saldo ou política (402). Confira sua conta OpenRouter.";
        if (status == 429) return "Limite de solicitações atingido (429). Tente novamente mais tarde.";
        if (status == 404) return "O roteador/modelo não está disponível nesta tentativa (404). Tente mais tarde.";
        return "Erro OpenRouter " + status + ": " + detail;
    }

    private void collapsePanel() {
        if (panel != null) {
            try { windowManager.removeView(panel); } catch (Exception ignored) { }
            panel = null;
        }
        conversation = null;
        input = null;
        chatScroll = null;
        expanded = false;
        showBubble();
    }

    private void mainThread(Runnable action) {
        new android.os.Handler(getMainLooper()).post(action);
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null ? "erro de conexão" : message;
    }

    private Button smallButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setPadding(0, 0, 0, 0);
        button.setBackground(roundRect(0xFF176B66, 11));
        return button;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setLineSpacing(dp(2), 1f);
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return text;
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), 0xFF30414A);
        return drawable;
    }

    private GradientDrawable circle(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setStroke(dp(2), 0xFFB8FFF3);
        return drawable;
    }

    private int overlayType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    @Override
    public void onDestroy() {
        if (bubble != null) {
            try { windowManager.removeView(bubble); } catch (Exception ignored) { }
            bubble = null;
        }
        if (panel != null) {
            try { windowManager.removeView(panel); } catch (Exception ignored) { }
            panel = null;
        }
        network.shutdownNow();
        super.onDestroy();
    }
}
