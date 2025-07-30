package com.github.melihemreguler.messagingconsumer.strategy.user;

import com.github.melihemreguler.messagingconsumer.strategy.user.UserActivityStrategyFactory;
import com.github.melihemreguler.messagingconsumer.strategy.user.impl.UnifiedUserActivityStrategy;
import com.github.melihemreguler.messagingconsumer.enums.UserActivityCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserActivityStrategyFactoryTest {
    private UserActivityStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new UserActivityStrategyFactory(new UnifiedUserActivityStrategy(null));
    }

    @Test
    void getStrategy_shouldReturnUnifiedStrategyForUserCreation() {
        //GIVEN
        UserActivityCommand command = UserActivityCommand.USER_CREATION;

        //WHEN
        var strategy = factory.getStrategy(command);

        //THEN
        assertTrue(strategy instanceof UnifiedUserActivityStrategy);
    }

    @Test
    void getStrategy_shouldReturnUnifiedStrategyForLoginAttempt() {
        //GIVEN
        UserActivityCommand command = UserActivityCommand.LOGIN_ATTEMPT;

        //WHEN
        var strategy = factory.getStrategy(command);

        //THEN
        assertTrue(strategy instanceof UnifiedUserActivityStrategy);
    }

    @Test
    void getStrategy_shouldThrowExceptionForNullCommand() {
        //GIVEN
        UserActivityCommand command = null;

        //WHEN
        var strategy = factory.getStrategy(command);

        //THEN
        assertNull(strategy);
    }
}
