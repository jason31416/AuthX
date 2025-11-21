package cn.jason31416.authX;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import lombok.Getter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

@Plugin(id = "authx", name = "AuthX", version = "1.0.0", authors = {"Jason31416"})
public class AuthXPlugin {
    @Getter
    public static AuthXPlugin instance;

    @Getter
    private final Logger logger;
    @Getter
    private final ProxyServer proxy;
    @Getter
    private final File dataDirectory;

    public static Scheduler getScheduler() {
        return instance.proxy.getScheduler();
    }

    @Inject
    public AuthXPlugin(@Nonnull ProxyServer proxy, @Nonnull Logger logger, @Nonnull @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory.toFile();

        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {}

    public void info(String message) {
        logger.info(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void warning(String message) {
        logger.warn(message);
    }
}
