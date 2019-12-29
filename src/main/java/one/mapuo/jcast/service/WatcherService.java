package one.mapuo.jcast.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by @author mapuo on 28/12/2019.
 */
@Slf4j
@Service
public class WatcherService {

  private final WatchService watchService;

  public WatcherService()
      throws IOException {

    watchService = FileSystems.getDefault().newWatchService();
  }

  @PreDestroy
  public void destroy()
      throws IOException {

    watchService.close();
  }

  @Async
  public void add(File file, Consumer<WatchEvent<?>> consumer) {
    try {
      Path path = file.toPath();
      Path directory = path.getParent();
      directory.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
      WatchKey key;
      while ((key = watchService.take()) != null) {
        key.pollEvents().stream()
            .filter(event -> event.context().equals(path.getFileName()))
            .findFirst()
            .ifPresent(event -> {
              log.info("File '{}' has been modified!", file);
              try {
                consumer.accept(event);
              } catch (Exception e) {
                log.warn("Error while processing change!", e);
              }
            });
        boolean valid = key.reset();
        if (!valid) {
          log.debug("Key has been unregistered");
        }
      }
    } catch (Exception e) {
      log.warn("Watching has stopped because: {}", e.getMessage());
    }
  }

}
