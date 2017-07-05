package de.charite.zpgen;

/**
 * Hard-coded changes of ZFIN corrections.
 *
 * <p>
 * For several annotations that are normal we generate the abnormal counterpart. For this we
 * sometimes have to correct the PATO modifier used by the annotator. E.g. "normal amount" has to be
 * replace with amount, because the "normal"-tag already indicates the fact that this is normal.
 * </p>
 *
 * An example line is:
 *
 * <pre>
 * ZDB-GENE-030131-6223	100001615	51684	sufu					ZFA:0001086	muscle pioneer	PATO:0002050	normal amount	normal						
 * </pre>
 *
 * @author Sebastian Koehler
 */
public final class EntryCorrector {

  /** The entry to correct. */
  private final ZfinEntry entry;

  /**
   * Constructor.
   *
   * @param entry The {@link ZfinEntry} to correct.
   */
  public EntryCorrector(ZfinEntry entry) {
    this.entry = entry;
  }

  /**
   * @return The corrected {@link ZfinEntry}.
   */
  public ZfinEntry getCorrectedEntry() {
    if (!entry.isAbnormal) {
      if (entry.patoID.equals("PATO:0002050")) { // normal amount
        entry.patoID = "PATO:0000070";
        entry.patoName = "amount";
      } else if (entry.patoID.equals("PATO:0001905")) { // has normal numbers of parts of type
        entry.patoID = "PATO:0001555";
        entry.patoName = "has number of";
      } else if (entry.patoID.equals("PATO:0000461")) { // normal
        entry.patoID = "PATO:0000001";
        entry.patoName = "quality";
      }
    }
    return entry;
  }

}
