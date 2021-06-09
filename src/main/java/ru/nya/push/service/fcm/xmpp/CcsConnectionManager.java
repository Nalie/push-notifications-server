package ru.nya.push.service.fcm.xmpp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.nya.push.rest.BusinessRuntimeException;
import ru.nya.push.service.fcm.FcmSettings;
import ru.nya.push.service.fcm.xmpp.backoff.BackOffStrategy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.nya.push.service.fcm.xmpp.ConnectionStatus.ACTIVE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcsConnectionManager {

    private static final List<CcsClient> ccsClientList = new ArrayList<>();

    private final FcmSettings settings;
    private final MessageReceiver messageReceiver;
    private final ApplicationEventPublisher applicationEventPublisher;

    public synchronized CcsClient getConnection() {
        Optional<CcsClient> client = ccsClientList.stream()
                .filter(x -> ACTIVE.equals(x.getStatus()))
                .findFirst();
        return client.orElseGet(this::createNewClient);
    }

    private CcsClient createNewClient() {
        CcsClient ret = new CcsClient(settings, messageReceiver, applicationEventPublisher);
        final BackOffStrategy backoff = new BackOffStrategy(5, 1000);
        while (backoff.shouldRetry()) {
            if (ACTIVE.equals(ret.getStatus())) {
                backoff.doNotRetry();
                ccsClientList.add(ret);
                return ret;
            }
        }
        ret.preDestroy();
        log.error("Couldn't create CcsClient");
        throw new BusinessRuntimeException("Couldn't create CcsClient");
    }

    public CcsClient getExisted() {
        Optional<CcsClient> client = ccsClientList.stream()
                .filter(x -> ACTIVE.equals(x.getStatus()))
                .findFirst();
        return client.orElseThrow(() -> {throw new BusinessRuntimeException("Connection doesn't exist");});
    }

    //TODO запускать по расписанию
    public synchronized void destroyInactive() {
        ccsClientList.stream()
                .filter(x -> ConnectionStatus.DEAD.equals(x.getStatus()))
                .forEach(CcsClient::preDestroy);
        ccsClientList.removeIf(x -> ConnectionStatus.DEAD.equals(x.getStatus()));
    }

    @PreDestroy
    public void destroyAll() {
        log.info("Destroy all connections");
        ccsClientList.forEach(CcsClient::preDestroy);
        ccsClientList.clear();
    }
}
