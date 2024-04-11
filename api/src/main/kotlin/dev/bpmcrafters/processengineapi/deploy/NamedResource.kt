package dev.bpmcrafters.processengineapi.deploy

import java.io.FileInputStream
import java.io.InputStream


/**
 * Named resource stream.
 * @since 0.0.1
 */
data class NamedResource(
  /**
   * Resource name.
   */
  val name: String,
  /**
   * Bytes representing the resource.
   */
  val resourceStream: InputStream,
  /**
   * Additional headers for resource description.
   */
  val resourceHeaders: Map<String, String> = mapOf()
) {
  companion object {

    /**
     * Constructs named resource from a classpath resource.
     * @param classpathResource class path resource.
     */
    @JvmStatic
    fun fromClasspath(classpathResource: String): NamedResource {
      return NamedResource(name = classpathResource, resourceStream = NamedResource::class.java
        .classLoader
        .getResourceAsStream(classpathResource) ?: throw IllegalArgumentException("Resource $classpathResource not found")
      )
    }

    /**
     * Constructs named resource from a file.
     * @param filename name of the file.
     */
    @JvmStatic
    fun fromFile(filename: String): NamedResource {
      return FileInputStream(filename)
        .use { inputStream ->
          NamedResource(name = filename, resourceStream = inputStream)
        }
    }
  }
}
