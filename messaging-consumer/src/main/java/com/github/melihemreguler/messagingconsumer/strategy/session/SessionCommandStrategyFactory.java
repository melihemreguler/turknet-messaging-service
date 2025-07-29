package com.github.melihemreguler.messagingconsumer.strategy.session;

import com.github.melihemreguler.messagingconsumer.enums.SessionCommand;
import com.github.melihemreguler.messagingconsumer.strategy.session.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SessionCommandStrategyFactory {
    
    private final SaveSessionStrategy saveSessionStrategy;
    private final UpsertSessionStrategy upsertSessionStrategy;
    private final UpdateSessionStrategy updateSessionStrategy;
    private final DeleteSessionStrategy deleteSessionStrategy;
    private final ExpireSessionStrategy expireSessionStrategy;
    
    private Map<SessionCommand, SessionCommandStrategy> strategies;
    
    public SessionCommandStrategy getStrategy(SessionCommand command) {
        if (strategies == null) {
            initializeStrategies();
        }
        return strategies.get(command);
    }
    
    private void initializeStrategies() {
        strategies = StrategyMapBuilder.builder()
                .withStrategy(SessionCommand.SAVE_SESSION, saveSessionStrategy)
                .withStrategy(SessionCommand.UPSERT_SESSION, upsertSessionStrategy)
                .withStrategy(SessionCommand.UPDATE_SESSION, updateSessionStrategy)
                .withStrategy(SessionCommand.DELETE_SESSION, deleteSessionStrategy)
                .withStrategy(SessionCommand.EXPIRE_SESSION, expireSessionStrategy)
                .build();
    }
    
   
    private static class StrategyMapBuilder {
        private final Map<SessionCommand, SessionCommandStrategy> strategies;
        
        private StrategyMapBuilder() {
            this.strategies = new EnumMap<>(SessionCommand.class);
        }
        
        public static StrategyMapBuilder builder() {
            return new StrategyMapBuilder();
        }
        
        public StrategyMapBuilder withStrategy(SessionCommand command, SessionCommandStrategy strategy) {
            strategies.put(command, strategy);
            return this;
        }
        
        public Map<SessionCommand, SessionCommandStrategy> build() {
            return new EnumMap<>(strategies); // Return defensive copy
        }
    }
}
