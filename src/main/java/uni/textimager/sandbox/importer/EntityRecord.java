package uni.textimager.sandbox.importer;

import java.util.Map;

/**
 * Immutable data holder for a parsed XMI element.
 * tag: the sanitized class name derived from the XML element’s tag.
 * attributes: map of sanitized attribute names to their string values,
 * includes "filename" entry with source file name.
 */
public record EntityRecord(String tag, Map<String, String> attributes) {
}
