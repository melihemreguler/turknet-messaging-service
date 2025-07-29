package com.github.melihemreguler.messagingconsumer.strategy.user;

import com.github.melihemreguler.messagingconsumer.enums.UserActivityCommand;
import com.github.melihemreguler.messagingconsumer.strategy.user.impl.UnifiedUserActivityStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserActivityStrategyFactory {
    
    private final UnifiedUserActivityStrategy unifiedUserActivityStrategy;
    
    private Map<UserActivityCommand, UserActivityStrategy> strategies;
    
    public UserActivityStrategy getStrategy(UserActivityCommand command) {
        if (strategies == null) {
            initializeStrategies();
        }
        return strategies.get(command);
    }
    
    private void initializeStrategies() {
        strategies = StrategyMapBuilder.builder()
                .withStrategy(UserActivityCommand.USER_CREATION, unifiedUserActivityStrategy)
                .withStrategy(UserActivityCommand.LOGIN_ATTEMPT, unifiedUserActivityStrategy)
                .build();
    }
    
    
    private static class StrategyMapBuilder {
        private final Map<UserActivityCommand, UserActivityStrategy> strategies;
        
        private StrategyMapBuilder() {
            this.strategies = new EnumMap<>(UserActivityCommand.class);
        }
        
        public static StrategyMapBuilder builder() {
            return new StrategyMapBuilder();
        }
        
        public StrategyMapBuilder withStrategy(UserActivityCommand command, UserActivityStrategy strategy) {
            strategies.put(command, strategy);
            return this;
        }
        
        public Map<UserActivityCommand, UserActivityStrategy> build() {
            return new EnumMap<>(strategies); // Return defensive copy
        }
    }
}
