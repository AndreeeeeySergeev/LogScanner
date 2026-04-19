//package alert.impl;
//
//import alert.AlertService;
//import model.LogEvent;
//
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//
//public class TelegramAlertService implements AlertService {
//
//    private final String botToken;
//    private final String chatId;
//
//    public TelegramAlertService(String botToken, String chatId) {
//        this.botToken = botToken;
//        this.chatId = chatId;
//    }
//
//    @Override
//    public void process(LogEvent event) {
//
//        if (event == null) return;
//
//        if (!isCritical(event.getLevel())) return;
//
//        send(event);
//    }
//
//    private boolean isCritical(String level) {
//        return level != null &&
//                (level.equalsIgnoreCase("ERROR") ||
//                        level.equalsIgnoreCase("CRITICAL"));
//    }
//
//    private void send(LogEvent event) {
//
//        try {
//            String text = URLEncoder.encode(format(event), StandardCharsets.UTF_8);
//
//            String urlString = "https://api.telegram.org/bot" + botToken +
//                    "/sendMessage?chat_id=" + chatId +
//                    "&text=" + text;
//
//            URL url = new URL(urlString);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            conn.setRequestMethod("GET");
//            conn.getResponseCode();
//
//        } catch (Exception e) {
//            System.err.println("Ошибка Telegram: " + e.getMessage());
//        }
//    }
//
//    private String format(LogEvent e) {
//        return "🚨 ALERT\n" +
//                "Time: " + e.getTimestamp() + "\n" +
//                "Source: " + e.getSource() + "\n" +
//                "Level: " + e.getLevel() + "\n" +
//                "Message: " + e.getMessage();
//    }
//}