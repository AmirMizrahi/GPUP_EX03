package sharedChat;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextArea;

import java.util.Timer;
import java.util.stream.Collectors;

import static Utils.Constants.CHAT_LINE_FORMATTING;
import static Utils.Constants.DASHBOARD_REFRESH_RATE;

public class SharedChat {

    private static ChatAreaRefresher chatAreaRefresher;
    private static final IntegerProperty chatVersion = new SimpleIntegerProperty();
    private static Timer timer;
    private static TextArea menu2;
    private static TextArea menu3;

    public static void startChatRefresher(TextArea menu2, TextArea menu3) {
        SharedChat.menu2 = menu2;
        SharedChat.menu3 = menu3;

        chatAreaRefresher = new ChatAreaRefresher(
                chatVersion,
                SharedChat::updateChatLines);
        timer = new Timer();
        timer.schedule(chatAreaRefresher, DASHBOARD_REFRESH_RATE, DASHBOARD_REFRESH_RATE);
    }

    private static void updateChatLines(ChatLinesWithVersion chatLinesWithVersion) {
        if (chatLinesWithVersion.getVersion() != chatVersion.get()) {
            String deltaChatLines = chatLinesWithVersion
                    .getEntries()
                    .stream()
                    .map(singleChatLine -> {
                        long time = singleChatLine.getTime();
                        return String.format(CHAT_LINE_FORMATTING, time, time, time, singleChatLine.getUsername(), singleChatLine.getChatString());
                    }).collect(Collectors.joining());

            Platform.runLater(() -> {
                chatVersion.set(chatLinesWithVersion.getVersion());

                menu2.appendText(deltaChatLines);
                menu2.selectPositionCaret(menu2.getLength());
                menu2.deselect();

                menu3.appendText(deltaChatLines);
                menu3.selectPositionCaret(menu3.getLength());
                menu3.deselect();
            });
        }
    }
}
