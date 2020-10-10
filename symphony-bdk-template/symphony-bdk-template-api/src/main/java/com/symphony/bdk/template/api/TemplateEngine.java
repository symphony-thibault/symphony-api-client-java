package com.symphony.bdk.template.api;

import org.apiguardian.api.API;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Interface to represent a specific template engine backed by a specific technology.
 * It creates {@link Template} instances based on a file provided in the file system, in the classpath or as a URL
 */
@API(status = API.Status.STABLE)
public interface TemplateEngine {

  /**
   * Create a {@link Template} instance from a file on the file system
   * @param templatePath path to a template file on the file system
   * @return a new {@link Template} instantiated from the provided file
   * @throws TemplateException when template cannot be loaded, e.g. file not accessible
   */
  Template newTemplateFromFile(String templatePath) ;

  /**
   * Create a {@link Template} instance from a file in the classpath
   * @param templatePath full path to a template file in the classpath
   * @return a new {@link Template} instantiated from the provided classpath resource
   * @throws TemplateException when template cannot be loaded, e.g. resource not accessible
   */
  Template newTemplateFromClasspath(String templatePath) ;

  static TemplateEngine getDefaultImplementation() {
    final ServiceLoader<TemplateEngine> engineServiceLoader = ServiceLoader.load(TemplateEngine.class);

    final List<TemplateEngine> templateEngines = StreamSupport.stream(engineServiceLoader.spliterator(), false)
            .collect(Collectors.toList());

    if (templateEngines.isEmpty()) {
      throw new IllegalStateException("No TemplateEngine implementation found in classpath.");
    } else if (templateEngines.size() > 1) {
      LoggerFactory.getLogger(TemplateEngine.class)
          .warn("More than 1 TemplateEngine implementation found in classpath, will use : {}",
          templateEngines.stream().findFirst().get());
    }
    return templateEngines.stream().findFirst().get();
  }
}
