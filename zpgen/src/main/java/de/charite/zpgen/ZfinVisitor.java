package de.charite.zpgen;

import java.io.BufferedWriter;

/**
 * The visitor interface.
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public interface ZfinVisitor {

  /**
   * This method is called for any entry. Not that the entry is recycled.
   * 
   * @param entry The currently visited {@link ZfinEntry}.
   * @param outPositiveAnnotations {@link BufferedWriter} to write positive annotations out to.
   * @param outNegativeAnnotations {@link BufferedWriter} to write negative annotations out to.
   * @return {@code true} if we should continue visiting/iterating, {@code false} if we are to stop.
   */
  boolean visit(ZfinEntry entry, BufferedWriter outPositiveAnnotations,
      BufferedWriter outNegativeAnnotations);

}
