package test;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lhotse.config.core.SingleStorage;
import org.lhotse.config.core.Storage;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    final Storage<Integer, UserConfig> userConfigStorage;
    final SingleStorage<SystemConfig> systemConfig;

    @PostConstruct
    void init() {
        log.info("user configs: {}", userConfigStorage.listConfig());
        log.info("system config: {}", systemConfig.value());
    }
}
