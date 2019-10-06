package webserver.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;
import webserver.BadRequestException;
import webserver.controller.Controller;
import webserver.controller.PageTemplateController;
import webserver.pageprovider.FilePageProvider;

import java.util.*;

public class FileServerRouter implements Router {
    private static final Logger log = LoggerFactory.getLogger(FileServerRouter.class);

    private final List<String> directoryPrefixes = Arrays.asList(
            "",
            "static",
            "templates"
    );

    private final Map<String, FilePageProvider> filePageProviders = new HashMap<String, FilePageProvider>() {{
        for (String directoryPrefix : directoryPrefixes) {
            put(directoryPrefix, FilePageProvider.fromDirectory(directoryPrefix));
        }
    }};

    private FileServerRouter() {
    }

    private static class BillPughSingleton {
        private static final FileServerRouter INSTANCE = new FileServerRouter();
    }

    public static Router getInstance() {
        return BillPughSingleton.INSTANCE;
    }

    @Override
    public Controller retrieveController(String pattern) {
        String prefix = findFirstPrefixCanHandle(pattern)
                .orElseThrow(() -> BadRequestException.ofPattern(pattern));
        log.debug("prefix :{}, pattern: {}", prefix, pattern);

        return PageTemplateController.ofGetPageProvider(filePageProviders.get(prefix));
    }

    @Override
    public boolean canHandle(String pattern) {
        return findFirstPrefixCanHandle(pattern).isPresent();
    }

    private Optional<String> findFirstPrefixCanHandle(String pattern) {
        return directoryPrefixes.stream()
                .filter(prefix -> canHandleWithPrefix(prefix, pattern))
                .findFirst();
    }

    private boolean canHandleWithPrefix(String prefix, String pattern) {
        return FileIoUtils.canUseResourceFromFilePath(String.format("./%s%s", prefix, pattern));
    }
}
