package com.github.melihemreguler.messagingconsumer.strategy.session;

import com.github.melihemreguler.messagingconsumer.model.SessionEvent;

public interface SessionCommandStrategy {
    void execute(SessionEvent sessionEvent);
}
